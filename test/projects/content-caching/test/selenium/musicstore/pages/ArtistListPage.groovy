package musicstore.pages

import grails.plugins.selenium.pageobjects.GrailsListPage

class ArtistListPage extends GrailsListPage {

	static ArtistListPage open() {
		def page = new ArtistListPage()
		page.selenium.open "/artist/list"
		return page
	}

	ArtistListPage refresh() {
		selenium.refreshAndWait()
		return new ArtistListPage()
	}

	String getTitle() { selenium.title }

	boolean isSitemeshDecorated() { selenium.isElementPresent "css=#grailsLogo" }
}