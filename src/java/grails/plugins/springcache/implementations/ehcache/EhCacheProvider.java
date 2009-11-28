package grails.plugins.springcache.implementations.ehcache;

import grails.plugins.springcache.cache.CacheProvider;
import grails.plugins.springcache.cache.CacheFacade;
import grails.plugins.springcache.cache.CacheNotFoundException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Cache;

public class EhCacheProvider implements CacheProvider {

	private CacheManager cacheManager;

	public CacheFacade getCache(String name) {
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
}
