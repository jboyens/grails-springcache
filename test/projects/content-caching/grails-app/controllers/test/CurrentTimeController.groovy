package test

import grails.plugin.springcache.annotations.Cacheable

class CurrentTimeController {

	@Cacheable(modelId = "CurrentTimeController")
	def time = {
		render {
			div(class: "currentTime", new Date().format("yyyy-MM-dd HH:mm:ss"))
		}
	}

}