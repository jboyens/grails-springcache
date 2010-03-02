package grails.plugin.springcache

import grails.spring.BeanBuilder
import grails.test.GrailsUnitTestCase
import net.sf.ehcache.CacheManager
import net.sf.ehcache.Ehcache
import net.sf.ehcache.Element
import net.sf.ehcache.constructs.blocking.BlockingCache
import net.sf.ehcache.constructs.blocking.LockTimeoutException
import org.apache.commons.lang.ObjectUtils
import org.gmock.WithGMock
import org.hamcrest.Matcher
import org.springframework.cache.ehcache.EhCacheFactoryBean
import static org.hamcrest.Matchers.*
import org.springframework.context.ApplicationContext

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

	/**
	 * Creates a matcher that matches an Ehcache instance with the specified name and config properties.
	 */
	static Matcher<Ehcache> configuredCache(String name, Map config) {
		def propertyMatchers = config.collect { k, v ->
			hasProperty(k, equalTo(v))
		}
		allOf(
				instanceOf(Ehcache),
				hasProperty("name", equalTo(name)),
				hasProperty("cacheConfiguration", allOf(propertyMatchers))
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

	void testFlushAllFlushesEverything() {
		["1", "2", "A", "B"].each {
			def mockCache = mock(Ehcache) {
				flush()
			}
			service.springcacheCacheManager.getEhcache("cache$it").returns(mockCache)
		}
		play {
			service.flushAll()
		}
	}

	void testFlushAllClearsStatisticsAsWellIfTrueIsPassed() {
		["1", "2", "A", "B"].each {
			def mockCache = mock(Ehcache) {
				flush()
				clearStatistics()
			}
			service.springcacheCacheManager.getEhcache("cache$it").returns(mockCache)
		}
		play {
			service.flushAll(true)
		}
	}

	void testWithCacheRetrievesValueFromCacheIfFound() {
		def mockCache = mock(Ehcache) {
			get("key").returns(new Element("key", "value"))
			name.returns("cache1").stub()
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache)
		play {
			assertEquals "value", service.doWithCache("cache1", "key") {
				fail "Closure should not have been invoked"
			}
		}
	}

	void testWithCacheReturnsNullIfNullPlaceholderFoundInCache() {
		def mockCache = mock(Ehcache) {
			get("key").returns(new Element("key", ObjectUtils.NULL))
			name.returns("cache1").stub()
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache)
		play {
			assertNull service.doWithCache("cache1", "key") {
				fail "Closure should not have been invoked"
			}
		}
	}

	void testWithCacheStoresValueReturnedByClosureIfNotFound() {
		def mockCache = mock(Ehcache) {
			get("key").returns(null)
			put(element("key", "value"))
			name.returns("cache1").stub()
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache)
		play {
			assertEquals "value", service.doWithCache("cache1", "key") {
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
			name.returns("cache1").stub()
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache)
		play {
			assertEquals "value", service.doWithCache("cache1", "key") {
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
			name.returns("cache1").stub()
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache)
		play {
			assertNull service.doWithCache("cache1", "key") {
				return null
			}
		}
	}

	void testWithCacheThrowsExceptionIfCacheNotFoundAndAutoCreateIsFalse() {
		service.springcacheCacheManager.getEhcache("cache1").returns(null)
		play {
			service.autoCreateCaches = false
			shouldFail(NoSuchCacheException) {
				assertEquals "value", service.doWithCache("cache1", "key") {
					fail "Closure should not have been invoked"
				}
			}
		}
	}

	void testWithCacheCreatesNewCacheIfCacheNotFoundAndAutoCreateIsTrue() {
		def mockCache = mock(Ehcache) {
			get("key").returns(null)
			put(element("key", "value"))
			name.returns("cache1").stub()
		}
		def mockApplicationContext = mock(ApplicationContext) {
			getBean("cache1").returns(mockCache)
		}
		mock(BeanBuilder, constructor(anything())) {
			beans(anything())
			createApplicationContext().returns(mockApplicationContext)
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(null)
		play {
			service.autoCreateCaches = true
			assertEquals "value", service.doWithCache("cache1", "key") {
				return "value"
			}
		}
	}

	void testWithBlockingCacheDoesNotDecorateCacheIfItIsABlockingCacheAlready() {
		def mockCache = mock(BlockingCache) {
			get("key").returns(new Element("key", "value"))
			name.returns("cache1").stub()
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache)
		play {
			assertEquals "value", service.doWithBlockingCache("cache1", "key") {
				fail "Closure should not have been invoked"
			}
		}
	}

	void testWithBlockingCacheDecoratesCacheBeforeUsingIfItIsNonBlocking() {
		def mockCache = mock(Ehcache)
		def blockingCache = mock(BlockingCache, constructor(sameInstance(mockCache))) {
			get("key").returns(new Element("key", "value"))
			name.returns("cache1").stub()
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache)
		service.springcacheCacheManager.replaceCacheWithDecoratedCache(sameInstance(mockCache), sameInstance(blockingCache))
		play {
			assertEquals "value", service.doWithBlockingCache("cache1", "key") {
				fail "Closure should not have been invoked"
			}
		}
	}

	void testWithBlockingCacheClearsLockIfExceptionIsThrownFromClosure() {
		def mockCache = mock(BlockingCache) {
			get("key").returns(null)
			put(element("key", null))
			name.returns("cache1").stub()
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache)
		play {
			shouldFail(RuntimeException) {
				service.doWithBlockingCache("cache1", "key") {
					throw new RuntimeException("thrown to test exception handling")
				}
			}
		}
	}

	void testWithBlockingCacheDoesNotTryToClearLockIfItNeverAcquiresIt() {
		def mockCache = mock(BlockingCache) {
			get("key").raises(new LockTimeoutException("thrown if get() blocks too long"))
			name.returns("cache1").stub()
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache)
		play {
			shouldFail(LockTimeoutException) {
				service.doWithBlockingCache("cache1", "key") {
					fail "Closure should not have been invoked"
				}
			}
		}
	}

	void testDoWithCacheDelegatesToDoWithBlockingCacheIfItFindsABlockingCache() {
		def mockCache = mock(BlockingCache) {
			get("key").returns("value")
			put(element("key", null))
			name.returns("cache1").stub()
		}
		service.springcacheCacheManager.getEhcache("cache1").returns(mockCache).times(2)
		play {
			shouldFail(RuntimeException) {
				service.doWithCache("cache1", "key") {
					throw new RuntimeException("thrown to test exception handling")
				}
			}
		}
	}
}
