package grails.plugin.springcache

import net.sf.ehcache.CacheManager

class SpringcacheService {

	static transactional = false

	CacheManager springcacheCacheManager

	def flush(cacheNames) {
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
}
