package grails.plugin.springcache.web

import grails.plugin.springcache.CacheProvider
import grails.plugin.springcache.annotations.Cacheable
import grails.plugin.springcache.web.CachingFilterContext
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
import org.slf4j.LoggerFactory
import org.codehaus.groovy.grails.web.sitemesh.GrailsPageFilter
import org.codehaus.groovy.grails.web.sitemesh.GSPSitemeshPage
import org.codehaus.groovy.grails.web.sitemesh.GrailsContentBufferingResponse

class ContentCachingFilter extends PageFragmentCachingFilter {

	private static final REQUEST_CACHE_ATTR = "${ContentCachingFilter.name}.CACHE"

	private final log = LoggerFactory.getLogger(getClass())
	CacheProvider cacheProvider

	@Override void doInit(FilterConfig filterConfig) {
		// don't do anything - we need to get caches differently for each request
	}

	protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
		if (shouldCache(request)) {
			PageInfo pageInfo = buildPageInfo(request, response, chain)
			writeResponse(request, response, pageInfo)
		} else {
			chain.doFilter(request, response)
		}
	}

	protected PageInfo buildPageInfo(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
		// Look up the cached page
		BlockingCache cache = getCache(request)
		final key = calculateKey(request)
		GrailsSitemeshPageInfo pageInfo = null
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
			} else {
				log.debug "Serving cached content for $key"
				pageInfo = element.getObjectValue()
			}
		} catch (LockTimeoutException e) {
			//do not release the lock, because you never acquired it
			throw e
		}
		return pageInfo
	}

	protected PageInfo buildPage(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
		// Invoke the next entity in the chain
		def outstr = new ByteArrayOutputStream()
		def wrapper = new GenericResponseWrapper(response, outstr)
		chain.doFilter(request, wrapper)
		wrapper.flush()

		boolean storeGzipped = false
		long timeToLiveSeconds = getCache(request).cacheConfiguration.timeToLiveSeconds

		def gspSitemeshPage = request.getAttribute(GrailsPageFilter.GSP_SITEMESH_PAGE)
		if (gspSitemeshPage) log.debug "Got GSP_SITEMESH_PAGE: $gspSitemeshPage"

		def pageInfo = new GrailsSitemeshPageInfo(wrapper.status, wrapper.contentType, wrapper.headers, wrapper.cookies, outstr.toByteArray(), storeGzipped, timeToLiveSeconds)
		pageInfo.gspSitemeshPage = copyOf(gspSitemeshPage)
		return pageInfo
	}

	protected CacheManager getCacheManager() {
		throw new UnsupportedOperationException("Filter should not be calling getCacheManager")
	}

	protected String calculateKey(HttpServletRequest request) {
		def buffer = new StringBuilder()
		buffer << request.requestURI
		if (request.queryString) {
			buffer << request.queryString
		}
		return buffer.toString()
	}

	protected void writeResponse(HttpServletRequest request, HttpServletResponse response, PageInfo pageInfo) {
		if (pageInfo.gspSitemeshPage) {
			request.setAttribute(GrailsPageFilter.GSP_SITEMESH_PAGE, pageInfo.gspSitemeshPage)
		} else {
			super.writeResponse(response, pageInfo)
		}
	}

	private BlockingCache getCache(HttpServletRequest request) {
		request.getAttribute(REQUEST_CACHE_ATTR)
	}

	private void setCache(HttpServletRequest request, BlockingCache cache) {
		request.setAttribute(REQUEST_CACHE_ATTR, cache)
	}

	private boolean shouldCache(HttpServletRequest request) {
		def context = new CachingFilterContext()
		Cacheable cacheable = getAnnotation(context, Cacheable)
		if (cacheable) {
			if (log.isDebugEnabled()) {
			log.debug "Caching filter intercepting request..."
				log.debug "    method = $request.method"
				log.debug "    requestURI = $request.requestURI"
				log.debug "    controller = $context.controllerName"
				log.debug "    action = $context.actionName"
				log.debug "    forwardURI = $request.forwardURI"
			}
			def cache = cacheProvider.getCache(cacheable.modelId()).wrappedCache
			setCache(request, cache)
			return true
		} else {
			log.debug "No cacheable annotation found for $request.method:$request.requestURI $context"
			return false
		}
	}

	private Annotation getAnnotation(CachingFilterContext context, Class type) {
		// TODO: cache this by controller/action
		def annotation = context.actionClosure?.getAnnotation(type)
		if (!annotation) {
			annotation = context.controllerArtefact?.clazz?.getAnnotation(type)
		}
		return annotation
	}

	private GSPSitemeshPage copyOf(GSPSitemeshPage original) {
		def copy = new GSPSitemeshPage()
		copy.@headBuffer = original.@headBuffer
		copy.@bodyBuffer = original.@bodyBuffer
		copy.@pageBuffer = original.@pageBuffer
		copy.@used = original.@used
		copy.@titleCaptured = original.@titleCaptured
		copy.@contentBuffers = original.@contentBuffers?.clone()
		copy.properties.putAll(original.properties)
		copy.@pageData = original.@pageData
		if (original.request) {
			copy.request = original.request
		}
		return copy
	}
}

class GrailsSitemeshPageInfo extends PageInfo {

	GSPSitemeshPage gspSitemeshPage

	GrailsSitemeshPageInfo(int statusCode, String contentType, Collection headers, Collection cookies, byte[] body, boolean storeGzipped, long timeToLiveSeconds) {
		super(statusCode, contentType, headers, cookies, body, storeGzipped, timeToLiveSeconds)
	}

}
