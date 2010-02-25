package grails.plugin.springcache.web

import net.sf.ehcache.*
import groovy.util.*
import functionaltestplugin.FunctionalTestCase
import musicstore.Album
import musicstore.Artist
import static javax.servlet.http.HttpServletResponse.*

class ContentNegotiationTests extends FunctionalTestCase {

	CacheManager springcacheCacheManager
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
		springcacheCacheManager.cacheNames.each {
			def cache = springcacheCacheManager.getEhcache(it)
			cache.flush()
			cache.clearStatistics()
		}
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