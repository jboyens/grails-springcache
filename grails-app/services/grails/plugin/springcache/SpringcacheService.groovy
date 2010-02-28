package grails.plugin.springcache

import net.sf.ehcache.CacheManager
import net.sf.ehcache.Element
import net.sf.ehcache.Ehcache
import net.sf.ehcache.constructs.blocking.BlockingCache

class SpringcacheService {

	static transactional = false

	CacheManager springcacheCacheManager
	boolean autoCreateCaches = true // TODO: config?

	void flush(cacheNamePatterns) {
		if (cacheNamePatterns instanceof String) cacheNamePatterns = [cacheNamePatterns]
		springcacheCacheManager.cacheNames.each { name ->
			if (cacheNamePatterns.any { name ==~ it }) {
				if (log.isDebugEnabled()) log.debug "Flushing cache '$name'"
				try {
					springcacheCacheManager.getEhcache(name)?.flush()
				} catch (IllegalStateException e) {
					log.warn "Attempted to flush cache '$name' when it is not alive"
				}
			}
		}
	}

	void put(String cacheName, Serializable key, Object value) {
		def cache = getOrCreateCache(cacheName)
		cache.put(new Element(key, value))
	}

	def get(String cacheName, Serializable key) {
		def cache = getOrCreateCache(cacheName)
		def element = cache.get(key)
		if (!element || element.isExpired()) {
			return null
		} else {
			return element.objectValue
		}
	}

	void ensureCacheIsBlocking(String cacheName) {
		def cache = getOrCreateCache(cacheName)
		if (!(cache instanceof BlockingCache)) {
			log.warn "Cache '$cacheName' is not blocking. Decorating it now..."
			def blockingCache = new BlockingCache(cache)
			springcacheCacheManager.replaceCacheWithDecoratedCache(cache, blockingCache)
		}
	}

	private Ehcache getOrCreateCache(String cacheName) {
		def cache = springcacheCacheManager.getEhcache(cacheName)
		if (!cache) {
			if (autoCreateCaches) {
				log.warn "Cache '$cacheName' does not exist. Creating it now..."
				springcacheCacheManager.addCache(cacheName)
				cache = springcacheCacheManager.getEhcache(cacheName)
			} else {
				throw new NoSuchCacheException(cacheName)
			}
		}
		return cache
	}
}

class NoSuchCacheException extends RuntimeException {
	NoSuchCacheException(String cacheName) {
		super("No cache named '$cacheName' exists".toString())
	}
}
