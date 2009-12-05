package pirates

import grails.plugins.springcache.annotations.CacheFlush
import grails.plugins.springcache.annotations.Cacheable
import pirates.Pirate
import pirates.Ship

class PiracyService {

	static transactional = false

	@Cacheable(model = "PirateCachingModel")
	List listPirateNames() {
		Pirate.withCriteria {
			projections {
				property "name"
			}
			order "name", "asc"
		}
	}

	@Cacheable(model = "PirateCachingModel")
	List findPirateNames(String name) {
		Pirate.withCriteria {
			projections {
				property "name"
			}
			ilike "name", "%$name%"
			order "name", "asc"
		}
	}

	@Cacheable(model = "ShipCachingModel")
	List listShipNames() {
		Ship.withCriteria {
			projections {
				property "name"
			}
			order "name", "asc"
		}
	}

	@CacheFlush(model = "PirateFlushingModel")
	void newPirate(String name) {
		new Pirate(name: name).save(failOnError: true)
	}

	@CacheFlush(model = "AllFlushingModel")
	void newShip(String name, List crewNames) {
		new Ship(name: name, crew: crewNames.collect {
			Pirate.findByName(it) ?: new Pirate(name: it)
		}).save(failOnError: true)
	}
}