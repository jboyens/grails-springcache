package grails.plugin.springcache.web

import grails.plugins.selenium.SeleniumManager
import musicstore.pages.HomePage
import musicstore.pages.LoginPage
import net.sf.ehcache.CacheManager

abstract class AbstractContentCachingTestCase extends GroovyTestCase {

	CacheManager springcacheCacheManager

	void tearDown() {
		super.tearDown()

		springcacheCacheManager.cacheNames.each {
			def cache = springcacheCacheManager.getEhcache(it)
			cache.flush()
			cache.clearStatistics()
		}
	}

	HomePage loginAs(String username, String password = "password") {
		HomePage page
		def loginPage = LoginPage.open()
		loginPage.j_username = username
		loginPage.j_password = password
		page = loginPage.login()
		return page
	}

	HomePage logout() {
		 // TODO: better way to log out?
		SeleniumManager.instance.selenium.open "/logout"
		return new HomePage()
	}

}