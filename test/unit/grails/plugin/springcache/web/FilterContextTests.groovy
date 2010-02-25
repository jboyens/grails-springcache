package grails.plugin.springcache.web

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.DefaultGrailsControllerClass
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.gmock.WithGMock
import org.springframework.web.context.request.RequestContextHolder

@WithGMock
class FilterContextTests extends GroovyTestCase {

	void setUp() {
		super.setUp()
		
		def mockArtefact = new DefaultGrailsControllerClass(TestController)
		def mockApplication = mock(GrailsApplication) {
			getArtefactByLogicalPropertyName("Controller", "test").returns(mockArtefact).stub()
		}
		mock(ApplicationHolder).static.application.returns(mockApplication).stub()
	}

	void testControllerArtefactIsNullWhenNoControllerNamePresent() {
		def mockRequest = mock(GrailsWebRequest) {
			getControllerName().returns(null)
			getActionName().returns(null)
			getParameterMap().returns(null)
		}
		mock(RequestContextHolder).static.requestAttributes.returns(mockRequest).stub()

		play {
			def context = new FilterContext()
			assertNull context.controllerArtefact
		}
	}

	void testControllerArtefactIsLookedUp() {
		def mockRequest = mock(GrailsWebRequest) {
			getControllerName().returns("test")
			getActionName().returns(null)
			getParameterMap().returns(null)
		}
		mock(RequestContextHolder).static.requestAttributes.returns(mockRequest).stub()

		play {
			def context = new FilterContext()
			assertEquals TestController, context.controllerArtefact.clazz
		}
	}

	void testActionClosureIsNullWhenNoControllerNamePresent() {
		def mockRequest = mock(GrailsWebRequest) {
			getControllerName().returns(null)
			getActionName().returns(null)
			getParameterMap().returns(null)
		}
		mock(RequestContextHolder).static.requestAttributes.returns(mockRequest).stub()

		play {
			def context = new FilterContext()
			assertNull context.actionClosure
		}
	}

	void testActionClosureFoundOnControllerClass() {
		def mockRequest = mock(GrailsWebRequest) {
			getControllerName().returns("test")
			getActionName().returns("list")
			getParameterMap().returns(null)
		}
		mock(RequestContextHolder).static.requestAttributes.returns(mockRequest).stub()

		play {
			def context = new FilterContext()
			assertNotNull context.actionClosure
		}
	}

	void testActionClosureTakenFromDefaultActionIfNoActionNamePresent() {
		def mockRequest = mock(GrailsWebRequest) {
			getControllerName().returns("test")
			getActionName().returns(null)
			getParameterMap().returns(null)
		}
		mock(RequestContextHolder).static.requestAttributes.returns(mockRequest).stub()

		play {
			def context = new FilterContext()
			assertNotNull context.actionClosure
		}
	}

	void testActionClosureIsNullIfActionNotFoundOnControllerClass() {
		def mockRequest = mock(GrailsWebRequest) {
			getControllerName().returns("test")
			getActionName().returns("scaffold")
			getParameterMap().returns(null)
		}
		mock(RequestContextHolder).static.requestAttributes.returns(mockRequest).stub()

		play {
			def context = new FilterContext()
			assertNull context.actionClosure
		}
	}
}

class TestController {
	def index = {}
	def list = {}
}