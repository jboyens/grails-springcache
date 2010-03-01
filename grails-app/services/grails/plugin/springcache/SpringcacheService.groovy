package grails.plugin.springcache

import net.sf.ehcache.CacheManager
import net.sf.ehcache.Element
import net.sf.ehcache.Ehcache
import net.sf.ehcache.constructs.blocking.BlockingCache
import org.apache.commons.lang.ObjectUtils

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

	def withCache(String cacheName, Serializable key, Closure closure) {
		def cache = getOrCreateCache(cacheName)
		def element = cache.get(key)
		if (!element || element.isExpired()) {
			if (log.isDebugEnabled()) log.debug "Cache '$cacheName' missed with key '$key'"
			def value = closure()
			element = new Element(key, value == null ? ObjectUtils.NULL : value) // TODO: is this null handling really necessary?
			cache.put(element)
		} else {
			if (log.isDebugEnabled()) log.debug "Cache '$cacheName' hit with key '$key'"
		}
		return element.objectValue == ObjectUtils.NULL ? null : element.objectValue
	}

	def withBlockingCache(String cacheName, Serializable key, Closure closure) {
		ensureCacheIsBlocking cacheName
		return withCache(cacheName, key, closure)
		// TODO: handle errors and unlocking cache
	}

	private void ensureCacheIsBlocking(String cacheName) {
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
				springcacheCacheManager.addCache(cacheName) // TODO: configurable defaults?
				cache = springcacheCacheManager.getEhcache(cacheName)
			} else {
				log.error "Cache '$cacheName' does not exist."
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
