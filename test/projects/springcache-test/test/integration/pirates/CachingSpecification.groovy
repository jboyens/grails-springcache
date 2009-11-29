package pirates

import grails.plugin.spock.IntegrationSpecification
import net.sf.ehcache.Cache
import pirates.Pirate

class CachingSpecification extends IntegrationSpecification {

	def piracyService
	def cacheManager

	void cleanup() {
		cacheManager.removalAll()
		Pirate.list()*.delete()
	}

	void "Cached results should be returned for subsequent method calls"() {
		given: "Some data exists"
		Pirate.build(name: "Blackbeard")

		and: "A cache exists"
		def cache = new Cache("PirateCache", 100, false, true, 0, 0)
		cacheManager.addCache(cache)

		when: "A cachable method is called twice"
		def result1 = piracyService.listPirateNames()
		def result2 = piracyService.listPirateNames()

		then: "The first call primes the cache"
		cache.statistics.objectCount == 1

		and: "The second call hits the cache"
		cache.statistics.cacheHits == 1

		and: "The same result is returned by both calls"
		result1 == ["Blackbeard"]
		result1 == result2
	}

	void "Cached results should not be returned for a subsequent call with different arguments"() {
		given: "Some data exists"
		Pirate.build(name: "Blackbeard")
		Pirate.build(name: "Calico Jack")
		Pirate.build(name: "Black Bart")

		and: "A cache exists"
		def cache = new Cache("PirateCache", 100, false, true, 0, 0)
		cacheManager.addCache(cache)

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

}