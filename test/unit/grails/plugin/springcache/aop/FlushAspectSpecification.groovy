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
package grails.plugin.springcache.aop

import grails.plugin.springcache.annotations.CacheFlush
import grails.plugin.springcache.CacheFacade
import grails.plugin.springcache.CacheProvider
import spock.lang.Specification

class FlushAspectSpecification extends Specification {

	void "All specfied caches are flushed"() {
		given: "some caches exist"
		def cache1 = Mock(CacheFacade)
		def cache2 = Mock(CacheFacade)
		def cache3 = Mock(CacheFacade)
		def cacheManager = Mock(CacheProvider)
		cacheManager.getCaches("modelId") >> [cache1, cache2]

		when: "the flush aspect is triggered"
		def annotation = [modelId: {-> "modelId" }] as CacheFlush
		def aspect = new FlushingAspect()
		aspect.cacheProvider = cacheManager
		aspect.flushCaches(annotation)

		then: "the specified caches are flushed"
		1 * cache1.flush()
		1 * cache2.flush()

		and: "any other caches are not flushed"
		0 * cache3.flush()
	}

}