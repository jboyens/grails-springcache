package grails.plugin.springcache.web.key

import javax.servlet.http.HttpServletRequest
import org.gmock.WithGMock
import grails.plugin.springcache.web.key.MimeTypeAwareKeyGenerator
import grails.plugin.springcache.web.key.KeyGenerator
import grails.plugin.springcache.web.FilterContext

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
		def key1 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar"), request)
		def key2 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar"), request)
		def key3 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar"), request)

		assertEquals key1, key2
		assertFalse key1 == key3
		}
	}

}
