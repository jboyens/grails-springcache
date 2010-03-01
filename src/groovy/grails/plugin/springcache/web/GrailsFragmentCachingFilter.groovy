package grails.plugin.springcache.web

import grails.plugin.springcache.SpringcacheService
import grails.plugin.springcache.annotations.CacheFlush
import grails.plugin.springcache.annotations.Cacheable
import grails.plugin.springcache.web.key.KeyGenerator
import java.lang.annotation.Annotation
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import net.sf.ehcache.CacheManager
import net.sf.ehcache.constructs.web.GenericResponseWrapper
import net.sf.ehcache.constructs.web.PageInfo
import net.sf.ehcache.constructs.web.filter.PageFragmentCachingFilter
import org.codehaus.groovy.grails.web.servlet.WrappedResponseHolder
import org.codehaus.groovy.grails.web.util.WebUtils
import org.slf4j.LoggerFactory

class GrailsFragmentCachingFilter extends PageFragmentCachingFilter {

	private static final REQUEST_CACHE_NAME_ATTR = "${GrailsFragmentCachingFilter.name}.CACHE"
	private static final REQUEST_CACHE_CONTEXT_ATTR = "${GrailsFragmentCachingFilter.name}.CACHE_CONTEXT"

	private final log = LoggerFactory.getLogger(getClass())
	private final timingLog = LoggerFactory.getLogger("${getClass().name}.TIMINGS")
	SpringcacheService springcacheService
	CacheManager cacheManager
	KeyGenerator keyGenerator

	/**
	 * Overrides doInit in CachingFilter to be a no-op. The superclass initializes a single cache that is used for all
	 * intercepted requests but we will select a cache at runtime based on the target controller/action.
	 */
	@Override
	void doInit(FilterConfig filterConfig) {
		// don't do anything - we need to get caches differently for each request
	}

	/**
	 * Overrides doFilter in PageFragmentCachingFilter to handle flushing and caching behaviour selectively depending
	 * on annotations on target controller.
	 */
	@Override protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
		request[REQUEST_CACHE_CONTEXT_ATTR] = new FilterContext()
		if (handleFlush(request)) {
			chain.doFilter(request, response)
		} else if (shouldCache(request)) {
			super.doFilter(request, response, chain)
		} else {
			chain.doFilter(request, response)
		}
	}

	/**
	 * Overrides buildPageInfo in PageFragmentCachingFilter to use different cache depending on target controller rather
	 * than having the cache wired into the filter.
	 */
	@Override protected PageInfo buildPageInfo(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
		def timer = new Timer()
		timer.start(getCachedUri(request))
		boolean isCached = true

		def cacheName = request[REQUEST_CACHE_NAME_ATTR]
		def key = calculateKey(request)
		PageInfo pageInfo = springcacheService.withBlockingCache(cacheName, key) {
			isCached = false
			def pageInfo = buildPage(request, response, chain)
			if (pageInfo.isOk()) {
				log.debug "PageInfo ok. Adding to cache $cacheName with key $key"
				return pageInfo
			} else {
				log.debug "PageInfo was not ok(200). Putting null into cache $cacheName with key $key"
				return null
			}
		}
		
		timer.stop(isCached)
		return pageInfo
	}

	/**
	 * Overrides buildPage in PageFragmentCachingFilter to use different cache depending on target controller and to do
	 * special handling for Grails include requests.
	 */
	@Override protected PageInfo buildPage(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
		// Invoke the next entity in the chain
		def outstr = new ByteArrayOutputStream()
		def wrapper = new GenericResponseWrapper(response, outstr)

		// TODO: split the special include handling out into a separate method
		def originalResponse = null
		def isInclude = WebUtils.isIncludeRequest(request)
		if (isInclude) {
			originalResponse = WrappedResponseHolder.wrappedResponse
			WrappedResponseHolder.wrappedResponse = wrapper
		}
		try {
			chain.doFilter(request, wrapper)
		} finally {
			if (isInclude) {
				WrappedResponseHolder.wrappedResponse = originalResponse
			}
		}
		wrapper.flush()

		long timeToLiveSeconds = cacheManager.getEhcache(request[REQUEST_CACHE_NAME_ATTR]).cacheConfiguration.timeToLiveSeconds

		def contentType = wrapper.contentType ?: response.contentType
		return new PageInfo(wrapper.status, contentType, wrapper.headers, wrapper.cookies,
				outstr.toByteArray(), false, timeToLiveSeconds)
	}

	/**
	 * Overrides writeResponse in PageFragmentCachingFilter to set the contentType before writing the response. This is
	 * necessary so that Sitemesh is activated (yeah, setContentType on GrailsContentBufferingResponse has a
	 * side-effect) and will decorate our cached response.
	 */
	@Override protected void writeResponse(HttpServletResponse response, PageInfo pageInfo) {
		response.contentType = pageInfo.contentType
		super.writeResponse(response, pageInfo)
	}

	@Override protected CacheManager getCacheManager() {
		throw new UnsupportedOperationException("Not supported in this implementation")
	}

	/**
	 * Creates a cache key based on the target controller and action and any params (i.e. you want to cache
	 * /pirate/show/1 and /pirate/show/2 separately).
	 */
	@Override protected String calculateKey(HttpServletRequest request) {
		def context = request[REQUEST_CACHE_CONTEXT_ATTR]
		return keyGenerator.generateKey(context).toString()
	}

	private boolean shouldCache(HttpServletRequest request) {
		def context = request[REQUEST_CACHE_CONTEXT_ATTR]
		Cacheable cacheable = getAnnotation(context, Cacheable)
		if (cacheable) {
			request[REQUEST_CACHE_NAME_ATTR] = cacheable.value()
			logRequestDetails(request, context, "Caching request")
			return true
		} else {
			log.debug "No cacheable annotation found for $request.method:$request.requestURI $context"
			return false
		}
	}

	boolean handleFlush(HttpServletRequest request) {
		def context = request[REQUEST_CACHE_CONTEXT_ATTR]
		CacheFlush cacheFlush = getAnnotation(context, CacheFlush)
		if (cacheFlush) {
			logRequestDetails(request, context, "Flushing request")
			springcacheService.flush(cacheFlush.value())
			return true
		} else {
			log.debug "No cacheflush annotation found for $request.method:$request.requestURI $context"
			return false
		}
	}

	private Annotation getAnnotation(FilterContext context, Class type) {
		// TODO: cache this by controller/action
		def annotation = context.actionClosure?.getAnnotation(type)
		if (!annotation) {
			annotation = context.controllerArtefact?.clazz?.getAnnotation(type)
		}
		return annotation
	}

	private void logRequestDetails(HttpServletRequest request, FilterContext context, String message) {
		if (log.isDebugEnabled()) {
			log.debug "$message..."
			log.debug "    method = $request.method"
			log.debug "    requestURI = $request.requestURI"
			log.debug "    forwardURI = $request.forwardURI"
			if (WebUtils.isIncludeRequest(request)) {
				log.debug "    includeURI = ${request[WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE]}"
			}
			log.debug "    controller = $context.controllerName"
			log.debug "    action = $context.actionName"
			log.debug "    params = $context.params"
		}
	}

	private String getCachedUri(HttpServletRequest request) {
		if (WebUtils.isIncludeRequest(request)) {
			return request[WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE]
		}
		return request.requestURI
	}

}
