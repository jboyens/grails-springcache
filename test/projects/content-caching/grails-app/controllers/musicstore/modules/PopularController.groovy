package musicstore.modules

import grails.plugin.springcache.annotations.Cacheable
import musicstore.Album
import org.grails.rateable.RatingLink

class PopularController {

	@Cacheable("popularControllerCache")
	def albums = {
		if (RatingLink.countByType("album") > 0) {
			def albums = Album.listOrderByAverageRating(max: 10)
			return [albumInstanceList: albums]
		} else {
			return [albumInstanceList: []]
		}
	}

}