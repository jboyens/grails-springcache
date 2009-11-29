package pirates

class Pirate {

	String name

 	static constraints = {
		 name blank: false, unique: true
	 }

}