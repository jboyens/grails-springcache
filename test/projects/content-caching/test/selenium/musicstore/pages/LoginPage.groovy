package musicstore.pages

import grails.plugins.selenium.pageobjects.GrailsFormPage
import grails.plugins.selenium.pageobjects.GrailsPage
import grails.plugins.selenium.pageobjects.InvalidPageStateException

class LoginPage extends GrailsFormPage {

	static LoginPage open() {
		GrailsPage.open "/login"
		return new LoginPage()
	}

	HomePage login() {
		selenium.clickAndWait("css=#loginForm input[type=submit]")
		if (selenium.isElementPresent("css=.login_message")) {
			def loginMessage = selenium.getText("css=.login_message")
			throw new InvalidPageStateException("Login failed with message: '$loginMessage'")
		}
		return new HomePage()
	}

	@Override protected void validate() {
		def title = selenium.title
		if (title != "Login") {
			throw new InvalidPageStateException("Login page is not open, found page title $title")
		}
		if (!selenium.isElementPresent("css=#grailsLogo")) {
			throw new InvalidPageStateException("Page is missing Sitemesh decoration")
		}
	}
}