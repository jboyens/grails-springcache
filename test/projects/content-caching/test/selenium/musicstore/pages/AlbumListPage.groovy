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
}
