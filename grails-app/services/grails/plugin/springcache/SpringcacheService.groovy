/*
 * Copyright 2010 Rob Fletcher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.springcache

import grails.spring.BeanBuilder
import net.sf.ehcache.CacheManager
import net.sf.ehcache.Ehcache
import net.sf.ehcache.Element
import net.sf.ehcache.constructs.blocking.BlockingCache
import net.sf.ehcache.constructs.blocking.LockTimeoutException
import org.springframework.cache.ehcache.EhCacheFactoryBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

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
	 */
	void flushAll() {
		springcacheCacheManager.cacheNames.each {
			flushNamedCache(it)
		}
	}

	/**
	 * Clears statistics for all caches held by the service's cache manager.
	 */
	void clearStatistics() {
		springcacheCacheManager.cacheNames.each {
			springcacheCacheManager.getEhcache(it)?.clearStatistics()
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
		if (cache instanceof BlockingCache) {
			// delegate so that we get the special exception handling for blocking caches
			return doWithBlockingCache(cacheName, key, closure)
		} else {
			return doWithCacheInternal(cache, key, closure)
		}
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

	/**
	 * Gets a named blocking cache instance from the cache manager. If the named cache is non-blocking it will be
	 * decorated and replaced. If no such cache exists it will be created.
	 * @param name The cache name.
	 * @return a BlockingCache instance.
	 */
	BlockingCache getOrCreateBlockingCache(String name) {
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

	/**
	 * Gets a named cache instance from the cache manager. If no such cache exists it will be created.
	 * @param name The cache name.
	 * @return a cache instance.
	 */
	Ehcache getOrCreateCache(String name) {
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

	private doWithCacheInternal(Ehcache cache, Serializable key, Closure closure) {
		def element = cache.get(key)
		if (!element || element.isExpired()) {
			if (log.isDebugEnabled()) log.debug "Cache '$cache.name' missed with key '$key'"
			def value = closure()
			element = new Element(key, value)
			cache.put(element)
		} else {
			if (log.isDebugEnabled()) log.debug "Cache '$cache.name' hit with key '$key'"
		}
		return element.objectValue
	}

	private Ehcache createNewCacheWithDefaults(String name) {
		def beanBuilder = new BeanBuilder(applicationContext)
		beanBuilder.beans {
			"$name"(EhCacheFactoryBean) { bean ->
				bean.parent = ref("springcacheDefaultCache", true)
				cacheName = name
			}
		}
		def ctx = beanBuilder.createApplicationContext()
		return ctx.getBean(name)
	}

	private def flushNamedCache(String name) {
		if (log.isDebugEnabled()) log.debug "Flushing cache '$name'"
		try {
			springcacheCacheManager.getEhcache(name)?.flush()
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
