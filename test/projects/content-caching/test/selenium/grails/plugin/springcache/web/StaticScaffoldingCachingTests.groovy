package grails.plugin.springcache.web

import musicstore.Album
import musicstore.Artist
import musicstore.pages.AlbumCreatePage
import musicstore.pages.AlbumListPage
import musicstore.pages.AlbumShowPage
import net.sf.ehcache.Ehcache

class StaticScaffoldingCachingTests extends AbstractContentCachingTestCase {

	Ehcache albumControllerCache

	void tearDown() {
		Album.list()*.delete()
		Artist.list()*.delete()
		super.tearDown()
	}

	void testOpeningListPageWithEmptyCache() {
		AlbumListPage.open()

		assertEquals 0, albumControllerCache.statistics.cacheHits
		assertEquals 1, albumControllerCache.statistics.cacheMisses
	}

	void testReloadingListPageHitsCache() {
		def page = AlbumListPage.open()

		page.refresh()

		assertEquals 1, albumControllerCache.statistics.cacheHits
		assertEquals 1, albumControllerCache.statistics.objectCount
	}

	void testSaveFlushesCache() {
		def listPage = AlbumListPage.open()
		assertEquals 0, listPage.rowCount

		def createPage = AlbumCreatePage.open()
		createPage.artist = "Edward Sharpe & the Magnetic Zeros"
		createPage.name = "Up From Below"
		createPage.year = "2009"
		createPage.save()

		assertEquals "Album failed to save", 1, Album.count()

		listPage = AlbumListPage.open()
		assertEquals "Album list page is still displayed cached content", 1, listPage.rowCount

		assertEquals 0, albumControllerCache.statistics.cacheHits
		assertEquals 3, albumControllerCache.statistics.cacheMisses // 2 misses on list page, 1 on show
		assertEquals 2, albumControllerCache.statistics.objectCount // show and list pages cached
	}

	void testDifferentShowPagesCachedSeparately() {
		def artist = Artist.build(name: "Metric")
		def album1 = Album.build(artist: artist, name: "Fantasies", year: "2009")
		def album2 = Album.build(artist: artist, name: "Live It Out", year: "2005")

		def showPage1 = AlbumShowPage.open(album1.id)
		assertEquals album1.name, showPage1.Name

		def showPage2 = AlbumShowPage.open(album2.id)
		assertEquals album2.name, showPage2.Name

		assertEquals 0, albumControllerCache.statistics.cacheHits
		assertEquals 2, albumControllerCache.statistics.cacheMisses
		assertEquals 2, albumControllerCache.statistics.objectCount
	}

	void testNotFoundDoesNotGetCached() {
		def page = AlbumShowPage.openInvalidId(404)
		assertEquals "Album not found with id 404", page.flashMessage

		// cache size will be 1 as list page is returned
		assertEquals 1, albumControllerCache.statistics.objectCount
	}
}
