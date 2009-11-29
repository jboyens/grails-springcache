package grails.plugins.springcache.annotations

import grails.plugins.springcache.annotations.CacheAspect
import grails.plugins.springcache.cache.CacheFacade
import grails.plugins.springcache.cache.DefaultCacheKey
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.Signature
import spock.lang.Specification

class CacheAspectSpecification extends Specification {

	private static final UNCACHED_VALUE = "UNCACHED"
	private static final CACHED_VALUE = "CACHED"
	private static final KEY = new DefaultCacheKey([])

	void "Cache keys are distinguished by the name and arguments of the invoked method"() {
		when: "cache keys are generated"
		def aspect = new CacheAspect()
		def key1 = aspect.generateCacheKey(joinPoint1)
		def key2 = aspect.generateCacheKey(joinPoint2)

		then: "the keys should not be equal for different method name and argument combinations"
		key1 != key2

		and: "the keys' hashCodes should not be equal"
		key1.hashCode() != key2.hashCode()

		where:
		joinPoint1 << [newJoinPoint("method1"), newJoinPoint("method", ["a"]), newJoinPoint("method", ["a", "b"]), newJoinPoint("method")]
		joinPoint2 << [newJoinPoint("method2"), newJoinPoint("method", ["b"]), newJoinPoint("method", ["a", "c"]), newJoinPoint("method", ["x"])]
	}

	void "Cache keys are consistent for repeated method calls"() {
		when: "cache keys are generated"
		def aspect = new CacheAspect()
		def key1 = aspect.generateCacheKey(joinPoint1)
		def key2 = aspect.generateCacheKey(joinPoint2)

		then: "the keys should be equal for multiple method calls with the same arguments"
		key1 == key2

		and: "the keys' hashCodes should be equal"
		key1.hashCode() == key2.hashCode()

		where:
		joinPoint1 << [newJoinPoint("method"), newJoinPoint("method", ["a", "b"])]
		joinPoint2 << [newJoinPoint("method"), newJoinPoint("method", ["a", "b"])]
	}

	static ProceedingJoinPoint newJoinPoint(String methodName, List args = []) {
		def joinPoint = [:]
		joinPoint.getSignature = {-> [getName: {-> methodName }] as Signature }
		joinPoint.getArgs = {-> args as Object[] }
		return joinPoint as ProceedingJoinPoint
	}

	void "The intercepted method is invoked if the cache does not contain the result of a previous call"() {
		given: "the cache is empty"
		def joinPoint = Mock(ProceedingJoinPoint)
		def cache = Mock(CacheFacade)
		cache.containsKey(KEY) >> false

		when: "a method call is intercepted"
		def result = new CacheAspect().getFromCacheOrInvoke(joinPoint, cache, KEY)

		then: "the method is invoked"
		1 * joinPoint.proceed() >> UNCACHED_VALUE

		and: "the method's result is returned"
		result == UNCACHED_VALUE

		and: "the method's result is cached"
		1 * cache.put(KEY, UNCACHED_VALUE)
	}

	void "The cached value is returned if the cache contains the result of a previous call"() {
		given: "the result of a previous call is in the cache"
		def joinPoint = Mock(ProceedingJoinPoint)
		def cache = Mock(CacheFacade)
		cache.containsKey(KEY) >> true
		cache.get(KEY) >> CACHED_VALUE

		when: "a method call is intercepted"
		def result = new CacheAspect().getFromCacheOrInvoke(joinPoint, cache, KEY)

		then: "the cached value is returned"
		result == CACHED_VALUE

		and: "the method is not invoked"
		0 * joinPoint.proceed()
	}

}