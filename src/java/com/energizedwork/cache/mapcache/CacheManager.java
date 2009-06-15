package com.energizedwork.cache.mapcache;

import static java.lang.String.format;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.collections.Factory;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class CacheManager implements InitializingBean, DisposableBean {

	private static final Log log = LogFactory.getLog(CacheManager.class);

	public static final long DEFAULT_TTL = SECONDS.toMillis(600); // 10 minutes
	public static final long DEFAULT_EVICT_FREQUENCY = SECONDS.toMillis(60); // 1 minute
	public static final int DEFAULT_MAX_SIZE = 1000;

	private final Runnable evictionStrategy = new Runnable() {
		public void run() {
			if (log.isTraceEnabled()) {
				log.trace("Running eviction strategy");
			}
			for (Map.Entry<String, MapCache> entry : caches.entrySet()) {
				if (log.isTraceEnabled()) {
					log.trace(format("Evicting cache %s", entry.getKey()));
				}
				entry.getValue().evict();
			}
		}
	};

	private final Factory cacheFactory = new Factory() {
		public Object create() {
			return new MapCache(new ConcurrentHashMap<Object, MapCacheEntry>(), ttl, maxSize);
		}
	};

	@SuppressWarnings("unchecked")
	private final Map<String, MapCache> caches = ConcurrentLazyMap.decorate(new HashMap<String, MapCache>(), cacheFactory);

	private long ttl;
	private long evictFrequency;
	private int maxSize;
	private ScheduledExecutorService scheduledEvictor;
	private ExecutorService onDemandEvictor;

	public CacheManager() {
		ttl = DEFAULT_TTL;
		evictFrequency = DEFAULT_EVICT_FREQUENCY;
		maxSize = DEFAULT_MAX_SIZE;
	}

	public MapCache getCache(String cacheName) {
		return caches.get(cacheName);
	}

	public void afterPropertiesSet() {
		if (log.isDebugEnabled()) {
			log.debug(format("Initializing %s with ttl=%dms, evictFrequency=%dms, maxSize=%d",
					this.getClass().getSimpleName(), ttl, evictFrequency, maxSize));
		}

		onDemandEvictor = Executors.newSingleThreadExecutor();
		scheduledEvictor = Executors.newScheduledThreadPool(1);
		scheduledEvictor.scheduleAtFixedRate(evictionStrategy, evictFrequency, evictFrequency, MILLISECONDS);
	}

	public void destroy() throws Exception {
		if (log.isDebugEnabled()) {
			log.debug(format("Shutting down %s", this.getClass().getSimpleName()));
		}
		scheduledEvictor.shutdownNow();
		onDemandEvictor.shutdownNow();
	}

	/**
	 * Forces the evicition strategy to execute and blocks until it completes.
	 *
	 * @throws ExecutionException   if an exception is thrown by the eviction strategy.
	 * @throws InterruptedException if the current thread is interrupted whilst waiting for the eviction strategy to finish executing.
	 */
	void forceEvictAndWait() throws ExecutionException, InterruptedException {
		Future f = onDemandEvictor.submit(evictionStrategy);
		f.get();
	}

	/**
	 * Forces the eviction strategy to execute but does <em>not</em> wait for it to complete.
	 */
	void forceEvict() {
		onDemandEvictor.submit(evictionStrategy);
	}

	public long getTimeToLive() {
		return ttl;
	}

	public void setTimeToLive(long ttl) {
		this.ttl = ttl;
	}

	public long getEvictFrequency() {
		return evictFrequency;
	}

	public void setEvictFrequency(long evictFrequency) {
		this.evictFrequency = evictFrequency;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	/**
	 * An extension of <code>LazyMap</code> that can be used without a synchronized wrapper in contexts where values are
	 * not overwritten once initialized. It uses a <code>ReentrantLock</code> to ensure initialization of each key only
	 * happens once. General use of <code>get</code> is unsynchronized.
	 */
	private static class ConcurrentLazyMap extends LazyMap {

		private final Lock lock = new ReentrantLock();

		public static Map decorate(Map map, Factory factory) {
			return new ConcurrentLazyMap(map, factory);
		}

		private ConcurrentLazyMap(Map map, Factory factory) {
			super(map, factory);
		}

		public Object get(Object key) {
			if (!map.containsKey(key)) {
				return getOrInitialize(key);
			}
			return map.get(key);
		}

		@SuppressWarnings("unchecked")
		private Object getOrInitialize(Object key) {
			// this is effectively like a double-checked lock but using a Lock object instead of synchronizing this method
			lock.lock();
			try {
				if (!map.containsKey(key)) {
					Object value = factory.transform(key);
					map.put(key, value);
					return value;
				}
			} finally {
				lock.unlock();
			}
			return map.get(key);
		}
	}
}
