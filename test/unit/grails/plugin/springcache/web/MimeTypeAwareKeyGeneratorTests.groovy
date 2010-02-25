package grails.plugin.springcache.web

import javax.servlet.http.HttpServletRequest
import org.gmock.WithGMock

@WithGMock
class MimeTypeAwareKeyGeneratorTests extends GroovyTestCase {

	KeyGenerator generator

	void setUp() {
		super.setUp()

		generator = new MimeTypeAwareKeyGenerator()
	}

	void testKeyVariesByMimeType() {
		def request = mock(HttpServletRequest) {
			getFormat().returns("html").times(2)
			getFormat().returns("xml")
		}
		play {
		def key1 = generator.generateKey(new CachingFilterContext(controllerName: "foo", actionName: "bar"), request)
		def key2 = generator.generateKey(new CachingFilterContext(controllerName: "foo", actionName: "bar"), request)
		def key3 = generator.generateKey(new CachingFilterContext(controllerName: "foo", actionName: "bar"), request)

		assertEquals key1, key2
		assertFalse key1 == key3
		}
	}

}
