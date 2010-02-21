package musicstore

class Artist {

	String name
	Date dateCreated
	Date lastUpdated

	static hasMany = [albums: Album]

    static constraints = {
		name unique: true, blank: false
    }

	String toString() { name }

}
