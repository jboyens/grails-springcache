package grails.plugins.springcache.annotations

import grails.plugins.springcache.annotations.CacheAspect
import grails.plugins.springcache.cache.Cache
import grails.plugins.springcache.cache.CacheKey
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.Signature
import org.gmock.WithGMock
import spock.lang.Specification

@WithGMock
class CacheAspectSpecification extends Specification {

	private static final UNCACHED_VALUE = "UNCACHED"
	private static final CACHED_VALUE = "CACHED"

	void "Cache keys are distinguished by the name and arguments of the invoked method"() {
		setup:
		def aspect = new CacheAspect()

		when: "cache keys are generated"
		def key1 = aspect.generateCacheKey(joinPoint1)
		def key2 = aspect.generateCacheKey(joinPoint2)

		then: "the keys should not be equal for different method name and argument combinations"
		key1 != key2

		and: "the keys' hashCodes should not be equal"
		key1.hashCode() != key2.hashCode()

		where:
		joinPoint1 << [createJoinPoint("method1"), createJoinPoint("method", ["a"]), createJoinPoint("method", ["a", "b"]), createJoinPoint("method")]
		joinPoint2 << [createJoinPoint("method2"), createJoinPoint("method", ["b"]), createJoinPoint("method", ["a", "c"]), createJoinPoint("method", ["x"])]
	}

	void "Cache keys are consistent for repeated method calls"() {
		setup:
		def aspect = new CacheAspect()

		when: "cache keys are generated"
		def key1 = aspect.generateCacheKey(joinPoint1)
		def key2 = aspect.generateCacheKey(joinPoint2)

		then: "the keys should be equal for multiple method calls with the same arguments"
		key1 == key2

		and: "the keys' hashCodes should be equal"
		key1.hashCode() == key2.hashCode()

		where:
		joinPoint1 << [createJoinPoint("method"), createJoinPoint("method", ["a", "b"])]
		joinPoint2 << [createJoinPoint("method"), createJoinPoint("method", ["a", "b"])]
	}

	static ProceedingJoinPoint createJoinPoint(String methodName, List args = []) {
		def joinPoint = [:]
		joinPoint.getSignature = {-> [getName: {-> methodName }] as Signature }
		joinPoint.getArgs = {-> args as Object[] }
		return joinPoint as ProceedingJoinPoint
	}

	void "The intercepted method is invoked if the cache does not contain the result of a previous call"() {
		given: "the cache is empty"
		def aspect = new CacheAspect()
		def joinPoint = mock(ProceedingJoinPoint)
		def key = mock(CacheKey)
		def cache = new MockCache()

		when: "a method call is intercepted"
		joinPoint.proceed().returns(UNCACHED_VALUE)
		def result
		play {
			result = aspect.getFromCacheOrInvoke(joinPoint, cache, key)
		}

		then: "the method's result is returned"
		result == UNCACHED_VALUE

		and: "the method's result is cached"
		cache.get(key) == UNCACHED_VALUE
	}

	void "The cached value is returned if the cache contains the result of a previous call"() {
		given: "the result of a previous call is in the cache"
		def aspect = new CacheAspect()
		def joinPoint = mock(ProceedingJoinPoint)
		def key = mock(CacheKey)
		def cache = new MockCache()
		cache.put(key, CACHED_VALUE)

		when: "a method call is intercepted"
		def result
		play {
			result = aspect.getFromCacheOrInvoke(joinPoint, cache, key)
		}

		then: "the cached value is returned"
		result == CACHED_VALUE
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

	void flush() {
		map.clear()
	}
}