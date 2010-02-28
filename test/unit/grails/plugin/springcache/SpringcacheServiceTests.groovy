package grails.plugin.springcache

import grails.test.GrailsUnitTestCase
import net.sf.ehcache.CacheManager
import net.sf.ehcache.Ehcache
import org.gmock.WithGMock
import static org.hamcrest.Matchers.*
import net.sf.ehcache.Element
import net.sf.ehcache.constructs.blocking.BlockingCache
import net.sf.ehcache.Cache

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

	void testFlushAcceptsIndividualCacheNames() {
		def mockCache = mock(Ehcache) {
			flush()
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache)
		play {
			service.flush("cache1")
		}
	}

	void testFlushAcceptsMultipleCacheNames() {
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

	void testFlushAcceptsCacheNamePatterns() {
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

	void testFlushIgnoresInvalidCacheNames() {
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

	void testPutStoresElementsInTheNamedCache() {
		def mockCache = mock(Ehcache) {
			put(allOf(hasProperty("key", equalTo("key")), hasProperty("objectValue", equalTo("value"))))
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache)
		play {
			service.put("cache1", "key", "value")
		}
	}

	void testPutThrowsExceptionIfCacheNotFoundAndAutoCreateIsFalse() {
		service.springcacheCacheManager.getEhcache("cache1").returns(null)
		play {
			service.autoCreateCaches = false
			shouldFail(NoSuchCacheException) {
				service.put("cache1", "key", "value")
			}
		}
	}

	void testPutCreatesNewCacheAddedIfCacheNotFoundAndAutoCreateIsTrue() {
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

	void testGetRetrievesValueFromNamedCache() {
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

	void testGetThrowsExceptionIfInvalidCacheNameUsedAndAutoCreateIsFalse() {
		service.springcacheCacheManager.getEhcache("cache1").returns(null)
		play {
			service.autoCreateCaches = false
			shouldFail(NoSuchCacheException) {
				service.get("cache1", "key")
			}
		}
	}

	void testGetCreatesNewCacheAddedIfCacheNotFoundAndAutoCreateIsTrue() {
		def mockCache = mock(Ehcache) {
			get("key").returns(null)
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(null)
		service.springcacheCacheManager.addCache("cache1")
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache)
		play {
			service.autoCreateCaches = true
			assertNull service.get("cache1", "key")
		}
	}

	void testEnsureCacheIsBlockingDoesNothingIfCacheIsBlockingAlready() {
		def mockCache = mock(BlockingCache)
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache)
		play {
			service.ensureCacheIsBlocking("cache1")
		}
	}

	void testEnsureCacheIsBlockingWrapsCacheIfNotBlocking() {
		def mockCache = mock(Ehcache)
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache)
		def blockingCache = mock(BlockingCache, constructor(sameInstance(mockCache)))
		service.springcacheCacheManager.replaceCacheWithDecoratedCache(sameInstance(mockCache), sameInstance(blockingCache))
		play {
			service.ensureCacheIsBlocking("cache1")
		}
	}

	void testEnsureCacheIsBlockingThrowsExceptionIfInvalidCacheNameUsedAndAutoCreateIsFalse() {
		service.springcacheCacheManager.getEhcache("cache1").returns(null)
		play {
			service.autoCreateCaches = false
			shouldFail(NoSuchCacheException) {
				service.ensureCacheIsBlocking("cache1")
			}
		}
	}

	void testEnsureCacheIsBlockingCreatesNewCacheAddedIfCacheNotFoundAndAutoCreateIsTrue() {
		def mockCache = mock(BlockingCache)
		service.springcacheCacheManager.getEhcache("cache1").returns(null)
		service.springcacheCacheManager.addCache("cache1")
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache)
		play {
			service.autoCreateCaches = true
			service.ensureCacheIsBlocking("cache1")
		}
	}

}
