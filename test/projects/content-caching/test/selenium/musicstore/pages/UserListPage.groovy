package musicstore.pages

import grails.plugins.selenium.pageobjects.GrailsListPage

class UserListPage extends GrailsListPage {

	static UserListPage open() {
		def page = new UserListPage()
		page.selenium.open "/user/list"
		if (!page.selenium.title == "List Users") {
			throw new PageStateException("List Users page did not open, found page title $page.selenium.title")
		}
		return page
	}

	static LoginPage openNotAuthenticated() {
		def page = new LoginPage()
		page.selenium.open "/user/list"
		if (!page.selenium.title == "Denied") {
			throw new PageStateException("Should not be able to open List Users page without logging in")
		}
		return page
	}

}