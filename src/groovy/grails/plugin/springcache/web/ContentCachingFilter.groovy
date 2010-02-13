package grails.plugin.springcache.web

import grails.plugin.springcache.annotations.Cacheable
import grails.plugin.springcache.web.CachingFilterContext
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.web.filter.GenericFilterBean
import java.lang.annotation.Annotation
import net.sf.ehcache.CacheManager
import grails.plugin.springcache.CacheProvider
import grails.plugin.springcache.key.CacheKeyBuilder
import grails.plugin.springcache.CacheKey

class ContentCachingFilter extends GenericFilterBean {

	private final log = LoggerFactory.getLogger(getClass())
	CacheProvider cacheProvider

	public final void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
		if (!(servletRequest instanceof HttpServletRequest) || !(servletResponse instanceof HttpServletResponse)) {
			throw new ServletException("OncePerRequestFilter just supports HTTP requests");
		}
		doFilterInternal(servletRequest, servletResponse, filterChain)
	}

	private void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
		def context = new CachingFilterContext()
		Cacheable cacheable = getAnnotation(context, Cacheable)
		if (cacheable) {
			log.debug "Using caching filter for $request.method:$request.requestURI $context"
			def cache = cacheProvider.getCache(cacheable.modelId())
			def key = generateCacheKey(request)
			def cachedPage = cache.get(key)
			if (cachedPage) {
				// do something
			} else {
				filterChain.doFilter request, response
			}
		} else {
			log.debug "No cacheable annotation found for $request.method:$request.requestURI $context"
			filterChain.doFilter request, response
		}
	}

	Annotation getAnnotation(CachingFilterContext context, Class type) {
		def annotation = context.actionClosure?.getAnnotation(type)
		if (!annotation) {
			annotation = context.controllerArtefact?.clazz?.getAnnotation(type)
		}
		return annotation
	}

	private CacheKey generateCacheKey(HttpServletRequest request) {
		return new CacheKeyBuilder().append(request.requestURI).toCacheKey()
	}

}
