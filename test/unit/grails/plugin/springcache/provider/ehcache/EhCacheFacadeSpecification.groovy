package grails.plugin.springcache.provider.ehcache

import spock.lang.Specification
import net.sf.ehcache.*
import grails.plugin.springcache.CacheKey
import net.sf.ehcache.CacheManager

class EhCacheFacadeSpecification extends Specification {

	void "The facade should handle caching of a null value"() {
		given: "A cache with a facade"
		def cacheManager = new CacheManager()
		def cache = new Cache("SomeCache", 100, false, true, 0, 0)
		cacheManager.addCache(cache)
		def cacheFacade = new EhCacheFacade(cache)

		when: "A null value is put in the cache via the facade"
		def key = new CacheKey(0, 0L)
		cacheFacade.put key, null

		then: "The null value is cached"
		cacheFacade.size == 1
		cacheFacade.get(key) == null
	}
	
	void "The facade should know when a key has expired"() {
		given: "A cache with a facade"
		def cacheManager = new CacheManager()
		def cache = new Cache("SomeCache", 100, false, true, 0, 0)
		cacheManager.addCache(cache)
		def cacheFacade = new EhCacheFacade(cache)
		
		when: "A key exists in the cache but is expired"
		def key = new CacheKey(0, 0L)
		def element = new Element(key, "expired value")
		element.timeToLive = 0
		cache.put(element)
		while (!element.expired) {
			println "waiting..."
			Thread.sleep 100
		}
		println "ok element is expired"
		
		then: "The facade considers the key not present"
		cacheFacade.get(key) == null
	}

}