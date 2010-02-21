package grails.plugin.springcache.web

import grails.plugins.selenium.pageobjects.GrailsListPage
import net.sf.ehcache.CacheManager
import net.sf.ehcache.Ehcache
import grails.plugins.selenium.pageobjects.GrailsCreatePage
import musicstore.Album
import grails.plugins.selenium.pageobjects.GrailsShowPage
import musicstore.pages.AlbumListPage
import musicstore.pages.AlbumCreatePage

class StaticScaffoldingCachingTests extends GroovyTestCase {

	CacheManager springcacheCacheManager
	Ehcache albumControllerCache

	void setUp() {
		super.setUp()
		albumControllerCache = springcacheCacheManager.getEhcache("AlbumControllerCache")
		assert albumControllerCache, "Cache named AlbumControllerCache not found in $springcacheCacheManager.cacheNames"
	}

	void tearDown() {
		super.tearDown()
		albumControllerCache?.statistics?.clearStatistics()
		albumControllerCache?.removeAll()
	}

	void testOpeningListPageWithEmptyCache() {
		def page = AlbumListPage.open()
		assertEquals "Album List", page.title

		assertEquals 0, albumControllerCache.statistics.cacheHits
		assertEquals 1, albumControllerCache.statistics.cacheMisses
	}

	void testReloadingListPageHitsCache() {
		def page = AlbumListPage.open()
		assertEquals "Album List", page.title

		page = page.refresh()
		assertEquals "Album List", page.title

		assertEquals 1, albumControllerCache.statistics.cacheHits
	}

	void testCreateFlushesCache() {
		def listPage = AlbumListPage.open()
		assertEquals 0, listPage.rowCount

		def createPage = AlbumCreatePage.open()
		createPage."artist.name" = "Edward Sharpe & the Magnetic Zeros"
		createPage.name = "Up From Below"
		createPage.year = "2009"
		createPage.save()

		assertEquals "Album failed to save", 1, Album.count()

		listPage = AlbumListPage.open()
		assertEquals "Album list page is still displayed cached content", 1, listPage.rowCount

		assertEquals 0, albumControllerCache.statistics.cacheHits 
		assertEquals 2, albumControllerCache.statistics.cacheMisses
	}

}
