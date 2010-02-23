package grails.plugin.springcache.web

import musicstore.pages.HomePage
import musicstore.Album
import musicstore.Artist
import net.sf.ehcache.Ehcache
import musicstore.pages.AlbumCreatePage

class IncludedContentTests extends AbstractContentCachingTestCase {

	Ehcache latestControllerCache

	void setUp() {
		super.setUp()

		Album.build(artist: Artist.build(name: "Edward Sharpe & the Magnetic Zeros"), name: "Up From Below", year: "2009")
		Album.build(artist: Artist.build(name: "Yeasayer"), name: "Odd Blood", year: "2010")
		Album.build(artist: Artist.build(name: "Yeah Yeah Yeahs"), name: "It's Blitz!", year: "2009")
	}

	void tearDown() {
		super.tearDown()

		Album.withTransaction {tx ->
			Album.list()*.delete()
			Artist.list()*.delete()
		}
	}

	void testIncludedContentIsCached() {
		def expectedList = ["It's Blitz! by Yeah Yeah Yeahs (2009)", "Odd Blood by Yeasayer (2010)", "Up From Below by Edward Sharpe & the Magnetic Zeros (2009)"]
		def page = HomePage.open()
		assertEquals expectedList, page.latestAlbums

		page = page.refresh()
		assertEquals expectedList, page.latestAlbums

		assertEquals 1, latestControllerCache.statistics.cacheMisses
		assertEquals 1, latestControllerCache.statistics.cacheHits
	}

	void testIncludedContentCanBeFlushedByAnotherController() {
		def expectedList = ["It's Blitz! by Yeah Yeah Yeahs (2009)", "Odd Blood by Yeasayer (2010)", "Up From Below by Edward Sharpe & the Magnetic Zeros (2009)"]
		assertEquals expectedList, HomePage.open().latestAlbums

		def createPage = AlbumCreatePage.open()
		createPage."artist.name" = "Mumford & Sons"
		createPage.name = "Sigh No More"
		createPage.year = "2009"
		createPage.save()

		expectedList.add(0, "Sigh No More by Mumford & Sons (2009)")
		assertEquals expectedList, HomePage.open().latestAlbums
	}

}