package com.energizedwork.cache.mapcache

import static com.energizedwork.common.util.CurrentTimeUtils.withOffsetCurrentTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class MapCacheTests extends GroovyTestCase {

	CacheManager cacheManager
	MapCache cache

	void setUp() {
		cacheManager = new CacheManager()
		cacheManager.maxSize = 2
		cacheManager.afterPropertiesSet()

		cache = cacheManager.getCache(MapCacheTests.name)
	}

	void tearDown() {
		cacheManager.destroy()
	}

	void testValueCanBePutGotAndRemoved() {
		assertNull cache.get('word')
		assertEquals(0, cache.size())

		cache.put('word', 'catflap')
		assertEquals('catflap', cache.get('word'))
		assertEquals(1, cache.size())

		cache.put('word', 'rubberplant')
		assertEquals('rubberplant', cache.get('word'))
		assertEquals(1, cache.size())

		cache.remove('word')
		assertNull cache.get('word')
		assertEquals(0, cache.size())

		cache.put('word', 'marzipan')
		assertEquals(1, cache.size())
	}

	void testNullKeysAreNotPermitted() {
		shouldFail(NullPointerException) {
			cache.get(null)
		}
		shouldFail(NullPointerException) {
			cache.put(null, 'thing')
		}
		assertEquals(0, cache.size())
		shouldFail(NullPointerException) {
			cache.remove(null)
		}
		assertEquals(0, cache.size())
	}

	void testNullValuesAreNotPermitted() {
		shouldFail(NullPointerException) {
			cache.put('thing', null)
		}
		assertEquals(0, cache.size())
	}

	void testCacheEntriesAreEvictedWhenTheyExpire() {
		cache.put('word', 'marzipan')
		assertEquals('marzipan', cache.get('word'))

		withOffsetCurrentTime(cacheManager.timeToLive) {
			cacheManager.forceEvictAndWait()
		}

		assertNull cache.get('word')
		assertEquals(0, cache.size())
	}

	void testCacheEntriesAreEvictedToMaintainMaxSize() {
		cache.put('word1', 'catflap')
		// evict doesn't preserve insertion order for objects inserted in same millisecond
		withOffsetCurrentTime(1) {
			cache.put('word2', 'rubberplant')
		}
		withOffsetCurrentTime(2) {
			cache.put('word3', 'marzipan')
		}
		assertEquals('catflap', cache.get('word1'))
		assertEquals('rubberplant', cache.get('word2'))
		assertEquals('marzipan', cache.get('word3'))
		assertEquals(3, cache.size())

		cacheManager.forceEvictAndWait()

		assertNull cache.get('word1')
		assertEquals('rubberplant', cache.get('word2'))
		assertEquals('marzipan', cache.get('word3'))
		assertEquals(2, cache.size())
	}

	void testEvictionRunsOnSchedule() {
		// TODO: hacky, can this be better?
		cacheManager.destroy()
		cacheManager.evictFrequency = 250
		cacheManager.afterPropertiesSet()

		cache.put('word', 'catflap')

		withOffsetCurrentTime(cacheManager.timeToLive) {
			waitForCondition('cache empty', 1, TimeUnit.SECONDS) {
				cache.isEmpty()
			}
		}

		// cache entry should have been evicted
		assertNull cache.get('word')
		assertEquals(0, cache.size())

		// repeat to prove the eviction doesn't only fire once
		cache.put('word', 'rubberplant')

		withOffsetCurrentTime(cacheManager.timeToLive) {
			waitForCondition('cache empty', 1, TimeUnit.SECONDS) {
				cache.isEmpty()
			}
		}

		assertNull cache.get('word')
		assertEquals(0, cache.size())
	}

	private static void waitForCondition(String message, long timeout, TimeUnit unit, Closure closure) {
		CountDownLatch latch = new CountDownLatch(1)

		Thread.start {
			while (!closure.call()) {
				println '(-.-)zzZ'
				Thread.sleep(10)
			}
			println '\\o/'
			latch.countDown()
		}

		assertTrue "Timed out waiting for: $message", latch.await(timeout, unit)
	}
}

