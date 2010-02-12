package test

class CurrentTimeController {

	def time = {
		render {
			div(new Date().format("yyyy-MM-dd HH:mm:ss"))
		}
	}

}