package grails.plugin.springcache.web

import grails.plugin.springcache.CacheProvider
import grails.plugin.springcache.annotations.CacheFlush
import grails.plugin.springcache.annotations.Cacheable
import java.lang.annotation.Annotation
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import net.sf.ehcache.CacheManager
import net.sf.ehcache.Element
import net.sf.ehcache.constructs.blocking.BlockingCache
import net.sf.ehcache.constructs.blocking.LockTimeoutException
import net.sf.ehcache.constructs.web.GenericResponseWrapper
import net.sf.ehcache.constructs.web.PageInfo
import net.sf.ehcache.constructs.web.filter.PageFragmentCachingFilter
import org.codehaus.groovy.grails.web.servlet.WrappedResponseHolder
import org.codehaus.groovy.grails.web.util.WebUtils
import org.slf4j.LoggerFactory
import grails.plugin.springcache.web.key.KeyGenerator

class GrailsFragmentCachingFilter extends PageFragmentCachingFilter {

	private static final REQUEST_CACHE_ATTR = "${GrailsFragmentCachingFilter.name}.CACHE"
	private static final REQUEST_CACHE_CONTEXT_ATTR = "${GrailsFragmentCachingFilter.name}.CACHE_CONTEXT"

	private final log = LoggerFactory.getLogger(getClass())
	private final timingLog = LoggerFactory.getLogger("${getClass().name}.TIMINGS")
	CacheProvider cacheProvider
	KeyGenerator keyGenerator

	/**
	 * Overrides doInit in CachingFilter to be a no-op. The superclass initializes a single cache that is used for all
	 * intercepted requests but we will select a cache at runtime based on the target controller/action.
	 */
	@Override void doInit(FilterConfig filterConfig) {
		// don't do anything - we need to get caches differently for each request
	}

	/**
	 * Overrides doFilter in PageFragmentCachingFilter to handle flushing and caching behaviour selectively depending
	 * on annotations on target controller.
	 */
	@Override protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
		request[REQUEST_CACHE_CONTEXT_ATTR] = new FilterContext()
		if (shouldFlush(request)) {
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
		timer.start()
		// Look up the cached page
		BlockingCache cache = getCache(request)
		final key = calculateKey(request)
		PageInfo pageInfo = null
		try {
			Element element = cache.get(key)
			if (element == null || element.getObjectValue() == null) {
				try {
					// Page is not cached - build the response, cache it, and send to client
					pageInfo = buildPage(request, response, chain)
					if (pageInfo.isOk()) {
						log.debug "PageInfo ok. Adding to cache $cache.name with key $key"
						cache.put(new Element(key, pageInfo))
					} else {
						log.debug "PageInfo was not ok(200). Putting null into cache $cache.name with key $key"
						cache.put(new Element(key, null))
					}
				} catch (Throwable throwable) {
					// Must unlock the cache if the above fails. Will be logged at Filter
					cache.put(new Element(key, null))
					throw new Exception(throwable)
				}
				timer.stop("Uncached request for ${getCachedUri(request)}")
			} else {
				log.debug "Serving cached content for $key"
				pageInfo = element.getObjectValue()
				timer.stop("Cached request for ${getCachedUri(request)}")
			}
		} catch (LockTimeoutException e) {
			//do not release the lock, because you never acquired it
			throw e
		}
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

		long timeToLiveSeconds = getCache(request).cacheConfiguration.timeToLiveSeconds

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

	/**
	 * This should never get called in this implementation.
	 */
	protected CacheManager getCacheManager() {
		throw new UnsupportedOperationException("Filter should not be calling getCacheManager")
	}

	/**
	 * Creates a cache key based on the target controller and action and any params (i.e. you want to cache
	 * /pirate/show/1 and /pirate/show/2 separately).
	 */
	protected String calculateKey(HttpServletRequest request) {
		def context = request[REQUEST_CACHE_CONTEXT_ATTR]
		return keyGenerator.generateKey(context, request).toString()
	}

	private BlockingCache getCache(HttpServletRequest request) {
		request.getAttribute(REQUEST_CACHE_ATTR)
	}

	private void setCache(HttpServletRequest request, BlockingCache cache) {
		request.setAttribute(REQUEST_CACHE_ATTR, cache)
	}

	private boolean shouldCache(HttpServletRequest request) {
		def context = request[REQUEST_CACHE_CONTEXT_ATTR]
		Cacheable cacheable = getAnnotation(context, Cacheable)
		if (cacheable) {
			def cache = cacheProvider.getCache(cacheable.modelId()).wrappedCache
			// TODO: check cache is blocking otherwise we can't use it
			setCache(request, cache)
			if (log.isDebugEnabled()) {
				log.debug "Caching request..."
				logRequestDetails(request, context)
				log.debug "    cache = $cache.name"
			}
			return true
		} else {
			log.debug "No cacheable annotation found for $request.method:$request.requestURI $context"
			return false
		}
	}

	boolean shouldFlush(HttpServletRequest request) {
		def context = request[REQUEST_CACHE_CONTEXT_ATTR]
		CacheFlush cacheFlush = getAnnotation(context, CacheFlush)
		if (cacheFlush) {
			def caches = cacheProvider.getCaches(cacheFlush.modelId())*.wrappedCache
			if (log.isDebugEnabled()) {
				log.debug "Flushing request..."
				logRequestDetails(request, context)
				log.debug "    caches = ${caches.name.join(', ')}"
			}
			caches*.flush() // TODO: methods with side-effects suck - move this out
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

	private void logRequestDetails(HttpServletRequest request, FilterContext context) {
		log.debug "    method = $request.method"
		log.debug "    requestURI = $request.requestURI"
		log.debug "    forwardURI = $request.forwardURI"
		if (WebUtils.isIncludeRequest(request)) {
			log.debug "    includeURI = ${request.getAttribute(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE)}"
		}
		log.debug "    controller = $context.controllerName"
		log.debug "    action = $context.actionName"
		log.debug "    params = $context.params"
	}

	private String getCachedUri(HttpServletRequest request) {
		if (WebUtils.isIncludeRequest(request)) {
			return request.getAttribute(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE)
		}
		return request.requestURI
	}

}
