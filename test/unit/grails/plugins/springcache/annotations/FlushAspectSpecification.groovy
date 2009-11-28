package grails.plugins.springcache.annotations

import spock.lang.Specification
import org.gmock.WithGMock
import grails.plugins.springcache.cache.*

@WithGMock
class FlushAspectSpecification extends Specification {

	void "All specfied caches are flushed"() {
		given: "some caches exist"
		def cache1 = mock(Cache)
		def cache2 = mock(Cache)
		def cacheManager = mock(CacheManager) {
			getCache("cache1").returns(cache1)
			getCache("cache2").returns(cache2)
		}
		def aspect = new FlushAspect()
		aspect.cacheManager = cacheManager

		when: "the flush aspect is triggered"
		cache1.flush()
		cache2.flush()
		def annotation = [cacheNames: {-> ["cache1", "cache2"] as String[] }] as CacheFlush
		play {
			aspect.flushCaches(annotation)
		}

		then: "whatever"
	}

}