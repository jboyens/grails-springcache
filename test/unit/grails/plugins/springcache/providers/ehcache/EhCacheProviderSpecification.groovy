package grails.plugins.springcache.providers.ehcache

import grails.plugins.springcache.cache.CacheNotFoundException
import net.sf.ehcache.CacheManager
import spock.lang.Specification

class EhCacheProviderSpecification extends Specification {

	void "Provider throws exception if named cache does not exist"() {
		given: "An EHCache provider instance exists"
		def cacheManager = Mock(CacheManager)
		cacheManager.cacheExists("cacheName") >> false
		def provider = new EhCacheProvider()
		provider.cacheManager = cacheManager

		and: "A caching model is configured"
		provider.addCachingModel "modelId", new EhCacheCachingModel("cacheName")

		when: "The provider tries to find a cache that does not exist"
		provider.getCache "modelId"

		then: "An exception is thrown"
		thrown CacheNotFoundException
	}

}