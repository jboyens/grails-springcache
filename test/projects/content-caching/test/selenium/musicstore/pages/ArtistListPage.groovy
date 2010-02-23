package musicstore.pages

import grails.plugins.selenium.pageobjects.GrailsListPage
import grails.plugins.selenium.pageobjects.GrailsPage
import grails.plugins.selenium.pageobjects.InvalidPageStateException

class ArtistListPage extends GrailsListPage {

	static ArtistListPage open() {
		GrailsPage.open "/artist/list"
		return new ArtistListPage()
	}

	ArtistListPage refresh() {
		selenium.refreshAndWait()
		return new ArtistListPage()
	}

	String getTitle() { selenium.title }

	boolean isSitemeshDecorated() { selenium.isElementPresent "css=#grailsLogo" }

	@Override protected void validate() {
		def title = selenium.title
		if (title != "Artist List") {
			throw new InvalidPageStateException("Artist list page is not open, found page title $title")
		}
		if (!selenium.isElementPresent("css=#grailsLogo")) {
			throw new InvalidPageStateException("Page is missing Sitemesh decoration")
		}
	}
}