package grails.plugins.springcache.cache

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.Signature
import spock.lang.Specification

class CacheKeySpecification extends Specification {

	static final TARGET_1 = new Object()
	static final TARGET_2 = new Object()

	void "Cache keys are distinguished by the name and arguments of the invoked method"() {
		when: "cache keys are generated"
		def key1 = CacheKey.generate(joinPoint1)
		def key2 = CacheKey.generate(joinPoint2)

		then: "the keys should not be equal for different method name and argument combinations"
		key1 != key2

		and: "the keys' hashCodes should not be equal"
		key1.hashCode() != key2.hashCode()

		where:
		joinPoint1 << [mockJoinPoint(TARGET_1, "method1"), mockJoinPoint(TARGET_1, "method", ["a"]), mockJoinPoint(TARGET_1, "method", ["a", "b"]), mockJoinPoint(TARGET_1, "method"), mockJoinPoint(TARGET_1, "method")]
		joinPoint2 << [mockJoinPoint(TARGET_1, "method2"), mockJoinPoint(TARGET_1, "method", ["b"]), mockJoinPoint(TARGET_1, "method", ["a", "c"]), mockJoinPoint(TARGET_1, "method", ["x"]), mockJoinPoint(TARGET_2, "method")]
	}

	void "Cache keys are consistent for repeated method calls"() {
		when: "cache keys are generated"
		def key1 = CacheKey.generate(joinPoint1)
		def key2 = CacheKey.generate(joinPoint2)

		then: "the keys should be equal for multiple method calls with the same arguments"
		key1 == key2

		and: "the keys' hashCodes should be equal"
		key1.hashCode() == key2.hashCode()

		where:
		joinPoint1 << [mockJoinPoint(TARGET_1, "method"), mockJoinPoint(TARGET_1, "method", ["a", "b"])]
		joinPoint2 << [mockJoinPoint(TARGET_1, "method"), mockJoinPoint(TARGET_1, "method", ["a", "b"])]
	}

	static JoinPoint mockJoinPoint(Object target, String methodName, List args = []) {
		def joinPoint = [:]
		joinPoint.getTarget = {-> target }
		joinPoint.getSignature = {-> [getName: {-> methodName }] as Signature }
		joinPoint.getArgs = {-> args as Object[] }
		return joinPoint as JoinPoint
	}

}