package grails.plugin.springcache.web

import grails.plugins.selenium.pageobjects.GrailsListPage
import net.sf.ehcache.CacheManager
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
		pirateControllerCache?.removeAll()
	}

	void testOpeningListPageWithEmptyCache() {
		def page = PirateListPage.open()
		assertEquals "Pirate List", page.title

		assertEquals 0, pirateControllerCache.statistics.cacheHits
		assertEquals 1, pirateControllerCache.statistics.cacheMisses
	}

	void testReloadingListPageHitsCache() {
		def page = PirateListPage.open()
		assertEquals "Pirate List", page.title

		page = page.refresh()
		assertEquals "Pirate List", page.title

		assertEquals 1, pirateControllerCache.statistics.cacheHits
	}

}

class PirateListPage extends GrailsListPage {
	static PirateListPage open() {
		def page = new PirateListPage()
		page.selenium.open "/pirate/list"
		return page
	}

	PirateListPage refresh() {
		selenium.refreshAndWait()
		return new PirateListPage()
	}

	String getTitle() { selenium.title }
}