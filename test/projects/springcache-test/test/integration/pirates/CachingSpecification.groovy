package pirates

import grails.plugin.spock.IntegrationSpecification
import net.sf.ehcache.Cache
import pirates.Pirate
import grails.validation.ValidationException

class CachingSpecification extends IntegrationSpecification {

	def piracyService
	def springcacheCacheManager

	void setupSpec() {
		Pirate.build(name: "Blackbeard")
		Pirate.build(name: "Calico Jack")
		Pirate.build(name: "Black Bart")
		Ship.build(name: "Queen Anne's Revenge", crew: Pirate.findAllByName("Blackbeard"))
	}

	void cleanup() {
		springcacheCacheManager.removalAll()
	}

	void cleanupSpec() {
		Ship.list()*.delete()
		Pirate.list()*.delete()
	}

	void "Cached results should be returned for subsequent method calls"() {
		given: "A cache exists"
		def cache = new Cache("PirateCache", 100, false, true, 0, 0)
		springcacheCacheManager.addCache(cache)

		when: "A cachable method is called twice"
		def result1 = piracyService.listPirateNames()
		def result2 = piracyService.listPirateNames()

		then: "The first call primes the cache"
		cache.statistics.objectCount == 1

		and: "The second call hits the cache"
		cache.statistics.cacheHits == 1

		and: "The same result is returned by both calls"
		result1 == ["Black Bart", "Blackbeard", "Calico Jack"]
		result1 == result2
	}

	void "Cached results should not be returned for a subsequent call with different arguments"() {
		given: "A cache exists"
		def cache = new Cache("PirateCache", 100, false, true, 0, 0)
		springcacheCacheManager.addCache(cache)

		when: "A cacheable method is called twice with different arguments"
		def result1 = piracyService.findPirateNames("jack")
		def result2 = piracyService.findPirateNames("black")

		then: "The cache is not hit"
		cache.statistics.cacheHits == 0

		and: "The results are cached separately"
		cache.statistics.objectCount == 2

		and: "The results are correct"
		result1 == ["Calico Jack"]
		result2 == ["Black Bart", "Blackbeard"]
	}

	void "The cache can be flushed"() {
		given: "A cache exists"
		def cache = new Cache("PirateCache", 100, false, true, 0, 0)
		springcacheCacheManager.addCache(cache)

		when: "A cacheable method is called"
		def result1 = piracyService.listPirateNames()

		and: "A flushing method is called"
		piracyService.newPirate("Anne Bonny")

		and: "The cacheable method is called again"
		def result2 = piracyService.listPirateNames()

		then: "The cache is not hit"
		cache.statistics.cacheHits == 0

		and: "The results from before and after flushing are different"
		result1 == ["Black Bart", "Blackbeard", "Calico Jack"]
		result2 == ["Anne Bonny", "Black Bart", "Blackbeard", "Calico Jack"]
	}

	void "The cache is flushed even if the flushing method fails"() {
		given: "A cache exists"
		def cache = new Cache("PirateCache", 100, false, true, 0, 0)
		springcacheCacheManager.addCache(cache)

		and: "The cache is primed"
		piracyService.listPirateNames()
		def initialCacheSize = cache.statistics.objectCount

		when: "A flushing method is called with parameters that will cause it to fail"
		piracyService.newPirate("Blackbeard")

		then: "An exception is thrown by the flushing method"
		thrown ValidationException

		and: "The cache is still flushed"
		initialCacheSize == 1
		cache.statistics.objectCount == 0
	}

	void "Multiple caches can be flushed by a single method"() {
		given: "Multiple caches exist"
		def cache1 = new Cache("PirateCache", 100, false, true, 0, 0)
		def cache2 = new Cache("ShipCache", 100, false, true, 0, 0)
		springcacheCacheManager.addCache(cache1)
		springcacheCacheManager.addCache(cache2)

		and: "Both caches are primed"
		piracyService.listPirateNames()
		piracyService.listShipNames()
		def initialCache1Size = cache1.statistics.objectCount
		def initialCache2Size = cache2.statistics.objectCount

		when: "A method is called that should flush both caches"
		piracyService.newShip("Royal Fortune", ["Black Bart", "Walter Kennedy"])

		then: "Both caches are flushed"
		initialCache1Size == 1
		cache1.statistics.objectCount == 0
		initialCache2Size == 1
		cache2.statistics.objectCount == 0
	}

}