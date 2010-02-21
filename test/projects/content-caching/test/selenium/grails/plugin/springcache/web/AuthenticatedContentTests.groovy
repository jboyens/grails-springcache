package grails.plugin.springcache.web

import musicstore.auth.Role
import musicstore.auth.User
import musicstore.pages.HomePage

class AuthenticatedContentTests extends GroovyTestCase {

	def authenticateService

	void setUp() {
		super.setUp()
		def userRole = Role.findByAuthority("ROLE_USER")
		def user = new User(username: "blackbeard", userRealName: "Edward Teach", email: "blackbeard@energizedwork.com")
		user.passwd = authenticateService.encodePassword("password")
		user.authorities = [userRole] as Set
		user.save(failOnError: true)
	}

	void tearDown() {
		super.tearDown()
		def userRole = Role.findByAuthority("ROLE_USER")
		User.list().each {
			userRole.removeFromPeople(it)
			it.delete()
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

}