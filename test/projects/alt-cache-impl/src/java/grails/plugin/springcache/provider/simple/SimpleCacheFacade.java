package grails.plugin.springcache.provider.simple;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import grails.plugin.springcache.CacheFacade;
import grails.plugin.springcache.CacheKey;

class SimpleCacheFacade implements CacheFacade {

	private final String name;
	private final Map<CacheKey, Object> map = new ConcurrentHashMap<CacheKey, Object>();

	public SimpleCacheFacade(String name) {
		this.name = name;
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
