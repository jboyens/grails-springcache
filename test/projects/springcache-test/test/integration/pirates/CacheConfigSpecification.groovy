package pirates

import grails.plugin.spock.IntegrationSpecification
import net.sf.ehcache.store.MemoryStoreEvictionPolicy

class CacheConfigSpecification extends IntegrationSpecification {
	
	def springcacheCacheManager
	
	void "Caches can be configured in app config"() {
		when: "A cache that is configured in app config is retrieved from the cache manager"
		def cache = springcacheCacheManager.getEhcache("ConfiguredCache")
		
		then: "The cache should exist"
		cache != null
		
		and: "The configuration should be correct"
		cache.cacheConfiguration.timeToLiveSeconds == 86400
	}
	
	void "Cache defaults can be configured in app config"() {
		when: "A cache that is configured in app config is retrieved from the cache manager"
		def cache = springcacheCacheManager.getEhcache("ConfiguredCache")

		then: "The cache should exist"
		cache != null

		and: "Default properties should be correct"
		cache.cacheConfiguration.memoryStoreEvictionPolicy == MemoryStoreEvictionPolicy.LFU
	}

}