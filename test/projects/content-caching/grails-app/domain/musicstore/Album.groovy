package musicstore

class Album {

	Artist artist
	String name
	String year
	List tracks
	Date dateCreated
	Date lastUpdated

	static hasMany = [tracks: Song]

    static constraints = {
		name blank: false
		year matches: /^[0-9]{4}$/
    }

	static mapping = {
		artist cascade: "save-update"
	}

	String toString() { "$name by ${artist?.name} ($year)" }

}
