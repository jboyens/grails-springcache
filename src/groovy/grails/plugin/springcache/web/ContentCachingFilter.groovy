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

class ContentCachingFilter extends GenericFilterBean {

	private final log = LoggerFactory.getLogger(getClass())

	private static final ALREADY_FILTERED_ATTR = "ContentCachingFilter.FILTERED"

	public final void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
		if (!(servletRequest instanceof HttpServletRequest) || !(servletResponse instanceof HttpServletResponse)) {
			throw new ServletException("OncePerRequestFilter just supports HTTP requests");
		}
		doFilterInternal(servletRequest, servletResponse, filterChain)
	}

	private void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
		def context = new CachingFilterContext()
		def annotation = getCacheableAnnotation(context)
		if (annotation) {
			log.debug "Using caching filter for $request.method:$request.requestURI $context"
		} else {
			log.debug "No cacheable annotation found for $request.method:$request.requestURI $context"
		}

		request.setAttribute(ALREADY_FILTERED_ATTR, Boolean.TRUE)
		filterChain.doFilter request, response
	}

	boolean isAlreadyFiltered(HttpServletRequest request) {
		return request.getAttribute(ALREADY_FILTERED_ATTR) != null
	}

	Cacheable getCacheableAnnotation(CachingFilterContext context) {
		Cacheable annotation = context.actionClosure?.getAnnotation(Cacheable)
		if (!annotation) {
			annotation = context.controllerArtefact?.clazz?.getAnnotation(Cacheable)
		}
		return annotation
	}
}
