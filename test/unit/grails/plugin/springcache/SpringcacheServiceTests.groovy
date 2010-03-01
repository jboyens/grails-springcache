package grails.plugin.springcache

import grails.test.GrailsUnitTestCase
import net.sf.ehcache.CacheManager
import net.sf.ehcache.Ehcache
import net.sf.ehcache.Element
import net.sf.ehcache.constructs.blocking.BlockingCache
import org.gmock.WithGMock
import org.hamcrest.Matcher
import static org.hamcrest.Matchers.*
import org.apache.commons.lang.ObjectUtils

@WithGMock
class SpringcacheServiceTests extends GrailsUnitTestCase {

	SpringcacheService service

	void setUp() {
		super.setUp()

		mockLogging SpringcacheService, true
		service = new SpringcacheService()
		service.springcacheCacheManager = mock(CacheManager) {
			cacheNames.returns(["cache1", "cache2", "cacheA", "cacheB"] as String[]).stub()
		}
	}

	void tearDown() {
		super.tearDown()
	}

	/**
	 * Creates a matcher that matches a EHCache Element with the specified key and objectValue.
	 */
	static Matcher<Element> element(Serializable key, Object objectValue) {
		allOf(
				instanceOf(Element),
				hasProperty("key", equalTo(key)),
				hasProperty("objectValue", equalTo(objectValue))
		)
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

	void testWithCacheRetrievesValueFromCacheIfFound() {
		def mockCache = mock(Ehcache) {
			get("key").returns(new Element("key", "value"))
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache)
		play {
			assertEquals "value", service.withCache("cache1", "key") {
				fail "Closure should not have been invoked"
			}
		}
	}

	void testWithCacheReturnsNullIfNullPlaceholderFoundInCache() {
		def mockCache = mock(Ehcache) {
			get("key").returns(new Element("key", ObjectUtils.NULL))
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache)
		play {
			assertNull service.withCache("cache1", "key") {
				fail "Closure should not have been invoked"
			}
		}
	}

	void testWithCacheStoresValueReturnedByClosureIfNotFound() {
		def mockCache = mock(Ehcache) {
			get("key").returns(null)
			put(element("key", "value"))
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache)
		play {
			assertEquals "value", service.withCache("cache1", "key") {
				return "value"
			}
		}
	}

	void testWithCacheStoresValueReturnedByClosureIfCacheElementExpired() {
		def mockElement = mock(Element) {
			isExpired().returns(true)
		}
		def mockCache = mock(Ehcache) {
			get("key").returns(mockElement)
			put(element("key", "value"))
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache)
		play {
			assertEquals "value", service.withCache("cache1", "key") {
				return "value"
			}
		}
	}

	void testWithCacheStoresNullPlaceholderIfClosureReturnsNull() {
		def mockElement = mock(Element) {
			isExpired().returns(true)
		}
		def mockCache = mock(Ehcache) {
			get("key").returns(mockElement)
			put(element("key", ObjectUtils.NULL))
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache)
		play {
			assertNull service.withCache("cache1", "key") {
				return null
			}
		}
	}

	void testWithCacheThrowsExceptionIfCacheNotFoundAndAutoCreateIsFalse() {
		service.springcacheCacheManager.getEhcache("cache1").returns(null)
		play {
			service.autoCreateCaches = false
			shouldFail(NoSuchCacheException) {
				assertEquals "value", service.withCache("cache1", "key") {
					fail "Closure should not have been invoked"
				}
			}
		}
	}

	void testWithCacheCreatesNewCacheIfCacheNotFoundAndAutoCreateIsTrue() {
		def mockCache = mock(Ehcache) {
			get("key").returns(null)
			put(element("key", "value"))
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(null)
		service.springcacheCacheManager.addCache("cache1")
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache)
		play {
			service.autoCreateCaches = true
			assertEquals "value", service.withCache("cache1", "key") {
				return "value"
			}
		}
	}

	void testWithBlockingCacheDoesNotDecorateCacheIfItIsABlockingCacheAlready() {
		def mockCache = mock(BlockingCache) {
			get("key").returns(new Element("key", "value"))
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache).times(2)
		play {
			assertEquals "value", service.withBlockingCache("cache1", "key") {
				fail "Closure should not have been invoked"
			}
		}
	}

	void testWithBlockingCacheDecoratesCacheBeforeUsingIfItIsNonBlocking() {
		def mockCache = mock(Ehcache)
		def blockingCache = mock(BlockingCache, constructor(sameInstance(mockCache))) {
			get("key").returns(new Element("key", "value"))
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache)
		service.springcacheCacheManager.replaceCacheWithDecoratedCache(sameInstance(mockCache), sameInstance(blockingCache))
		service.springcacheCacheManager.getEhcache("cache1").returns(blockingCache)
		play {
			assertEquals "value", service.withBlockingCache("cache1", "key") {
				fail "Closure should not have been invoked"
			}
		}
	}
}
