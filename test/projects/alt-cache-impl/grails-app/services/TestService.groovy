import grails.plugins.springcache.annotations.Cacheable
import grails.plugins.springcache.annotations.CacheFlush

class TestService {

	private final List elements = []

	@Cacheable(cacheName = "testCache")
	List getElements() {
		elements.asImmutable()
	}

	@CacheFlush(cacheNames = "testCache")
	void addElement(element) {
		elements << element
	}

	@CacheFlush(cacheNames = "testCache")
	void clearElements() {
		elements.clear()
	}

}