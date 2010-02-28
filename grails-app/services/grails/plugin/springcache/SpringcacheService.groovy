package grails.plugin.springcache

import net.sf.ehcache.CacheManager
import net.sf.ehcache.Element

class SpringcacheService {

	static transactional = false

	CacheManager springcacheCacheManager
	boolean autoCreateCaches

	void flush(cacheNames) {
		if (cacheNames instanceof String) cacheNames = [cacheNames]
		def cachesToFlush = springcacheCacheManager.cacheNames.findAll { name ->
			cacheNames.any { name ==~ it }
		}
		if (log.isDebugEnabled()) log.debug "Flushing caches ${cachesToFlush.join(', ')}"
		cachesToFlush.each { name ->
			try {
				springcacheCacheManager.getEhcache(name)?.flush()
			} catch (IllegalStateException e) {
				log.warn "Attempted to flush cache $name when it is not alive"
			}
		}
	}

	void put(String cacheName, Serializable key, Object value) {
		def cache = springcacheCacheManager.getEhcache(cacheName)
		if (!cache) {
			if (autoCreateCaches) {
				springcacheCacheManager.addCache(cacheName)
				cache = springcacheCacheManager.getEhcache(cacheName)
			} else {
				throw new NoSuchCacheException(cacheName)
			}
		}
		cache.put(new Element(key, value))
	}

	def get(String cacheName, Serializable key) {
		def cache = springcacheCacheManager.getEhcache(cacheName)
		if (!cache) throw new NoSuchCacheException(cacheName)
		def element = cache.get(key)
		if (!element || element.isExpired()) {
			return null
		} else {
			return element.objectValue
		}
	}
}

class NoSuchCacheException extends RuntimeException {
	NoSuchCacheException(String cacheName) {
		super("No cache named '$cacheName' exists".toString())
	}
}
