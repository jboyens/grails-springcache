package pirates

class PirateController {

	def piracyService

	def list = {
		[pirateNames: piracyService.listPirateNames()]
	}
	
	def add = {
		piracyService.newPirate(params.name)
		redirect action: "list"
	}
}
