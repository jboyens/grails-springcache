package com.energizedwork.cache.mapcache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// TODO: this should not be package-protected not public, but http://jira.codehaus.org/browse/GRAILS-4120
public class MapCache {

	private static final Log log = LogFactory.getLog(MapCache.class);

	private final Map<Object, MapCacheEntry> delegate;
	private final long ttl;
	private final int maxSize;

	MapCache(Map<Object, MapCacheEntry> delegate, long ttl, int maxSize) {
		this.delegate = delegate;
		this.ttl = ttl;
		this.maxSize = maxSize;
	}

	public Object get(Object key) {
		MapCacheEntry cacheEntry = delegate.get(key);
		return cacheEntry == null ? null : cacheEntry.getValue();
	}

	public void put(final Object key, Object value) {
		delegate.put(key, new MapCacheEntry(value, ttl));
	}

	public void remove(final Object key) {
		delegate.remove(key);
	}

	public void clear() {
		delegate.clear();
	}

	public int size() {
		return delegate.size();
	}

	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	private static final Comparator<Map.Entry<Object, MapCacheEntry>> SORT_OLDEST_FIRST = new Comparator<Map.Entry<Object, MapCacheEntry>>() {
		public int compare(Map.Entry<Object, MapCacheEntry> o1, Map.Entry<Object, MapCacheEntry> o2) {
			long c1 = o1.getValue().getCreated();
			long c2 = o2.getValue().getCreated();
			if (c1 < c2) return -1;
			else if (c1 == c2) return 0;
			return 1;
		}
	};

	/**
	 * This method is synchronized to prevent the scheduledEvictor and onDemandEvictors from attempting to call it
	 * at the same time.
	 * TODO: synchronize using a semaphore
	 */
	synchronized void evict() {
		// sort cache entries oldest first
		// TODO: if 2 entries are created in the same millisecond this doesn't preserve strict insertion order
		List<Map.Entry<Object, MapCacheEntry>> entries = new ArrayList<Map.Entry<Object, MapCacheEntry>>(delegate.entrySet());
		Collections.sort(entries, SORT_OLDEST_FIRST);

		// evict entries if cache is over max size or entry is expired
		for (final Map.Entry<Object, MapCacheEntry> entry : entries) {
			// TODO: if another put of this key has just happened we're removing a value that is no longer expired, but then why would it have happened if the key existed?
			if (delegate.size() > maxSize) {
				if (log.isDebugEnabled()) {
					log.debug(String.format("Evicting entry %s from cache to maintain max size", entry.getKey()));
				}
				delegate.remove(entry.getKey());
			} else if (entry.getValue().isExpired()) {
				if (log.isDebugEnabled()) {
					log.debug(String.format("Evicting expired entry %s from cache", entry.getKey()));
				}
				delegate.remove(entry.getKey());
			}
		}
	}

}