package grails.plugin.springcache.provider.ehcache

import spock.lang.Specification
import net.sf.ehcache.Cache
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
		cacheFacade.containsKey(key)
		cacheFacade.get(key) == null
	}

}