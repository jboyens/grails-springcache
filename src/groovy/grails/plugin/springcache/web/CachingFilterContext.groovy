package grails.plugin.springcache.web

import org.codehaus.groovy.grails.commons.GrailsControllerClass
import java.lang.reflect.Field
import org.springframework.web.context.request.RequestContextHolder
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.apache.commons.lang.builder.ToStringBuilder

class CachingFilterContext {

	private final String controllerName
	private final String actionName
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
	}

	GrailsControllerClass getControllerArtefact() { controllerArtefact }
	Field getActionClosure() { actionClosure }
	String getControllerName() { controllerName }
	String getActionName() { actionName }

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