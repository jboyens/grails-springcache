package grails.plugin.springcache.web

class DefaultKeyGeneratorTests extends GroovyTestCase {

	KeyGenerator generator

	void setUp() {
		super.setUp()

		generator = new DefaultKeyGenerator()
	}

	void testKeyVariesOnControllerName() {
		def key1 = generator.generateKey(new CachingFilterContext(controllerName: "foo", actionName: "bar"), null)
		def key2 = generator.generateKey(new CachingFilterContext(controllerName: "foo", actionName: "bar"), null)
		def key3 = generator.generateKey(new CachingFilterContext(controllerName: "baz", actionName: "bar"), null)

		assertEquals key1, key2
		assertFalse key1 == key3
	}

	void testKeyVariesOnActionName() {
		def key1 = generator.generateKey(new CachingFilterContext(controllerName: "foo", actionName: "bar"), null)
		def key2 = generator.generateKey(new CachingFilterContext(controllerName: "foo", actionName: "bar"), null)
		def key3 = generator.generateKey(new CachingFilterContext(controllerName: "foo", actionName: "baz"), null)

		assertEquals key1, key2
		assertFalse key1 == key3
	}

	void testKeyVariesWithParams() {
		def key1 = generator.generateKey(new CachingFilterContext(controllerName: "foo", actionName: "bar", params: [:]), null)
		def key2 = generator.generateKey(new CachingFilterContext(controllerName: "foo", actionName: "bar", params: [:]), null)
		def key3 = generator.generateKey(new CachingFilterContext(controllerName: "foo", actionName: "bar", params: [id: "1"]), null)
		def key4 = generator.generateKey(new CachingFilterContext(controllerName: "foo", actionName: "baz", params: [id: "2"]), null)
		def key5 = generator.generateKey(new CachingFilterContext(controllerName: "foo", actionName: "baz", params: [id: "2"]), null)

		assertEquals key1, key2
		assertFalse key1 == key3
		assertFalse key3 == key4
		assertEquals key4, key5
	}

	void testParameterOrderNotImportant() {
		def key1 = generator.generateKey(new CachingFilterContext(controllerName: "foo", actionName: "bar", params: [id: "1", foo: "bar"]), null)
		def key2 = generator.generateKey(new CachingFilterContext(controllerName: "foo", actionName: "bar", params: [foo: "bar", id: "1"]), null)

		assertEquals key1, key2
	}

	void testMatchingSubsetOfParamsCreatesDifferentKey() {
		def key1 = generator.generateKey(new CachingFilterContext(controllerName: "foo", actionName: "bar", params: [id: "1"]), null)
		def key2 = generator.generateKey(new CachingFilterContext(controllerName: "foo", actionName: "bar", params: [id: "1", foo: "bar"]), null)

		assertFalse key1 == key2
	}

	void testControllerAndActionParamsAreIgnored() {
		def key1 = generator.generateKey(new CachingFilterContext(controllerName: "foo", actionName: "bar", params: [:]), null)
		def key2 = generator.generateKey(new CachingFilterContext(controllerName: "foo", actionName: "bar", params: [controller: "foo", action: "bar"]), null)

		assertEquals key1, key2
	}

}
