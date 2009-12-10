package pirates

import grails.plugin.springcache.annotations.CacheFlush
import grails.plugin.springcache.annotations.Cacheable
import pirates.Pirate
import pirates.Ship

class PiracyService {

	static transactional = false

	@Cacheable(modelId = "Pirates")
	List listPirateNames() {
		Pirate.withCriteria {
			projections {
				property "name"
			}
			order "name", "asc"
		}
	}

	@Cacheable(modelId = "Pirates")
	List findPirateNames(String name) {
		Pirate.withCriteria {
			projections {
				property "name"
			}
			ilike "name", "%$name%"
			order "name", "asc"
		}
	}

	@Cacheable(modelId = "Ships")
	List listShipNames() {
		Ship.withCriteria {
			projections {
				property "name"
			}
			order "name", "asc"
		}
	}

	@CacheFlush(modelId = "Pirates")
	void newPirate(String name) {
		new Pirate(name: name).save(failOnError: true)
	}

	@CacheFlush(modelId = "All")
	void newShip(String name, List crewNames) {
		new Ship(name: name, crew: crewNames.collect {
			Pirate.findByName(it) ?: new Pirate(name: it)
		}).save(failOnError: true)
	}
}