package grails.plugins.springcache.annotations

import grails.plugins.springcache.annotations.CacheAspect
import grails.plugins.springcache.annotations.Cacheable
import grails.plugins.springcache.cache.Cache
import grails.plugins.springcache.cache.CacheKey
import grails.plugins.springcache.cache.CacheManager
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.Signature
import org.gmock.WithGMock
import spock.lang.Specification
import static org.hamcrest.Matchers.instanceOf

@WithGMock
class CacheAspectSpecification extends Specification {

	private static final UNCACHED_VALUE = "UNCACHED"
	private static final CACHED_VALUE = "CACHED"

	CacheManager cacheManager
	Cacheable cacheable
	CacheAspect aspect

	void setup() {
		cacheable = [cacheName: {-> "cacheName" }] as Cacheable

		cacheManager = mock(CacheManager)

		aspect = new CacheAspect()
		aspect.cacheManager = cacheManager
	}

	void "Cache keys are distinguished by the name and arguments of the invoked method"() {
		when: "cache keys are generated"
		def key1 = aspect.toCacheKey(joinPoint1)
		def key2 = aspect.toCacheKey(joinPoint2)

		then: "the keys should not be equal for different method name and argument combinations"
		key1 != key2

		and: "the keys' hashCodes should not be equal"
		key1.hashCode() != key2.hashCode()

		where:
		joinPoint1 << [createJoinPoint("method1"), createJoinPoint("method", ["a"]), createJoinPoint("method")]
		joinPoint2 << [createJoinPoint("method2"), createJoinPoint("method", ["b"]), createJoinPoint("method", ["x"])]
	}

	void "The intercepted method is invoked if the cache does not contain the result of a previous call"() {
		given: "an empty cache"
		def cache = new MockCache()
		cacheManager.getCache(cacheable).returns(cache)

		when: "an method call is intercepted"
		def result
		play {
			result = aspect.aroundAdvice(createJoinPoint("methodName"), cacheable)
		}

		then: "the method is invoked and its return value returned"
		result == UNCACHED_VALUE

		and: "the method result is cached"
		cache.size() == 1
	}

	void "the cached value is returned if the cache contains the result of a previous call"() {
		given: "the result of a previous call is in the cache"
		def cache = new MockCache()
		cache.put(aspect.toCacheKey(createJoinPoint("methodName")), CACHED_VALUE)
		cacheManager.getCache(cacheable).returns(cache)

		when: "a method call is intercepted"
		def result
		play {
			result = aspect.aroundAdvice(createJoinPoint("methodName"), cacheable)
		}

		then: "the cached value is returned"
		result == CACHED_VALUE
	}

	static ProceedingJoinPoint createJoinPoint(String methodName, List args = []) {
		def joinPoint = [:]
		joinPoint.getSignature = {-> [getName: {-> methodName }] as Signature }
		joinPoint.getArgs = {-> args as Object[] }
		joinPoint.proceed = {-> UNCACHED_VALUE }
		return joinPoint as ProceedingJoinPoint
	}
}

class MockCache implements Cache {

	private Map map = [:]

	boolean containsKey(CacheKey key) {
		return map.containsKey(key)
	}

	Object get(CacheKey key) {
		return map[key]
	}

	void put(CacheKey key, Object value) {
		map[key] = value
	}

	int size() {
		map.size()
	}
}