package musicstore.pages

import grails.plugins.selenium.pageobjects.GrailsShowPage

class AlbumShowPage extends GrailsShowPage {
	static AlbumShowPage open(id) {
		def page = new AlbumShowPage()
		page.selenium.open "/album/show/$id"
		return page
	}
}