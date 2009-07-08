package springcache.test

class Sailor {
	String name

	static constraints = {
		name unique: true
	}

	int hashCode() {
		name.hashCode()
	}

	boolean equals(Object o) {
		if (!o) return false
		if (o.is(this)) return true
		if (!o.instanceOf(Sailor)) return false
		return o.name == this.name
	}
}