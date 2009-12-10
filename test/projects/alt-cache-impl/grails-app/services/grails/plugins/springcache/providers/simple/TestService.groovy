package grails.plugins.springcache.providers.simple

import grails.plugins.springcache.annotations.Cacheable
import grails.plugins.springcache.annotations.CacheFlush

class TestService {

	private final List elements = []

	@Cacheable(modelId = "TestCachingModel")
	List getElements() {
		elements.asImmutable()
	}

	@CacheFlush(modelId = "TestFlushingModel")
	void addElement(element) {
		elements << element
	}

	@CacheFlush(modelId = "TestFlushingModel")
	void clearElements() {
		elements.clear()
	}

}