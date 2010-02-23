package musicstore.pages

import grails.plugins.selenium.pageobjects.GrailsShowPage
import grails.plugins.selenium.pageobjects.GrailsPage
import grails.plugins.selenium.pageobjects.InvalidPageStateException

class AlbumShowPage extends GrailsShowPage {

	static AlbumShowPage open(id) {
		GrailsPage.open "/album/show/$id"
		return new AlbumShowPage()
	}

	@Override protected void validate() {
		def title = selenium.title
		if (title != "Show Album") {
			throw new InvalidPageStateException("Show Album page is not open, found page title $title")
		}
		if (!selenium.isElementPresent("css=#grailsLogo")) {
			throw new InvalidPageStateException("Page is missing Sitemesh decoration")
		}
	}
}