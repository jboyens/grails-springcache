package pirates

import net.sf.ehcache.Cache
import grails.validation.ValidationException
import org.codehaus.groovy.grails.commons.ApplicationHolder

class CachingTests extends GroovyTestCase {

	def piracyService
	def springcacheCacheManager

	void setUp() {
		super.setUp()
		println "piracyService = $piracyService which is a ${piracyService.getClass()}"
		ApplicationHolder.application.mainContext.getBeanDefinitionNames().sort().each { name ->
			println "$name"
		}
		Pirate.build(name: "Blackbeard")
		Pirate.build(name: "Calico Jack")
		Pirate.build(name: "Black Bart")
		Ship.build(name: "Queen Anne's Revenge", crew: Pirate.findAllByName("Blackbeard"))
	}

	void tearDown() {
		super.tearDown()
		springcacheCacheManager.removalAll()
		Ship.list()*.delete()
		Pirate.list()*.delete()
	}

	void testCachedResultsAreReturnedForSubsequentMethodCalls() {
//		given: "A cache exists"
		def cache = new Cache("PirateCache", 100, false, true, 0, 0)
		springcacheCacheManager.addCache(cache)

//		when: "A cachable method is called twice"
		def result1 = piracyService.listPirateNames()
		def result2 = piracyService.listPirateNames()

//		then: "The first call primes the cache"
		assertEquals 1, cache.statistics.objectCount

//		and: "The second call hits the cache"
		assertEquals 1, cache.statistics.cacheHits

//		and: "The same result is returned by both calls"
		assertEquals(["Black Bart", "Blackbeard", "Calico Jack"], result1)
		assertEquals result1, result2
	}

	void testCachedResultsAreNotReturnedForASubsequentCallWithDifferentArguments() {
//		given: "A cache exists"
		def cache = new Cache("PirateCache", 100, false, true, 0, 0)
		springcacheCacheManager.addCache(cache)

//		when: "A cacheable method is called twice with different arguments"
		def result1 = piracyService.findPirateNames("jack")
		def result2 = piracyService.findPirateNames("black")

//		then: "The cache is not hit"
		assertEquals 0, cache.statistics.cacheHits

//		and: "The results are cached separately"
		assertEquals 2, cache.statistics.objectCount

//		and: "The results are correct"
		assertEquals(["Calico Jack"], result1)
		assertEquals(["Black Bart", "Blackbeard"], result2)
	}

	void testTheCacheCanBeFlushed() {
//		given: "A cache exists"
		def cache = new Cache("PirateCache", 100, false, true, 0, 0)
		springcacheCacheManager.addCache(cache)

//		when: "A cacheable method is called"
		def result1 = piracyService.listPirateNames()

//		and: "A flushing method is called"
		piracyService.newPirate("Anne Bonny")

//		and: "The cacheable method is called again"
		def result2 = piracyService.listPirateNames()

//		then: "The cache is not hit"
		assertEquals 0, cache.statistics.cacheHits

//		and: "The results from before and after flushing are different"
		assertEquals(["Black Bart", "Blackbeard", "Calico Jack"], result1)
		assertEquals(["Anne Bonny", "Black Bart", "Blackbeard", "Calico Jack"], result2)
	}

	void testTheCacheIsFlushedEvenIfTheFlushingMethodFails() {
//		given: "A cache exists"
		def cache = new Cache("PirateCache", 100, false, true, 0, 0)
		springcacheCacheManager.addCache(cache)

//		and: "The cache is primed"
		piracyService.listPirateNames()
		def initialCacheSize = cache.statistics.objectCount

//		when: "A flushing method is called with parameters that will cause it to fail"
		shouldFail(ValidationException) {
		piracyService.newPirate("Blackbeard")
		}

//		and: "The cache is still flushed"
		assertEquals 1, initialCacheSize
		assertEquals 0, cache.statistics.objectCount
	}

	void testMultipleCachesCanBeFlushedByASingleMethod() {
//		given: "Multiple caches exist"
		def cache1 = new Cache("PirateCache", 100, false, true, 0, 0)
		def cache2 = new Cache("ShipCache", 100, false, true, 0, 0)
		springcacheCacheManager.addCache(cache1)
		springcacheCacheManager.addCache(cache2)

//		and: "Both caches are primed"
		piracyService.listPirateNames()
		piracyService.listShipNames()
		def initialCache1Size = cache1.statistics.objectCount
		def initialCache2Size = cache2.statistics.objectCount

//		when: "A method is called that should flush both caches"
		piracyService.newShip("Royal Fortune", ["Black Bart", "Walter Kennedy"])

//		then: "Both caches are flushed"
		assertEquals 1, initialCache1Size
		assertEquals 0, cache1.statistics.objectCount
		assertEquals 1, initialCache2Size
		assertEquals 0, cache2.statistics.objectCount
	}
}