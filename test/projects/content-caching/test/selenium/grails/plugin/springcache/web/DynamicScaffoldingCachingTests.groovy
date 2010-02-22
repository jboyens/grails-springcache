package grails.plugin.springcache.web

import musicstore.pages.ArtistListPage
import net.sf.ehcache.CacheManager
import net.sf.ehcache.Ehcache

class DynamicScaffoldingCachingTests extends GroovyTestCase {

	CacheManager springcacheCacheManager
	Ehcache artistControllerCache

	void setUp() {
		super.setUp()
		artistControllerCache = springcacheCacheManager.getEhcache("ArtistControllerCache")
		assert artistControllerCache, "Cache named ArtistControllerCache not found in $springcacheCacheManager.cacheNames"
	}

	void tearDown() {
		super.tearDown()

		springcacheCacheManager.cacheNames.each { cacheName ->
			def cache = springcacheCacheManager.getEhcache(cacheName)
			cache.flush()
			cache.clearStatistics()
		}
	}

	void testCacheableAnnotationAtClassLevelIsRecognised() {
		def page = ArtistListPage.open()
		assertEquals "Artist List", page.title

		page = page.refresh()
		assertEquals "Artist List", page.title

		assertEquals 1, artistControllerCache.statistics.cacheHits
		assertEquals 1, artistControllerCache.statistics.cacheMisses
	}

	void testCachedResponseIsDecoratedBySitemesh() {
		def page = ArtistListPage.open()
		assertTrue page.sitemeshDecorated

		page = page.refresh()
		assertTrue page.sitemeshDecorated

		assertEquals 1, artistControllerCache.statistics.cacheHits
	}
}