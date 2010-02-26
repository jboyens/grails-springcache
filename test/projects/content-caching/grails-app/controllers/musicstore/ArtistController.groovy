package musicstore

import grails.converters.JSON
import grails.plugin.springcache.annotations.Cacheable

@Cacheable("artistControllerCache")
class ArtistController {

	static scaffold = true

	def autoCompleteJSON = {
		def list = Artist.list(params.query)
		println "$params.query found $list"
		def jsonList = list.collect { [id: it.it, name: it.name] }
		def jsonResult = [
				result: jsonList
		]
		render jsonResult as JSON
	}
}
