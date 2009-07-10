package springcache.test

import org.springmodules.cache.annotations.Cacheable
import org.springmodules.cache.annotations.CacheFlush

class PiracyService {
	@Cacheable (modelId = 'sailorCachingModel')
	def getSailors() {
		println "getSailors"
		Sailor.list()
	}

	@Cacheable (modelId = 'sailorCachingModel')
	def getPirates() {
		Pirate.list()
	}

	@Cacheable (modelId = 'shipCachingModel')
	def getShips() {
		Ship.list()
	}

	@CacheFlush (modelId = 'sailorFlushingModel')
	void registerNewSailor(Sailor sailor) {
		println "registerNewSailor"
		sailor.save()
	}

	@CacheFlush (modelId = 'shipFlushingModel')
	void registerNewShip(Ship ship) {
		ship.save()
	}

	@CacheFlush (modelId = 'allFlushingModel')
	void registerNewShipWithCrew(Ship ship, Collection<Sailor> crew) {
		println "registerNewShipWithCrew"
		ship.save()
		crew*.save()
	}
}