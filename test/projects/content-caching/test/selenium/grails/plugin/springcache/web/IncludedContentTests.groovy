package grails.plugin.springcache.web

import musicstore.pages.HomePage
import musicstore.Album
import musicstore.Artist
import net.sf.ehcache.Ehcache

class IncludedContentTests extends AbstractContentCachingTestCase {

	Ehcache latestControllerCache

	void tearDown() {
		super.tearDown()

		Album.withTransaction {tx ->
			Album.list()*.delete()
			Artist.list()*.delete()
		}
	}

	void testIncludedContentIsCached() {
		Album.build(artist: Artist.build(name: "Edward Sharpe & the Magnetic Zeros"), name: "Up From Below", year: "2009")
		Album.build(artist: Artist.build(name: "Yeasayer"), name: "Odd Blood", year: "2010")
		Album.build(artist: Artist.build(name: "Yeah Yeah Yeahs"), name: "It's Blitz!", year: "2009")

		def expectedList = ["It's Blitz! by Yeah Yeah Yeahs", "Odd Blood by Yeasayer", "Up From Below by Edward Sharpe & the Magnetic Zeros"]

		def page = HomePage.open()
		assertEquals expectedList, page.latestAlbums

		page = page.refresh()
		assertEquals expectedList, page.latestAlbums

		assertEquals 1, latestControllerCache.statistics.cacheMisses
		assertEquals 1, latestControllerCache.statistics.cacheHits
	}

}