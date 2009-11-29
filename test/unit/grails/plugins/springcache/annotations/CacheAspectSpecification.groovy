package grails.plugins.springcache.annotations

import grails.plugins.springcache.annotations.CacheAspect
import grails.plugins.springcache.cache.CacheFacade
import grails.plugins.springcache.cache.CacheKey
import org.aspectj.lang.ProceedingJoinPoint
import spock.lang.Specification

class CacheAspectSpecification extends Specification {

	private static final UNCACHED_VALUE = "UNCACHED"
	private static final CACHED_VALUE = "CACHED"
	private static final KEY = CacheKey.generate([])

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