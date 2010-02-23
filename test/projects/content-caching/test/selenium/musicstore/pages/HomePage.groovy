package musicstore.pages

import grails.plugins.selenium.pageobjects.GrailsPage
import grails.plugins.selenium.pageobjects.InvalidPageStateException

class HomePage extends GrailsPage {

	static HomePage open() {
		GrailsPage.open "/"
		return new HomePage()
	}

	HomePage refresh() {
		selenium.refreshAndWait()
		return new HomePage()
	}

	String getLoggedInMessage() {
		return isUserLoggedIn() ? selenium.getText("loggedInUser") : null
	}

	boolean isUserLoggedIn() {
		selenium.isElementPresent("loggedInUser")
	}

	LoginPage goToLogin() {
		if (isUserLoggedIn()) {
			throw new InvalidPageStateException("Already logged in")
		} else {
			selenium.clickAndWait("css=#loginLink a")
			return new LoginPage()
		}
	}

	List<String> getLatestAlbums() {
		def list = []
		int i = 1
		while (selenium.isElementPresent("//div[@id='latestAlbums']/ol/li[$i]")) {
			list << selenium.getText("//div[@id='latestAlbums']/ol/li[$i]")
			i++
		}
		return list
	}

	List<String> getPopularAlbums() {
		def list = []
		int i = 1
		while (selenium.isElementPresent("//div[@id='popularAlbums']/ol/li[$i]")) {
			list << selenium.getText("//div[@id='popularAlbums']/ol/li[$i]/span[@class='album']")
			i++
		}
		return list
	}

	@Override protected void validate() {
		def title = selenium.title
		if (title != "Welcome to Grails") {
			throw new InvalidPageStateException("Home page is not open, found page title $title")
		}
		if (!selenium.isElementPresent("css=#grailsLogo")) {
			throw new InvalidPageStateException("Page is missing Sitemesh decoration")
		}
	}
}