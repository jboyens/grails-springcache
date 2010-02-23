package musicstore.admin

import net.sf.ehcache.CacheManager

class CacheController {

	CacheManager springcacheCacheManager

	def index = {
		def cacheInstanceList = springcacheCacheManager.cacheNames.sort().collect {
			springcacheCacheManager.getEhcache(it)
		}
		[cacheInstanceList: cacheInstanceList]
	}

	def flush = {
		def cacheInstance = springcacheCacheManager.getEhcache(params.name)
		if (cacheInstance) {
			cacheInstance.flush()
			flash.message = "Cache '$cacheInstance.name' flushed."
		} else {
			flash.message = "Cache '$params.name' not found."
		}
		redirect action: "index"
	}

	def clear = {
		def cacheInstance = springcacheCacheManager.getEhcache(params.name)
		if (cacheInstance) {
			cacheInstance.clearStatistics()
			flash.message = "Statistics for '$cacheInstance.name' cleared."
		} else {
			flash.message = "Cache '$params.name' not found."
		}
		redirect action: "index"
	}
}
