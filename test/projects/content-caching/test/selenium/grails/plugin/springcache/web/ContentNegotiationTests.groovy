package grails.plugin.springcache.web

import musicstore.pages.HomePage
import net.sf.ehcache.*
import groovy.util.*

class ContentNegotiationTests extends AbstractContentCachingTestCase {

	Ehcache latestControllerCache

	void testCachedContentNotServedWhenAcceptHeaderIsDifferent() {
		def page = HomePage.open()
		assertEquals 1, latestControllerCache.statistics.cacheMisses
		assertEquals 1, latestControllerCache.statistics.objectCount

//		def http = new HTTPBuilder("http://localhost:8080")
//		http.request(GET, XML) {
//			uri.path = "/latest/albums"
//			response.success = { resp, xml ->
//				XMLSlurper.parseText(xml)
//			}
//			response.failure = { resp ->
//				fail "Unexpected error: $resp.statusLine.statusCode : $resp.statusLine.reasonPhrase"
//			}
//		}
	}

}