package grails.plugin.springcache.provider.simple

import grails.plugin.springcache.CacheFacade

class SimpleCacheProviderTests extends GroovyTestCase {

	def testService
	def simpleCacheProvider

	void tearDown() {
		super.tearDown()
		testService.clearElements()
	}

	void testCacheableMethodUsesConfiguredCacheProvider() {
		assertEquals 0, testService.elements.size()

		CacheFacade cache = simpleCacheProvider.caches.testCache
		assertEquals "Cache should be primed after call to cacheable method", 1, cache.size
	}

	void testCacheFlushMethodUsesConfiguredCacheProvider() {
		assertEquals 0, testService.elements.size()

		CacheFacade cache = simpleCacheProvider.caches.testCache
		assertEquals "Cache should be primed after first call to cacheable method", 1, cache.size

		testService.addElement "FOO"

		assertEquals "Cache should be empty after flushing method call", 0, cache.size
		assertEquals "Uncached results should be returned after flush", 1, testService.elements.size()
		assertEquals "Cache should be re-populated", 1, cache.size
	}

}