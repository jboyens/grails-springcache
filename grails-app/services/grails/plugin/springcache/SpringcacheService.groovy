package grails.plugin.springcache

import net.sf.ehcache.CacheManager
import net.sf.ehcache.Element
import net.sf.ehcache.Ehcache
import net.sf.ehcache.constructs.blocking.BlockingCache
import org.apache.commons.lang.ObjectUtils
import net.sf.ehcache.constructs.blocking.LockTimeoutException
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationContext
import grails.spring.BeanBuilder
import org.springframework.cache.ehcache.EhCacheFactoryBean

class SpringcacheService implements ApplicationContextAware {

	static transactional = false

	ApplicationContext applicationContext
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
				flushNamedCache(name)
			}
		}
	}

	/**
	 * Flushes all caches held by the service's cache manager.
	 * @param clearStatistics if true the method will clear all cache statistics as well as flushing.
	 */
	void flushAll(boolean clearStatistics = false) {
		springcacheCacheManager.cacheNames.each {
			flushNamedCache(it, clearStatistics)
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

	private BlockingCache getOrCreateBlockingCache(String name) {
		def cache = getOrCreateCache(name)
		if (cache instanceof BlockingCache) {
			return cache
		} else {
			log.warn "Cache '$name' is non-blocking. Decorating it now..."
			def blockingCache = new BlockingCache(cache)
			springcacheCacheManager.replaceCacheWithDecoratedCache(cache, blockingCache)
			return blockingCache
		}
	}

	private Ehcache getOrCreateCache(String name) {
		Ehcache cache = springcacheCacheManager.getEhcache(name)
		if (!cache) {
			if (autoCreateCaches) {
				log.warn "Cache '$name' does not exist. Creating it now..."
				cache = createNewCacheWithDefaults(name)
			} else {
				log.error "Cache '$name' does not exist."
				throw new NoSuchCacheException(name)
			}
		}
		return cache
	}

	private Ehcache createNewCacheWithDefaults(String name) {
		def beanBuilder = new BeanBuilder(applicationContext)
		beanBuilder.beans {
			"$name"(EhCacheFactoryBean) { bean ->
				bean.parent = ref("abstractCache", true)
				cacheName = name
			}
		}
		def ctx = beanBuilder.createApplicationContext()
		return ctx.getBean(name)
	}

	private def flushNamedCache(String name, boolean clearStatistics = false) {
		if (log.isDebugEnabled()) log.debug "Flushing cache '$name'"
		try {
			def cache = springcacheCacheManager.getEhcache(name)
			cache?.flush()
			if (clearStatistics) cache?.clearStatistics()
		} catch (IllegalStateException e) {
			log.warn "Attempted to flush cache '$name' when it is not alive"
		}
	}

}

class NoSuchCacheException extends RuntimeException {
	NoSuchCacheException(String cacheName) {
		super("No cache named '$cacheName' exists".toString())
	}
}
