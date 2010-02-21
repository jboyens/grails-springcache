package musicstore.pages

import grails.plugins.selenium.pageobjects.GrailsFormPage

class LoginPage extends GrailsFormPage {

	static LoginPage open() {
		def page = new LoginPage()
		page.selenium.open "/login"
		return page
	}

	HomePage login() {
		selenium.clickAndWait("css=#loginForm input[type=submit]")
		if (selenium.isElementPresent("css=.login_message")) {
			def loginMessage = selenium.getText("css=.login_message")
			throw new PageStateException("Login failed with message: '$loginMessage'")
		}
		return new HomePage()
	}

}