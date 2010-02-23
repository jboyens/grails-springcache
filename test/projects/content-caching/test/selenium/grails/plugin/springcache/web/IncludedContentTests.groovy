package grails.plugin.springcache.web

import musicstore.pages.HomePage
import musicstore.Album
import musicstore.Artist
import net.sf.ehcache.Ehcache
import musicstore.pages.AlbumCreatePage
import org.grails.rateable.Rating
import org.grails.rateable.RatingLink
import musicstore.auth.User
import musicstore.pages.AlbumShowPage

class IncludedContentTests extends AbstractContentCachingTestCase {

	Ehcache latestControllerCache
	Ehcache popularControllerCache
	Album album1, album2, album3

	void setUp() {
		super.setUp()

		album1 = Album.build(artist: Artist.build(name: "Edward Sharpe & the Magnetic Zeros"), name: "Up From Below", year: "2009")
		album2 = Album.build(artist: Artist.build(name: "Yeasayer"), name: "Odd Blood", year: "2010")
		album3 = Album.build(artist: Artist.build(name: "Yeah Yeah Yeahs"), name: "It's Blitz!", year: "2009")
	}

	void tearDown() {
		super.tearDown()

		Album.withTransaction {tx ->
			RatingLink.list()*.delete()
			Rating.list()*.delete()
			Album.list()*.delete()
			Artist.list()*.delete()
		}
	}

	void testIncludedContentIsCached() {
		def expectedList = [album3, album2, album1].collect { it.toString() }
		def page = HomePage.open()
		assertEquals expectedList, page.latestAlbums

		page = page.refresh()
		assertEquals expectedList, page.latestAlbums

		assertEquals 1, latestControllerCache.statistics.cacheMisses
		assertEquals 1, latestControllerCache.statistics.cacheHits
	}

	void testIncludedContentCanBeFlushedByAnotherController() {
		def expectedList = [album3, album2, album1].collect { it.toString() }
		assertEquals expectedList, HomePage.open().latestAlbums

		def createPage = AlbumCreatePage.open()
		createPage."artist.name" = "Mumford & Sons"
		createPage.name = "Sigh No More"
		createPage.year = "2009"
		createPage.save()

		expectedList.add(0, "Sigh No More by Mumford & Sons (2009)")
		assertEquals expectedList, HomePage.open().latestAlbums
	}

	void testMultipleIncludesAreCachedSeparately() {
		def user = setUpUser("blackbeard", "Edward Teach")
		setUpAlbumRating(album1, user, 5.0)
		setUpAlbumRating(album2, user, 3.0)
		setUpAlbumRating(album3, user, 4.0)

		def expectedLatestList = [album3, album2, album1].collect { it.toString() }
		def expectedPopularList = [album1, album3, album2].collect { it.toString() }

		def page = HomePage.open()
		assertEquals expectedLatestList, page.latestAlbums
		assertEquals expectedPopularList, page.popularAlbums

		assertEquals 1, latestControllerCache.statistics.objectCount
		assertEquals 1, latestControllerCache.statistics.cacheMisses
		assertEquals 0, latestControllerCache.statistics.cacheHits
		assertEquals 1, popularControllerCache.statistics.objectCount
		assertEquals 1, popularControllerCache.statistics.cacheMisses
		assertEquals 0, popularControllerCache.statistics.cacheHits
	}

	void testIncludedContentFlushedByRateable() {
		setUpUser("ponytail", "Steven Segal")
		def user = setUpUser("roundhouse", "Chuck Norris")
		setUpAlbumRating(album1, user, 5.0)
		setUpAlbumRating(album2, user, 3.0)
		setUpAlbumRating(album3, user, 4.0)

		def expectedPopularList = [album1, album3, album2].collect { it.toString() }

		def homePage = loginAs("ponytail")
		assertEquals expectedPopularList, homePage.popularAlbums

		def showPage = AlbumShowPage.open(album1.id)
		showPage.vote 1

		homePage = HomePage.open()
		// TODO: not working because of goofy query in Rateable plugin - orders by num votes then avg rating!
//		assertEquals([album3, album2, album1], homePage.popularAlbums)

		assertEquals 2, popularControllerCache.statistics.cacheMisses
	}

}