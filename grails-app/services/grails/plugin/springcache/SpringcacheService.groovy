package grails.plugin.springcache

import net.sf.ehcache.CacheManager
import net.sf.ehcache.Element
import net.sf.ehcache.Ehcache
import net.sf.ehcache.constructs.blocking.BlockingCache
import org.apache.commons.lang.ObjectUtils
import net.sf.ehcache.constructs.blocking.LockTimeoutException

class SpringcacheService {

	static transactional = false

	CacheManager springcacheCacheManager
	boolean autoCreateCaches = true // TODO: config?

	/**
	 * Flushes the specified cache or set of caches.
	 * @param cacheNamePatterns can be a single cache name or a regex pattern or a Collection/array of them.
	 */
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

	/**
	 * Calls a closure conditionally depending on whether a cache entry from a previous invocation exists. If the
	 * closure is called its return value is written to the cache..
	 * @param cacheName The name of the cache to use.
	 * @param key The key used to get and put cache entries.
	 * @param closure The closure to invoke if no cache entry exists already.
	 * @return The cached value if a cache entry exists or the return value of the closure otherwise.
	 */
	def doWithCache(String cacheName, Serializable key, Closure closure) {
		def cache = getOrCreateCache(cacheName)
		// TODO: needs to call doWithBlockingCache if it has found a blocking cache - just in case error is thrown
		return doWithCacheInternal(cache, key, closure)
	}

	/**
	 * A variant on doWithCache that guarantees the cache used will be a BlockingCache instance. The method handles
	 * clearing the lock if there is any exception raised.
	 * @param cacheName The name of the cache to use. If the named cache is not a BlockingCache instance it will be
	 *  decorated with one and replaced in the cache manager.
	 * @param key The key used to get and put cache entries.
	 * @param closure The closure to invoke if no cache entry exists already.
	 * @return The cached value if a cache entry exists or the return value of the closure otherwise.
	 */
	def doWithBlockingCache(String cacheName, Serializable key, Closure closure) {
		def cache = getOrCreateBlockingCache(cacheName)
		try {
			return doWithCacheInternal(cache, key, closure)
		} catch (LockTimeoutException e) {
			// do not release the lock as you never acquired it
			throw e
		} catch (Throwable t) {
			if (log.isDebugEnabled()) log.debug "Clearing lock on cache '$cache.name'"
			cache.put(new Element(key, null))
			throw t
		}
	}

	private doWithCacheInternal(Ehcache cache, Serializable key, Closure closure) {
		def element = cache.get(key)
		if (!element || element.isExpired()) {
			if (log.isDebugEnabled()) log.debug "Cache '$cache.name' missed with key '$key'"
			def value = closure()
			element = new Element(key, value == null ? ObjectUtils.NULL : value) // TODO: is this null handling really necessary?
			cache.put(element)
		} else {
			if (log.isDebugEnabled()) log.debug "Cache '$cache.name' hit with key '$key'"
		}
		return element.objectValue == ObjectUtils.NULL ? null : element.objectValue
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

	private BlockingCache getOrCreateBlockingCache(String cacheName) {
		def cache = getOrCreateCache(cacheName)
		if (cache instanceof BlockingCache) {
			return cache
		} else {
			log.warn "Cache '$cacheName' is non-blocking. Decorating it now..."
			def blockingCache = new BlockingCache(cache)
			springcacheCacheManager.replaceCacheWithDecoratedCache(cache, blockingCache)
			return blockingCache
		}
	}

}

class NoSuchCacheException extends RuntimeException {
	NoSuchCacheException(String cacheName) {
		super("No cache named '$cacheName' exists".toString())
	}
}
