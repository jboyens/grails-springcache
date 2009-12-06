package grails.plugins.springcache.providers.simple

import grails.plugins.springcache.annotations.Cacheable
import grails.plugins.springcache.annotations.CacheFlush

class TestService {

	private final List elements = []

	@Cacheable(model = "TestCachingModel")
	List getElements() {
		elements.asImmutable()
	}

	@CacheFlush(model = "TestFlushingModel")
	void addElement(element) {
		elements << element
	}

	@CacheFlush(model = "TestFlushingModel")
	void clearElements() {
		elements.clear()
	}

}