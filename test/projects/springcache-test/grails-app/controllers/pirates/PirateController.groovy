package pirates

class PirateController {

	static defaultAction = "list"

	def piracyService

	def list = {
		[pirateNames: piracyService.listPirateNames()]
	}
	
	def add = {
		piracyService.newPirate(params.name)
		redirect action: "list"
	}
}
