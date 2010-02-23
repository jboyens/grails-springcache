package musicstore.pages

import grails.plugins.selenium.pageobjects.GrailsCreatePage
import grails.plugins.selenium.pageobjects.GrailsPage
import grails.plugins.selenium.pageobjects.InvalidPageStateException

class AlbumCreatePage extends GrailsCreatePage {

	static AlbumCreatePage open() {
		GrailsPage.open "/album/create"
		return new AlbumCreatePage()
	}

	@Override protected void validate() {
		def title = selenium.title
		if (title != "Create Album") {
			throw new InvalidPageStateException("Create Album page is not open, found page title $title")
		}
		if (!selenium.isElementPresent("css=#grailsLogo")) {
			throw new InvalidPageStateException("Page is missing Sitemesh decoration")
		}
	}
}