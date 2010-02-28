package grails.plugin.springcache

import grails.test.GrailsUnitTestCase
import net.sf.ehcache.CacheManager
import net.sf.ehcache.Ehcache
import org.gmock.WithGMock
import static org.hamcrest.Matchers.*
import net.sf.ehcache.Element

@WithGMock
class SpringcacheServiceTests extends GrailsUnitTestCase {

	SpringcacheService service

	void setUp() {
		super.setUp()

		mockLogging SpringcacheService
		service = new SpringcacheService()
		service.springcacheCacheManager = mock(CacheManager) {
			cacheNames.returns(["cache1", "cache2", "cacheA", "cacheB"] as String[]).stub()
		}
	}

	void tearDown() {
		super.tearDown()
	}

	void testCachesCanBeFlushedIndividually() {
		def mockCache = mock(Ehcache) {
			flush()
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache)
		play {
			service.flush("cache1")
		}
	}

	void testMultipleCachesCanBeFlushedAtOnce() {
		def mockCache1 = mock(Ehcache) {
			flush()
		}
		def mockCache2 = mock(Ehcache) {
			flush()
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache1)
		service.springcacheCacheManager.getEhcache("cache2").returns(mockCache2)
		play {
			service.flush(["cache1", "cache2"])
		}
	}

	void testCachesCanBeFlushedByPattern() {
		def mockCache1 = mock(Ehcache) {
			flush()
		}
		def mockCache2 = mock(Ehcache) {
			flush()
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache1)
		service.springcacheCacheManager.getEhcache("cache2").returns(mockCache2)
		play {
			service.flush(/cache[\d]/)
		}
	}

	void testInvalidCacheNamesAreHandled() {
		play {
			service.flush("cacheZ")
		}
	}

	void testExceptionsOnFlushAreHandled() {
		def mockCache1 = mock(Ehcache) {
			flush().raises(new IllegalStateException("this would happen if cache was not alive"))
		}
		def mockCache2 = mock(Ehcache) {
			flush()
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache1)
		service.springcacheCacheManager.getEhcache("cache2").returns(mockCache2)
		play {
			service.flush(["cache1", "cache2"])
		}
	}

	void testElementsStoredInTheNamedCache() {
		def mockCache = mock(Ehcache) {
			put(allOf(hasProperty("key", equalTo("key")), hasProperty("objectValue", equalTo("value"))))
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache)
		play {
			service.put("cache1", "key", "value")
		}
	}

	void testExceptionThrownIfCacheNotFoundAndAutoCreateIsFalse() {
		service.springcacheCacheManager.getEhcache("cache1").returns(null)
		play {
			service.autoCreateCaches = false
			shouldFail(NoSuchCacheException) {
				service.put("cache1", "key", "value")
			}
		}
	}

	void testObjectsRetrievedFromCache() {
		def mockCache = mock(Ehcache) {
			get("key").returns(new Element("key", "value"))
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache)
		play {
			assertEquals "value", service.get("cache1", "key")
		}
	}

	void testGetReturnsNullIfNoElementInCache() {
		def mockCache = mock(Ehcache) {
			get("key").returns(null)
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache)
		play {
			assertNull service.get("cache1", "key")
		}
	}

	void testGetReturnsNullIfCacheElementExpired() {
		def mockElement = mock(Element) {
			isExpired().returns(true)
		}
		def mockCache = mock(Ehcache) {
			get("key").returns(mockElement)
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache)

		play {
			assertNull service.get("cache1", "key")
		}
	}

	void testGetThrowsExceptionIfInvalidCacheNameUsed() {
		service.springcacheCacheManager.getEhcache("cache1").returns(null)
		play {
			service.autoCreateCaches = false
			shouldFail(NoSuchCacheException) {
				service.get("cache1", "key")
			}
		}
	}

	void testNewCacheAddedIfCacheNotFoundAndAutoCreateIsTrue() {
		def mockCache = mock(Ehcache) {
			put(allOf(hasProperty("key", equalTo("key")), hasProperty("objectValue", equalTo("value"))))
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(null)
		service.springcacheCacheManager.addCache("cache1")
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache)
		play {
			service.autoCreateCaches = true
			service.put("cache1", "key", "value")
		}
	}
}
