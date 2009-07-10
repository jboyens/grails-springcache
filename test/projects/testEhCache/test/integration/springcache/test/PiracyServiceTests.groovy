package springcache.test

class PiracyServiceTests extends GroovyTestCase {

	def cacheManager
	def piracyService

	void tearDown() {
		super.tearDown()

		cacheManager.cacheNames.each { cacheName ->
			cacheManager.getCache(cacheName).statistics.clearStatistics()
		}
		cacheManager.clearAll()
	}

	int hits(String cacheName) {
		return cacheManager.getCache(cacheName).statistics.cacheHits
	}

	int misses(String cacheName) {
		return cacheManager.getCache(cacheName).statistics.cacheMisses
	}

	void testCacheAndFlush() {
		piracyService.registerNewSailor(new Sailor(name: "Rob"))

		assertEquals "Rob", piracyService.sailors.name.join(", ")

		assertEquals 1, misses("SAILOR_CACHE")
		assertEquals 0, hits("SAILOR_CACHE")

		assertEquals "Rob", piracyService.sailors.name.join(", ")

		assertEquals 1, misses("SAILOR_CACHE")
		assertEquals 1, hits("SAILOR_CACHE")

		piracyService.registerNewSailor(new Sailor(name: "Glenn"))

		assertEquals "Rob, Glenn", piracyService.sailors.name.join(", ")

		assertEquals 2, misses("SAILOR_CACHE")
		assertEquals 1, hits("SAILOR_CACHE")
	}

	void testFlushMultipleCaches() {
		2.times {
			assertEquals([], piracyService.sailors)
			assertEquals([], piracyService.ships)
		}

		assertEquals 1, misses("SAILOR_CACHE")
		assertEquals 1, misses("SHIP_CACHE")
		assertEquals 1, hits("SAILOR_CACHE")
		assertEquals 1, hits("SHIP_CACHE")

		piracyService.registerNewShipWithCrew(new Ship(name: "Queen Anne's Revenge"), [new Sailor(name: "Rob")])

		2.times {
			assertEquals "Rob", piracyService.sailors.name.join(", ")
			assertEquals "Queen Anne's Revenge", piracyService.ships.name.join(", ")
		}

		assertEquals 2, misses("SAILOR_CACHE")
		assertEquals 2, misses("SHIP_CACHE")
		assertEquals 2, hits("SAILOR_CACHE")
		assertEquals 2, hits("SHIP_CACHE")
	}

}