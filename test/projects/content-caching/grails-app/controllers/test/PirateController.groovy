package test

import grails.plugin.springcache.annotations.Cacheable

@Cacheable (modelId = "PirateController")
class PirateController {

	def beforeInterceptor = {
		log.debug "Hit controller = $controllerName, action = $actionName"
	}

	static scaffold = true
}
