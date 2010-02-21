package musicstore

class Song {

	String name
	Date dateCreated
	Date lastUpdated

	static belongsTo = [album: Album]

    static constraints = {
		name blank: false
    }

	String toString() { name }
}
