package pirates

class Ship {

	String name

	static hasMany = [crew: Pirate]

}