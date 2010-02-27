package musicstore.modules

import musicstore.Album
import grails.converters.*
import grails.plugin.springcache.annotations.Cacheable

class LatestController {

	@Cacheable("latestControllerCache")
	def albums = {
		def albums = Album.list(sort: "dateCreated", order: "desc", max: 10)
		withFormat {
			html albumInstanceList: albums
			xml {
				render albums as XML
			}
		}
	}

}