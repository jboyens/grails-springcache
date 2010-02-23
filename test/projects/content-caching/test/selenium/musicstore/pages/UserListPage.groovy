package musicstore.pages

import grails.plugins.selenium.pageobjects.GrailsListPage
import grails.plugins.selenium.pageobjects.GrailsPage
import grails.plugins.selenium.pageobjects.InvalidPageStateException

class UserListPage extends GrailsListPage {

	static UserListPage open() {
		GrailsPage.open "/user/list"
		return new UserListPage()
	}

	static LoginPage openNotAuthenticated() {
		GrailsPage.open "/user/list"
		return new LoginPage()
	}

	@Override protected void validate() {
		def title = selenium.title
		if (title != "User List") {
			throw new InvalidPageStateException("List Users page did not open, found page title $title")
		}
	}

}