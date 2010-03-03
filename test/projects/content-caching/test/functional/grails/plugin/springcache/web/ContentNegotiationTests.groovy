package grails.plugin.springcache.web

import functionaltestplugin.FunctionalTestCase
import grails.plugin.springcache.SpringcacheService
import musicstore.Album
import musicstore.Artist
import net.sf.ehcache.Ehcache
import static javax.servlet.http.HttpServletResponse.SC_OK

class ContentNegotiationTests extends FunctionalTestCase {

	SpringcacheService springcacheService
	Ehcache latestControllerCache

	void setUp() {
		super.setUp()

		baseURL = "http://localhost:8080"

		Album.withTransaction { tx ->
			def artist = Artist.build(name: "The Cure")
			Album.build(artist: artist, name: "Pornography", year: "1982")
		}
	}

	void tearDown() {
		super.tearDown()

		Album.withTransaction {tx ->
			Album.list()*.delete()
			Artist.list()*.delete()
		}
		springcacheService.flushAll()
		springcacheService.clearStatistics()
	}

	void testCachedContentNotServedWhenAcceptHeaderIsDifferent() {
		get "/"
		assertStatus SC_OK
		assertContentType "text/html"
		assertEquals 0, latestControllerCache.statistics.cacheHits
		assertEquals 1, latestControllerCache.statistics.cacheMisses
		assertEquals 1, latestControllerCache.statistics.objectCount

		get("/latest/albums") {
			headers["Accept"] = "text/xml"
		}
		assertStatus SC_OK
		assertContentType "text/xml"
		assertEquals 0, latestControllerCache.statistics.cacheHits
		assertEquals 2, latestControllerCache.statistics.cacheMisses
		assertEquals 2, latestControllerCache.statistics.objectCount
	}

}