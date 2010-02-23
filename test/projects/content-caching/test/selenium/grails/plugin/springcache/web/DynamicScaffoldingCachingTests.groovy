package grails.plugin.springcache.web

import musicstore.pages.ArtistListPage
import net.sf.ehcache.Ehcache

class DynamicScaffoldingCachingTests extends AbstractContentCachingTestCase {

	Ehcache artistControllerCache

	void testCacheableAnnotationAtClassLevelIsRecognised() {
		def page = ArtistListPage.open()
		assertEquals "Artist List", page.title

		page = page.refresh()
		assertEquals "Artist List", page.title

		assertEquals 1, artistControllerCache.statistics.cacheHits
		assertEquals 1, artistControllerCache.statistics.cacheMisses
	}

}