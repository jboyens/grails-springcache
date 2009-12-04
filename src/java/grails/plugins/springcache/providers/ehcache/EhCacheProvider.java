package grails.plugins.springcache.providers.ehcache;

import java.util.Collection;
import grails.plugins.springcache.cache.CacheFacade;
import grails.plugins.springcache.cache.CacheKey;
import grails.plugins.springcache.cache.CacheNotFoundException;
import grails.plugins.springcache.cache.CacheProvider;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class EhCacheProvider implements CacheProvider {

	private CacheManager cacheManager;

	public CacheFacade getCache(String cacheModelId) throws CacheNotFoundException {
		throw new UnsupportedOperationException();
	}

	public Collection<CacheFacade> getCaches(String flushModelId) throws CacheNotFoundException {
		throw new UnsupportedOperationException();
	}

	private CacheFacade getCacheByName(String name) {
		if (cacheManager.cacheExists(name)) {
			Cache cache = cacheManager.getCache(name);
			return new EhCacheFacade(cache);
		} else {
			throw new CacheNotFoundException(name);
		}
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	private static class EhCacheFacade implements CacheFacade {

		private final Cache cache;

		public EhCacheFacade(Cache cache) {
			this.cache = cache;
		}

		public boolean containsKey(CacheKey key) {
			return cache.isKeyInCache(key);
		}

		public Object get(CacheKey key) {
			Element element = cache.get(key);
			return element == null ? null : element.getValue();
		}

		public void put(CacheKey key, Object value) {
			Element element = new Element(key, value);
			cache.put(element);
		}

		public void flush() {
			cache.flush();
		}

		public String getName() {
			return cache.getName();
		}

		public Number getSize() {
			return cache.getStatistics().getObjectCount();
		}
	}
}
