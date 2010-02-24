package grails.plugin.springcache.web

import musicstore.pages.AlbumListPage
import musicstore.pages.HomePage
import net.sf.ehcache.Ehcache
import musicstore.pages.UserListPage

class AuthenticatedContentTests extends AbstractContentCachingTestCase {

	Ehcache albumControllerCache
	Ehcache userControllerCache

	void setUp() {
		super.setUp()
		setUpUser("blackbeard", "Edward Teach")
	}

	void tearDown() {
		logout()
		tearDownUsers()
		super.tearDown()
	}

	void testLoginOnUncachedPage() {
		def page = HomePage.open()
		assertFalse "User should not be logged in", page.isUserLoggedIn()

		page = loginAs("blackbeard")

		assertTrue "User should now be logged in", page.isUserLoggedIn()
		assertEquals "Logged in as blackbeard", page.loggedInMessage
	}

	void testLoginStateNotCachedWithPage() {
		def listPage = AlbumListPage.open()
		assertFalse "User should not be logged in", listPage.isUserLoggedIn()

		loginAs "blackbeard"

		listPage = AlbumListPage.open()
		assertEquals "Logged in as blackbeard", listPage.loggedInMessage

		assertEquals 1, albumControllerCache.statistics.cacheHits
	}

	void testCachingOfAuthenticatedAction() {
		def loginPage = UserListPage.openNotAuthenticated()
		assertEquals "Page should not be cached if status is 403", 0, userControllerCache.statistics.objectCount

		loginPage.j_username = "blackbeard"
		loginPage.j_password = "password"
		loginPage.login(UserListPage)
		
		assertEquals 1, userControllerCache.statistics.objectCount

		logout()
		UserListPage.openNotAuthenticated()
	}

}