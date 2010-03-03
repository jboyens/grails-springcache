package musicstore.pages

import grails.plugins.selenium.pageobjects.GrailsShowPage
import grails.plugins.selenium.pageobjects.GrailsPage
import grails.plugins.selenium.pageobjects.InvalidPageStateException
import junit.framework.Assert

class AlbumShowPage extends GrailsShowPage {

	static AlbumShowPage open(id) {
		GrailsPage.open "/album/show/$id"
		return new AlbumShowPage()
	}

	static AlbumListPage openInvalidId(id) {
		GrailsPage.open "/album/show/$id"
		return new AlbumListPage()
	}

	void vote(int stars) {
		if (stars in (1..5)) {
			selenium.click("rating_star_$stars")
			waitFor("rating to be saved") {
				selenium.getText("rating_notifytext") =~ /^Rating saved\./
			}
		} else {
			throw new IllegalArgumentException("Can only vote 1..5 stars")
		}
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

	// TODO: this should be accessible somewhere on the Selenium RC API
	void waitFor(String message, Closure condition) {
		def timeoutTime = System.currentTimeMillis() + selenium.defaultTimeout
		while (System.currentTimeMillis() < timeoutTime) {
			try {
				if (condition.call()) {
					return
				}
			}
			catch (e) {}
			sleep(500)
		}

		throw new InvalidPageStateException("Timed out waiting for: $message.")
	}
}