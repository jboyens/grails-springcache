package grails.plugins.springcache.implementations.ehcache

import grails.plugins.springcache.cache.CacheNotFoundException
import grails.plugins.springcache.implementations.ehcache.EhCacheProvider
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
		provider.getCache("cacheName")

		then: "An exception is thrown"
		thrown(CacheNotFoundException)
	}

}