package grails.plugins.springcache.providers.ehcache;

import grails.plugins.springcache.cache.CacheFacade;
import grails.plugins.springcache.cache.CacheKey;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

class EhCacheFacade implements CacheFacade {

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
