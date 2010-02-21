package musicstore.pages

import grails.plugins.selenium.pageobjects.GrailsCreatePage

class AlbumCreatePage extends GrailsCreatePage {

	static AlbumCreatePage open() {
		def page = new AlbumCreatePage()
		page.selenium.open "/album/create"
		return page
	}

}