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

import grails.plugin.springcache.SpringcacheService
import grails.plugin.springcache.annotations.CacheFlush
import spock.lang.Specification

class FlushingAspectSpecification extends Specification {

	void "All specified caches are flushed"() {
		given: "a flushing aspect"
		def aspect = new FlushingAspect()
		aspect.springcacheService = Mock(SpringcacheService)

		when: "the aspect is triggered"
		def annotation = [value: {-> ["cache1", "cache2"] as String[] }] as CacheFlush
		aspect.flushCaches(annotation)

		then: "the specified caches are flushed"
		1 * aspect.springcacheService.flush(["cache1", "cache2"])
	}
}
