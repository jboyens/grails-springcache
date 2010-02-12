package test

class PirateController {

	def beforeInterceptor = {
		log.debug "Hit controller = $controllerName, action = $actionName"
	}

	static scaffold = true
}
