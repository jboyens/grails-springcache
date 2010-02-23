package musicstore.pages

import grails.plugins.selenium.pageobjects.GrailsListPage
import grails.plugins.selenium.pageobjects.GrailsPage
import grails.plugins.selenium.pageobjects.InvalidPageStateException

class AlbumListPage extends GrailsListPage {

	static AlbumListPage open() {
		GrailsPage.open "/album/list"
		return new AlbumListPage()
	}

	AlbumListPage refresh() {
		selenium.refreshAndWait()
		return new AlbumListPage()
	}

	String getTitle() { selenium.title }

	// TODO: sucks duplicating these from HomePage but I get StackOverflowError if I try to use @Mixin as classes have common root
	String getLoggedInMessage() {
		return isUserLoggedIn() ? selenium.getText("loggedInUser") : null
	}

	boolean isUserLoggedIn() {
		selenium.isElementPresent("loggedInUser")
	}

	@Override protected void validate() {
		def title = selenium.title
		if (title != "Album List") {
			throw new InvalidPageStateException("Album list page is not open, found page title $title")
		}
		if (!selenium.isElementPresent("css=#grailsLogo")) {
			throw new InvalidPageStateException("Page is missing Sitemesh decoration")
		}
	}

}
