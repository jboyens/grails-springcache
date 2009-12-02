package grails.plugins.springcache.providers.simple;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import grails.plugins.springcache.cache.CacheFacade;
import grails.plugins.springcache.cache.CacheKey;
import grails.plugins.springcache.cache.CacheNotFoundException;
import grails.plugins.springcache.cache.CacheProvider;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;

public class SimpleCacheProvider implements CacheProvider {

	@SuppressWarnings({"unchecked"})
	private Map<String, CacheFacade> caches = LazyMap.decorate(new ConcurrentHashMap(), new Transformer() {
		public Object transform(Object o) {
			return new SimpleCacheFacade(o.toString());
		}
	});

	public CacheFacade getCache(String name) throws CacheNotFoundException {
		return caches.get(name);
	}

	private static class SimpleCacheFacade implements CacheFacade {

		private final String name;
		private final Map<CacheKey, Object> map = new ConcurrentHashMap<CacheKey, Object>();

		public SimpleCacheFacade(String name) {
			this.name = name;
		}

		public boolean containsKey(CacheKey key) {
			return map.containsKey(key);
		}

		public Object get(CacheKey key) {
			return map.get(key);
		}

		public void put(CacheKey key, Object value) {
			map.put(key, value);
		}

		public void flush() {
			map.clear();
		}

		public String getName() {
			return name;
		}

		public Number getSize() {
			return map.size();
		}
	}
}
