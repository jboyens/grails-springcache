package pirates

import grails.plugin.spock.IntegrationSpecification

class CacheConfigSpecification extends IntegrationSpecification {
	
	def springcacheCacheManager
	
	void "Caches can be configured in app config"() {
		when: "A cache that is configured in app config is retrieved from the cache manager"
		def cache = springcacheCacheManager.getCache("ConfiguredCache")
		
		then: "The cache should exist"
		cache != null
		
		and: "The configuration should be correct"
		cache.cacheConfiguration.eternal
	}
	
}