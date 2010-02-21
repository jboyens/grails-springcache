package musicstore.pages

import grails.plugins.selenium.pageobjects.GrailsPage

class HomePage extends GrailsPage {


	static HomePage open() {
		def page = new HomePage()
		page.selenium.open("/")
		return page
	}

	String getLoggedInMessage() {
		return isUserLoggedIn() ? selenium.getText("loggedInUser") : null
	}

	boolean isUserLoggedIn() {
		selenium.isElementPresent("loggedInUser")
	}

	LoginPage goToLogin() {
		if (isUserLoggedIn()) {
			throw new IllegalStateException("Already logged in")
		} else {
			selenium.clickAndWait("css=#loginLink a")
			return new LoginPage()
		}
	}

}