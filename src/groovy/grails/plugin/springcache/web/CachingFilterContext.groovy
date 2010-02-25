package grails.plugin.springcache.web

import java.lang.reflect.Field
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.GrailsControllerClass
import org.springframework.web.context.request.RequestContextHolder

class CachingFilterContext {

	String controllerName
	String actionName
	Map params
	private final GrailsControllerClass controllerArtefact
	private final Field actionClosure

	CachingFilterContext() {
		controllerName = RequestContextHolder.requestAttributes?.controllerName
		controllerArtefact = controllerName ? ApplicationHolder.application.getArtefactByLogicalPropertyName("Controller", controllerName) : null
		actionName = RequestContextHolder.requestAttributes?.actionName ?: controllerArtefact?.defaultAction
		try {
			actionClosure = actionName ? controllerArtefact?.clazz?.getDeclaredField(actionName) : null
		} catch (NoSuchFieldException e) {
			// happens with dynamic scaffolded controllers
		}
		params = RequestContextHolder.requestAttributes?.parameterMap?.asImmutable()
	}

	GrailsControllerClass getControllerArtefact() { controllerArtefact }
	Field getActionClosure() { actionClosure }

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