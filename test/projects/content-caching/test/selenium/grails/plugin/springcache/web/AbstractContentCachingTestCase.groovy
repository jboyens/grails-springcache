package grails.plugin.springcache.web

import net.sf.ehcache.CacheManager

abstract class AbstractContentCachingTestCase extends GroovyTestCase {

	CacheManager springcacheCacheManager

	void tearDown() {
		super.tearDown()

		springcacheCacheManager.cacheNames.each {
			def cache = springcacheCacheManager.getEhcache(it)
			cache.flush()
			cache.clearStatistics()
		}
	}

}