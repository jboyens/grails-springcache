package grails.plugins.springcache.annotations

import grails.plugins.springcache.cache.InvocationCacheKey
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.Signature
import spock.lang.Specification

class CacheKeySpecification extends Specification {

	void "Cache keys are distinguished by the name and arguments of the invoked method"() {
		when: "cache keys are generated"
		def key1 = new InvocationCacheKey(joinPoint1)
		def key2 = new InvocationCacheKey(joinPoint2)

		then: "the keys should not be equal for different method name and argument combinations"
		key1 != key2

		and: "the keys' hashCodes should not be equal"
		key1.hashCode() != key2.hashCode()

		where:
		joinPoint1 << [mockJoinPoint("method1"), mockJoinPoint("method", ["a"]), mockJoinPoint("method", ["a", "b"]), mockJoinPoint("method")]
		joinPoint2 << [mockJoinPoint("method2"), mockJoinPoint("method", ["b"]), mockJoinPoint("method", ["a", "c"]), mockJoinPoint("method", ["x"])]
	}

	void "Cache keys are consistent for repeated method calls"() {
		when: "cache keys are generated"
		def key1 = new InvocationCacheKey(joinPoint1)
		def key2 = new InvocationCacheKey(joinPoint2)

		then: "the keys should be equal for multiple method calls with the same arguments"
		key1 == key2

		and: "the keys' hashCodes should be equal"
		key1.hashCode() == key2.hashCode()

		where:
		joinPoint1 << [mockJoinPoint("method"), mockJoinPoint("method", ["a", "b"])]
		joinPoint2 << [mockJoinPoint("method"), mockJoinPoint("method", ["a", "b"])]
	}

	static JoinPoint mockJoinPoint(String methodName, List args = []) {
		def joinPoint = [:]
		joinPoint.getSignature = {-> [getName: {-> methodName }] as Signature }
		joinPoint.getArgs = {-> args as Object[] }
		return joinPoint as JoinPoint
	}

}