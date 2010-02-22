package musicstore.pages

import grails.plugins.selenium.pageobjects.GrailsListPage

class AlbumListPage extends GrailsListPage {

	static AlbumListPage open() {
		def page = new AlbumListPage()
		page.selenium.open "/album/list"
		return page
	}

	AlbumListPage refresh() {
		selenium.refreshAndWait()
		return new AlbumListPage()
	}

	String getTitle() { selenium.title }

	boolean isSitemeshDecorated() { selenium.isElementPresent "css=#grailsLogo" }

	// TODO: sucks duplicating these from HomePage but I get StackOverflowError if I try to use @Mixin as classes have common root
	String getLoggedInMessage() {
		return isUserLoggedIn() ? selenium.getText("loggedInUser") : null
	}

	boolean isUserLoggedIn() {
		selenium.isElementPresent("loggedInUser")
	}

}
