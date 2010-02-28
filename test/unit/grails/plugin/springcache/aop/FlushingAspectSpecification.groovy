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
import net.sf.ehcache.CacheManager
import net.sf.ehcache.Ehcache
import spock.lang.Specification

class FlushingAspectSpecification extends Specification {

	void "All specified caches are flushed"() {
		given: "some caches exist"
		def cache1 = Mock(Ehcache)
		def cache2 = Mock(Ehcache)
		def cacheManager = Mock(CacheManager)
		cacheManager.getCacheNames() >> {["cache1", "cache2", "cache3"] as String[]}
		cacheManager.getEhcache("cache1") >> cache1
		cacheManager.getEhcache("cache2") >> cache2

		when: "the flush aspect is triggered"
		def annotation = [value: {-> ["cache1", "cache2"] as String[] }] as CacheFlush
		def aspect = new FlushingAspect()
		aspect.cacheManager = cacheManager
		aspect.flushCaches(annotation)

		then: "the specified caches are flushed"
		1 * cache1.flush()
		1 * cache2.flush()
	}

	void "Cache names can be specified as regular expressions"() {
		given: "some caches exist"
		def cache1 = Mock(Ehcache)
		def cache2 = Mock(Ehcache)
		def cacheManager = Mock(CacheManager)
		cacheManager.getCacheNames() >> {["cache1", "cache2", "cache3"] as String[]}
		cacheManager.getEhcache("cache1") >> cache1
		cacheManager.getEhcache("cache2") >> cache2

		when: "the flush aspect is triggered"
		def annotation = [value: {-> [/cache[1-2]/] as String[] }] as CacheFlush
		def aspect = new FlushingAspect()
		aspect.cacheManager = cacheManager
		aspect.flushCaches(annotation)

		then: "the specified caches are flushed"
		1 * cache1.flush()
		1 * cache2.flush()
	}

}
