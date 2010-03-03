/*
 * Copyright 2010 Rob Fletcher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
			request.returns(null)
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
			request.returns(null)
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
			request.returns(null)
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
			request.returns(null)
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
			request.returns(null)
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
			request.returns(null)
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