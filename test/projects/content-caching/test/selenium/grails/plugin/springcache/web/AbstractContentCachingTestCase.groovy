package grails.plugin.springcache.web

import grails.plugins.selenium.SeleniumManager
import musicstore.pages.HomePage
import musicstore.pages.LoginPage
import net.sf.ehcache.CacheManager
import musicstore.auth.User
import musicstore.auth.Role
import org.grails.plugins.springsecurity.service.AuthenticateService

abstract class AbstractContentCachingTestCase extends GroovyTestCase {

	CacheManager springcacheCacheManager
	AuthenticateService authenticateService

	void tearDown() {
		super.tearDown()

		springcacheCacheManager.cacheNames.each {
			def cache = springcacheCacheManager.getEhcache(it)
			cache.flush()
			cache.clearStatistics()
		}
	}

	protected User setUpUser(username, userRealName) {
		User.withTransaction {tx ->
			def userRole = Role.findByAuthority("ROLE_USER")
			def user = new User(username: username, userRealName: userRealName, email: "$username@energizedwork.com", enabled: true)
			user.passwd = authenticateService.encodePassword("password")
			user.save(failOnError: true)

			userRole.addToPeople user
			userRole.save(failOnError: true)

			return user
		}
	}

	protected void tearDownUsers() {
		def userRole = Role.findByAuthority("ROLE_USER")
		User.withTransaction {tx ->
			User.list().each {
				userRole.removeFromPeople(it)
				it.delete()
			}
		}
	}

	HomePage loginAs(String username, String password = "password") {
		def loginPage = LoginPage.open()
		loginPage.j_username = username
		loginPage.j_password = password
		return loginPage.login()
	}

	HomePage logout() {
		 // TODO: better way to log out?
		SeleniumManager.instance.selenium.open "/logout"
		return new HomePage()
	}

}