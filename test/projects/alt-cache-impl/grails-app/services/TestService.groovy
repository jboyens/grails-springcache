import grails.plugins.springcache.annotations.Cacheable
import grails.plugins.springcache.annotations.CacheFlush

class TestService {

	private final List elements = []

	@Cacheable(model = "testCache")
	List getElements() {
		elements.asImmutable()
	}

	@CacheFlush(model = "testCache")
	void addElement(element) {
		elements << element
	}

	@CacheFlush(model = "testCache")
	void clearElements() {
		elements.clear()
	}

}