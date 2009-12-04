package grails.plugins.springcache.providers.ehcache

import grails.plugins.springcache.cache.CacheNotFoundException
import net.sf.ehcache.CacheManager
import spock.lang.Specification

class EhCacheProviderSpecification extends Specification {

	void "Provider throws exception if named cache does not exist"() {
		given: "An EHCache provider instance exists"
		def cacheManager = Mock(CacheManager)
		cacheManager.cacheExists("cacheName") >> false

		when: "The provider tries to find a cache that does not exist"
		def provider = new EhCacheProvider()
		provider.cacheManager = cacheManager
		provider.getCache(new EhCacheCachingModel("cacheName"))

		then: "An exception is thrown"
		thrown(CacheNotFoundException)
	}

}