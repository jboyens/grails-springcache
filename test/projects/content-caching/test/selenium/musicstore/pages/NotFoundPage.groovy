package musicstore.pages

import grails.plugins.selenium.pageobjects.GrailsPage
import grails.plugins.selenium.pageobjects.InvalidPageStateException

class NotFoundPage extends GrailsPage {

	void validate() {
		def title = selenium.title
		if (title != "404: Not Found") {
			throw new InvalidPageStateException("Expected 404 page but found $title")
		}
	}
}
