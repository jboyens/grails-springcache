package grails.plugins.springcache.aop

import grails.plugins.springcache.annotations.CacheFlush
import grails.plugins.springcache.cache.CacheFacade
import grails.plugins.springcache.cache.CacheProvider
import spock.lang.Specification
import grails.plugins.springcache.cache.FlushingModel

class FlushAspectSpecification extends Specification {

	void "All specfied caches are flushed"() {
		given: "some caches exist"
		def cache1 = Mock(CacheFacade)
		def cache2 = Mock(CacheFacade)
		def cache3 = Mock(CacheFacade)
		def flushingModel = Mock(FlushingModel)
		def cacheManager = Mock(CacheProvider)
		cacheManager.getCaches(flushingModel) >> [cache1, cache2]

		when: "the flush aspect is triggered"
		def annotation = [model: {-> "model1" }] as CacheFlush
		def aspect = new FlushAspect()
		aspect.cacheProvider = cacheManager
		aspect.models = [model1: flushingModel]
		aspect.flushCaches(annotation)

		then: "the specified caches are flushed"
		1 * cache1.flush()
		1 * cache2.flush()

		and: "any other caches are not flushed"
		0 * cache3.flush()
	}

}