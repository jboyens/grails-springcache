package springcache.test

class StatsController {

	def cacheManager

	def index = {
		render {
			html {
				head {
					title "EHCache Statistics"
				}
				body {
					table {
						cacheManager.cacheNames.each {cacheName ->
							def stats = cacheManager.getCache(cacheName).statistics
							tr {
								th colspan: 2, cacheName
							}
							tr {
								th "Hits:"
								td stats.cacheHits
							}
							tr {
								th "Misses:"
								td stats.cacheMisses
							}
						}
					}
				}
			}
		}
	}
}