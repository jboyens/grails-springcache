package grails.plugin.springcache.providers.simple

import grails.plugin.springcache.annotations.Cacheable
import grails.plugin.springcache.annotations.CacheFlush

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