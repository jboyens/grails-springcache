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
package grails.plugin.springcache.provider.ehcache

import grails.plugin.springcache.CacheConfigurationException
import grails.plugin.springcache.CacheNotFoundException
import grails.plugin.springcache.provider.ehcache.EhCacheCachingModel
import grails.plugin.springcache.provider.ehcache.EhCacheProvider
import net.sf.ehcache.CacheManager
import spock.lang.Specification
import grails.plugin.springcache.CacheConfigurationException

class EhCacheProviderSpecification extends Specification {

	void "Caching model configuration fails if required properties are missing"() {
		given: "An EHCache provider instance"
		def provider = new EhCacheProvider()

		when: "A caching model is added with invalid properties"
		provider.addCachingModel "modelId", new Properties()

		then: "An exception is thrown"
		thrown CacheConfigurationException
	}

	void "Flushing model configuration fails if required properties are missing"() {
		given: "An EHCache provider instance"
		def provider = new EhCacheProvider()

		when: "A caching model is added with invalid properties"
		provider.addFlushingModel "modelId", new Properties()

		then: "An exception is thrown"
		thrown CacheConfigurationException
	}

	void "Provider throws exception if named cache does not exist"() {
		given: "An EHCache provider instance exists"
		def provider = new EhCacheProvider()
		provider.cacheManager = Mock(CacheManager)
		provider.cacheManager.cacheExists("cacheName") >> false

		and: "A caching model is configured"
		provider.addCachingModel new EhCacheCachingModel("modelId", "cacheName")

		when: "The provider tries to find a cache that does not exist"
		provider.getCache "modelId"

		then: "An exception is thrown"
		thrown CacheNotFoundException
	}

}