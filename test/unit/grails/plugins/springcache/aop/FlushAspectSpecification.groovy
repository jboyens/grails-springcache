package grails.plugins.springcache.aop

import grails.plugins.springcache.annotations.CacheFlush
import grails.plugins.springcache.cache.CacheFacade
import grails.plugins.springcache.cache.CacheProvider
import spock.lang.Specification

class FlushAspectSpecification extends Specification {

	void "All specfied caches are flushed"() {
		given: "some caches exist"
		def cache1 = Mock(CacheFacade)
		def cache2 = Mock(CacheFacade)
		def cache3 = Mock(CacheFacade)
		def cacheManager = Mock(CacheProvider)
		cacheManager.getCaches("modelId") >> [cache1, cache2]

		when: "the flush aspect is triggered"
		def annotation = [model: {-> "modelId" }] as CacheFlush
		def aspect = new FlushAspect()
		aspect.cacheProvider = cacheManager
		aspect.flushCaches(annotation)

		then: "the specified caches are flushed"
		1 * cache1.flush()
		1 * cache2.flush()

		and: "any other caches are not flushed"
		0 * cache3.flush()
	}

}