/*
 * Copyright 2009 Rob Fletcher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugins.springcache.aop

import grails.plugins.springcache.cache.CacheFacade
import grails.plugins.springcache.cache.CacheKey
import org.aspectj.lang.ProceedingJoinPoint
import spock.lang.Specification

class CacheAspectSpecification extends Specification {

	private static final UNCACHED_VALUE = "UNCACHED"
	private static final CACHED_VALUE = "CACHED"
	private static final KEY = new CacheKey(0)

	void "The intercepted method is invoked if the cache does not contain the result of a previous call"() {
		given: "the cache is empty"
		def joinPoint = Mock(ProceedingJoinPoint)
		def cache = Mock(CacheFacade)
		cache.containsKey(KEY) >> false

		when: "a method call is intercepted"
		def result = new CachingAspect().getFromCacheOrInvoke(joinPoint, cache, KEY)

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
		def result = new CachingAspect().getFromCacheOrInvoke(joinPoint, cache, KEY)

		then: "the cached value is returned"
		result == CACHED_VALUE

		and: "the method is not invoked"
		0 * joinPoint.proceed()
	}

}