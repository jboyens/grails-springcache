package grails.plugin.springcache.providers.simple;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import grails.plugin.springcache.cache.CacheFacade;
import grails.plugin.springcache.cache.CacheKey;

class SimpleCacheFacade implements CacheFacade {

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
