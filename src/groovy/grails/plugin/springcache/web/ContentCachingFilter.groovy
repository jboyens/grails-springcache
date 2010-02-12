package grails.plugin.springcache.web

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.filter.GenericFilterBean
import org.apache.commons.collections.EnumerationUtils
import org.codehaus.groovy.grails.commons.ApplicationHolder
import java.lang.reflect.Field
import org.codehaus.groovy.grails.commons.GrailsControllerClass

class ContentCachingFilter extends GenericFilterBean {

	private final log = LoggerFactory.getLogger(getClass())

	private static final ALREADY_FILTERED_ATTR = "ContentCachingFilter.FILTERED"

	public final void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {

		if (!(servletRequest instanceof HttpServletRequest) || !(servletResponse instanceof HttpServletResponse)) {
			throw new ServletException("OncePerRequestFilter just supports HTTP requests");
		}
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;

		def context = new CachingFilterContext()

		log.debug "Using caching filter for $request.method:$request.requestURI"
		log.debug "    alreadyFiltered = ${isAlreadyFiltered(request)}"
		log.debug context.toString()

		request.setAttribute(ALREADY_FILTERED_ATTR, Boolean.TRUE)
		filterChain.doFilter request, response
	}

	boolean isAlreadyFiltered(HttpServletRequest request) {
		return request.getAttribute(ALREADY_FILTERED_ATTR) != null
	}

	private String getControllerName() {
		return RequestContextHolder.requestAttributes?.controllerName
	}

	private String getActionName() {
		return RequestContextHolder.requestAttributes?.actionName
	}

	private String getDefaultActionName() {
		return controllerArtefact?.defaultAction
	}

	private GrailsControllerClass getControllerArtefact() {
		def controllerClass = null
		if (controllerName) {
			controllerClass = ApplicationHolder.application.getArtefactByLogicalPropertyName("Controller", controllerName)
		}
		return controllerClass
	}

	private Field getActionClosure() {
		def action = null
		if (controllerArtefact) {
			def localActionName = actionName ?: defaultActionName
			if (localActionName) {
				try {
					action = controllerArtefact.clazz.getDeclaredField(localActionName)
				} catch (NoSuchFieldException e) {
					// dynamic scaffolding
				}
			}
		}
		return action
	}
}
