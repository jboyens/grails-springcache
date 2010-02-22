package grails.plugin.springcache.web

import grails.plugins.selenium.SeleniumManager
import musicstore.auth.Role
import musicstore.auth.User
import musicstore.pages.HomePage
import musicstore.pages.AlbumListPage
import net.sf.ehcache.CacheManager
import net.sf.ehcache.Ehcache
import musicstore.pages.LoginPage

class AuthenticatedContentTests extends GroovyTestCase {

	def authenticateService
	CacheManager springcacheCacheManager
	Ehcache albumControllerCache

	void setUp() {
		super.setUp()

		albumControllerCache = springcacheCacheManager.getEhcache("AlbumControllerCache")
		assert albumControllerCache, "Cache named AlbumControllerCache not found in $springcacheCacheManager.cacheNames"

		User.withTransaction {tx ->
			def userRole = Role.findByAuthority("ROLE_USER")
			def user = new User(username: "blackbeard", userRealName: "Edward Teach", email: "blackbeard@energizedwork.com", enabled: true)
			user.passwd = authenticateService.encodePassword("password")
			user.save(failOnError: true)

			userRole.addToPeople user
			userRole.save(failOnError: true)
		}
	}

	void tearDown() {
		super.tearDown()
		
		SeleniumManager.instance.selenium.open "/logout" // TODO: better way to log out?
		def userRole = Role.findByAuthority("ROLE_USER")
		User.withTransaction {tx ->
			User.list().each {
				userRole.removeFromPeople(it)
				it.delete()
			}
		}

		springcacheCacheManager.cacheNames.each { cacheName ->
			def cache = springcacheCacheManager.getEhcache(cacheName)
			cache.flush()
			cache.clearStatistics()
		}
	}

	void testLoginOnUncachedPage() {
		def page = HomePage.open()
		assertFalse "User should not be logged in", page.isUserLoggedIn()

		def loginPage = page.goToLogin()
		loginPage.j_username = "blackbeard"
		loginPage.j_password = "password"
		page = loginPage.login()

		assertTrue "User should now be logged in", page.isUserLoggedIn()
		assertEquals "Logged in as blackbeard", page.loggedInMessage
	}

	void testLoginStateNotCachedWithPage() {
		def listPage = AlbumListPage.open()
		assertFalse "User should not be logged in", listPage.isUserLoggedIn()

		def loginPage = LoginPage.open()
		loginPage.j_username = "blackbeard"
		loginPage.j_password = "password"
		loginPage.login()

		listPage = AlbumListPage.open()
		assertEquals "Logged in as blackbeard", listPage.loggedInMessage

		assertEquals 1, albumControllerCache.statistics.cacheHits
	}

}