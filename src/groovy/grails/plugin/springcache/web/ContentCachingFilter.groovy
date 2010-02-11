package grails.plugin.springcache.web

import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import net.sf.ehcache.CacheManager
import net.sf.ehcache.constructs.web.filter.SimplePageCachingFilter
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.support.WebApplicationContextUtils

class ContentCachingFilter extends SimplePageCachingFilter {

	private final log = LoggerFactory.getLogger(getClass())

	private CacheManager cacheManager

	void doInit(FilterConfig filterConfig) {
		cacheManager = applicationContext.getBean("springcacheCacheManager")
		super.doInit(filterConfig)
	}

//	@Override void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
//		if (controllerName) {
//			log.debug "Using caching filter for $controllerName"
//			super.doFilter request, response, chain
//		} else {
//			log.debug "Not caching $request.requestURI"
//			chain.doFilter request, response
//		}
//	}

	@Override CacheManager getCacheManager() {
		return cacheManager
	}

	@Override String getCacheName() {
		return super.getCacheName()
	}

	@Override boolean acceptsGzipEncoding(HttpServletRequest request) {
		return false
	}

	@Override String calculateKey(HttpServletRequest request) {
		def buffer = new StringBuffer()
		buffer << request.method
		buffer << request.requestURI
		return buffer as String
	}

	private ApplicationContext getApplicationContext() {
		return WebApplicationContextUtils.getWebApplicationContext(ServletContextHolder.servletContext)
	}

	private String getControllerName() {
		return RequestContextHolder.requestAttributes.controllerName
	}

	private String getActionName() {
		return RequestContextHolder.requestAttributes.actionName
	}
}
