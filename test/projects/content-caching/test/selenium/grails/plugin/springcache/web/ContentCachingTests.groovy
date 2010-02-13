package grails.plugin.springcache.web

import grails.plugins.selenium.pageobjects.GrailsListPage
import net.sf.ehcache.CacheManager
import net.sf.ehcache.Cache
import net.sf.ehcache.Ehcache

class ContentCachingTests extends GroovyTestCase {

	CacheManager springcacheCacheManager
	Ehcache pirateControllerCache

	void setUp() {
		super.setUp()
		pirateControllerCache = springcacheCacheManager.getEhcache("PirateControllerCache")
		assert pirateControllerCache, "Cache named PirateControllerCache not found in $springcacheCacheManager.cacheNames"
	}

	void tearDown() {
		super.tearDown()
		pirateControllerCache?.statistics?.clearStatistics()
	}

	void testOpeningListPageWithEmptyCache() {
		def page = PirateListPage.open()
		assertEquals "Pirate List", page.title

		assertEquals 0, pirateControllerCache.statistics.cacheHits
		assertEquals 1, pirateControllerCache.statistics.cacheMisses
	}

}

class PirateListPage extends GrailsListPage {
	static GrailsListPage open() {
		def page = new PirateListPage()
		page.selenium.open "/pirate/list"
		return page
	}

	String getTitle() { selenium.title }
}