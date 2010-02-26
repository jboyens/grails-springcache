package grails.plugin.springcache.web

import java.lang.reflect.Field
import javax.servlet.http.HttpServletRequest
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.GrailsControllerClass
import org.springframework.web.context.request.RequestContextHolder

class FilterContext {

	String controllerName
	String actionName
	Map params
	HttpServletRequest request

	FilterContext() {
		request = request = RequestContextHolder.requestAttributes?.request
		controllerName = RequestContextHolder.requestAttributes?.controllerName
		actionName = RequestContextHolder.requestAttributes?.actionName ?: controllerArtefact?.defaultAction
		params = RequestContextHolder.requestAttributes?.parameterMap?.asImmutable()
	}

	@Lazy GrailsControllerClass controllerArtefact = {
		controllerName ? ApplicationHolder.application.getArtefactByLogicalPropertyName("Controller", controllerName) : null
	}()

	@Lazy Field actionClosure = {
		try {
			return actionName ? controllerArtefact?.clazz?.getDeclaredField(actionName) : null
		} catch (NoSuchFieldException e) {
			// happens with dynamic scaffolded controllers
			return null
		}
	}()

	String toString() {
		def buffer = new StringBuilder("[")
		buffer << "controller=" << controllerName
		if (controllerArtefact == null) buffer << "?"
		buffer << ", action=" << actionName
		if (actionClosure == null) buffer << "?"
		buffer << "]"
		return buffer.toString()
	}

}