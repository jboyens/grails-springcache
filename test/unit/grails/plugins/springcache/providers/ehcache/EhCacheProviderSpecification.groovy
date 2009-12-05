package grails.plugins.springcache.providers.ehcache

import grails.plugins.springcache.cache.CacheConfigurationException
import grails.plugins.springcache.cache.CacheNotFoundException
import grails.plugins.springcache.providers.ehcache.EhCacheCachingModel
import grails.plugins.springcache.providers.ehcache.EhCacheProvider
import net.sf.ehcache.CacheManager
import spock.lang.Specification

class EhCacheProviderSpecification extends Specification {

	void "Caching model configuration fails if required properties are missing"() {
		given: "An EHCache provider instance"
		def provider = new EhCacheProvider()

		when: "A caching model is added with invalid properties"
		provider.addCachingModel "modelId", new Properties()

		then: "An exception is thrown"
		thrown CacheConfigurationException
	}

	void "Flushing model configuration fails if required properties are missing"() {
		given: "An EHCache provider instance"
		def provider = new EhCacheProvider()

		when: "A caching model is added with invalid properties"
		provider.addFlushingModel "modelId", new Properties()

		then: "An exception is thrown"
		thrown CacheConfigurationException
	}

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