package grails.plugin.springcache.provider.simple

import grails.plugin.springcache.annotations.Cacheable
import grails.plugin.springcache.annotations.CacheFlush

class TestService {

	private final List elements = []

	@Cacheable("testCache")
	List getElements() {
		elements.asImmutable()
	}

	@CacheFlush("testCache")
	void addElement(element) {
		elements << element
	}

	@CacheFlush("testCache")
	void clearElements() {
		elements.clear()
	}

}