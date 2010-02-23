package musicstore.modules

import grails.plugin.springcache.annotations.Cacheable
import musicstore.Album
import org.codehaus.groovy.grails.web.util.WebUtils

class PopularController {

	@Cacheable(modelId = "PopularController")
	def albums = {
		println "controller=$controllerName, INCLUDE_REQUEST_URI_ATTRIBUTE= ${request.getAttribute(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE)}"
		def albums = Album.listOrderByAverageRating(max: 10)
		return [albumInstanceList: albums]
	}

}