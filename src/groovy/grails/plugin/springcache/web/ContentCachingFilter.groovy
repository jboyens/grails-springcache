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

class ContentCachingFilter extends PageFragmentCachingFilter {

	private static final REQUEST_CACHE_ATTR = "${ContentCachingFilter.name}.CACHE"

	private final log = LoggerFactory.getLogger(getClass())
	CacheProvider cacheProvider

	@Override void doInit(FilterConfig filterConfig) {
		// don't do anything - we need to get caches differently for each request
	}

	protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
		def context = new CachingFilterContext()
		Cacheable cacheable = getAnnotation(context, Cacheable)
		if (cacheable) {
			log.debug "Using caching filter for $request.method:$request.requestURI $context"
			def cache = cacheProvider.getCache(cacheable.modelId()).wrappedCache
			setCache(request, cache)
			super.doFilter(request, response, chain)
		} else {
			log.debug "No cacheable annotation found for $request.method:$request.requestURI $context"
			chain.doFilter(request, response)
		}
	}

	protected PageInfo buildPageInfo(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
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

		return new PageInfo(wrapper.status, wrapper.contentType, wrapper.headers, wrapper.cookies, outstr.toByteArray(), storeGzipped, timeToLiveSeconds)
	}

	protected CacheManager getCacheManager() {
		throw new UnsupportedOperationException("Filter should not be calling getCacheManager")
	}

	protected String calculateKey(HttpServletRequest request) {
		def buffer = new StringBuilder()
		buffer << request.requestURI
		buffer << request.queryString
		return buffer.toString()
	}

	private BlockingCache getCache(HttpServletRequest request) {
		request.getAttribute(REQUEST_CACHE_ATTR)
	}

	private void setCache(HttpServletRequest request, BlockingCache cache) {
		request.setAttribute(REQUEST_CACHE_ATTR, cache)
	}

	private Annotation getAnnotation(CachingFilterContext context, Class type) {
		// TODO: cache this by controller/action
		def annotation = context.actionClosure?.getAnnotation(type)
		if (!annotation) {
			annotation = context.controllerArtefact?.clazz?.getAnnotation(type)
		}
		return annotation
	}
}
