package musicstore

import org.grails.rateable.Rateable
import org.springframework.context.MessageSourceResolvable

class Album implements MessageSourceResolvable, Rateable {

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

	static transients = ["codes", "arguments", "defaultMessage"]

	String toString() { "$name by ${artist?.name} ($year)" }

	String[] getCodes() { [Album.name] as String[] }

	Object[] getArguments() { [name, artist.name, year] as Object[] }

	String getDefaultMessage() { "{0} by {1} ({2})" }
}
