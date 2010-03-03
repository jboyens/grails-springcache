package grails.plugin.springcache.web

import grails.plugin.springcache.SpringcacheService
import grails.plugins.selenium.SeleniumManager
import musicstore.Album
import musicstore.auth.Role
import musicstore.auth.User
import musicstore.pages.HomePage
import musicstore.pages.LoginPage
import org.grails.plugins.springsecurity.service.AuthenticateService
import org.grails.rateable.Rating
import org.grails.rateable.RatingLink

abstract class AbstractContentCachingTestCase extends GroovyTestCase {

	SpringcacheService springcacheService
	AuthenticateService authenticateService

	void tearDown() {
		super.tearDown()
		springcacheService.flushAll()
		springcacheService.clearStatistics()
	}

	protected void setUpAlbumRating(Album album, User rater, double stars) {
		def rating = new Rating(stars: stars, raterId: rater.id, raterClass: User.name)
		rating.save(failOnError: true)
		def link = new RatingLink(rating: rating, ratingRef: album.id, type: "album")
		link.save(failOnError: true)
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
			}
		}
	}

	HomePage loginAs(String username, String password = "password") {
		def loginPage = LoginPage.open()
		loginPage.j_username = username
		loginPage.j_password = password
		return loginPage.login()
	}

	void logout() {
		 // TODO: better way to log out?
		SeleniumManager.instance.selenium.open "/logout"
	}

}