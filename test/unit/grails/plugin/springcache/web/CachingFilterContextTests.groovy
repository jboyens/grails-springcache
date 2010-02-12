package grails.plugin.springcache.web

import org.springframework.web.context.request.RequestContextHolder
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.gmock.WithGMock
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsControllerClass
import org.codehaus.groovy.grails.commons.DefaultGrailsControllerClass

@WithGMock
class CachingFilterContextTests extends GroovyTestCase {

	void testControllerArtefactIsNullWhenNoControllerNamePresent() {
		RequestContextHolder.requestAttributes = mock(GrailsWebRequest) {
			getControllerName().returns(null)
			getActionName().returns(null)
		}

		play {
			def context = new CachingFilterContext()
			assertNull context.controllerArtefact
		}
	}

	void testControllerArtefactIsLookedUp() {
		RequestContextHolder.requestAttributes = mock(GrailsWebRequest) {
			getControllerName().returns("pirate")
			getActionName().returns(null)
		}
		def mockArtefact = new DefaultGrailsControllerClass(TestController)
		ApplicationHolder.application = mock(GrailsApplication) {
			getArtefactByLogicalPropertyName("Controller", "pirate").returns(mockArtefact)
		}

		play {
			def context = new CachingFilterContext()
			assertEquals mockArtefact, context.controllerArtefact
		}
	}

	void testActionClosureIsNullWhenNoControllerNamePresent() {
		RequestContextHolder.requestAttributes = mock(GrailsWebRequest) {
			getControllerName().returns(null)
			getActionName().returns(null)
		}

		play {
			def context = new CachingFilterContext()
			assertNull context.actionClosure
		}
	}

	void testActionClosureFoundOnControllerClass() {
		RequestContextHolder.requestAttributes = mock(GrailsWebRequest) {
			getControllerName().returns("pirate")
			getActionName().returns("list")
		}
		def mockArtefact = new DefaultGrailsControllerClass(TestController)
		ApplicationHolder.application = mock(GrailsApplication) {
			getArtefactByLogicalPropertyName("Controller", "pirate").returns(mockArtefact)
		}

		play {
			def context = new CachingFilterContext()
			assertNotNull context.actionClosure
		}
	}

	void testActionClosureTakenFromDefaultActionIfNoActionNamePresent() {
		RequestContextHolder.requestAttributes = mock(GrailsWebRequest) {
			getControllerName().returns("pirate")
			getActionName().returns(null)
		}
		def mockArtefact = new DefaultGrailsControllerClass(TestController)
		ApplicationHolder.application = mock(GrailsApplication) {
			getArtefactByLogicalPropertyName("Controller", "pirate").returns(mockArtefact)
		}

		play {
			def context = new CachingFilterContext()
			assertNotNull context.actionClosure
		}
	}

	void testActionClosureIsNullIfActionNotFoundOnControllerClass() {
		RequestContextHolder.requestAttributes = mock(GrailsWebRequest) {
			getControllerName().returns("pirate")
			getActionName().returns("scaffold")
		}
		def mockArtefact = new DefaultGrailsControllerClass(TestController)
		ApplicationHolder.application = mock(GrailsApplication) {
			getArtefactByLogicalPropertyName("Controller", "pirate").returns(mockArtefact)
		}

		play {
			def context = new CachingFilterContext()
			assertNull context.actionClosure
		}
	}
}

class TestController {
	def index = {}
	def list = {}
}