package pirates

import grails.plugins.springcache.annotations.Cacheable

class PiracyService {

	static transactional = false

	@Cacheable(cacheName = "PirateCache")
	List listPirateNames() {
		Pirate.withCriteria {
			projections {
				property "name"
			}
			order "name", "asc"
		}
	}

	@Cacheable(cacheName = "PirateCache")
	List findPirateNames(String name) {
		Pirate.withCriteria {
			projections {
				property "name"
			}
			ilike "name", "%$name%"
			order "name", "asc"
		}
	}
}