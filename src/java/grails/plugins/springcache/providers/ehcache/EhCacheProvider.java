package grails.plugins.springcache.providers.ehcache;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import grails.plugins.springcache.cache.CacheFacade;
import grails.plugins.springcache.cache.CacheNotFoundException;
import grails.plugins.springcache.cache.CacheProvider;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

public class EhCacheProvider implements CacheProvider {

	private CacheManager cacheManager;
	private final Map<String, EhCacheCachingModel> cachingModels = new HashMap<String, EhCacheCachingModel>();
	private final Map<String, EhCacheFlushingModel> flushingModels = new HashMap<String, EhCacheFlushingModel>();

	public CacheFacade getCache(String cacheModelId) throws CacheNotFoundException {
		EhCacheCachingModel cachingModel = cachingModels.get(cacheModelId);
		return getCacheByName(cachingModel.getCacheName());
	}

	public Collection<CacheFacade> getCaches(String flushModelId) throws CacheNotFoundException {
		EhCacheFlushingModel flushingModel = flushingModels.get(flushModelId);
		Collection<CacheFacade> caches = new HashSet<CacheFacade>();
		for (String cacheName : flushingModel.getCacheNames()) {
			caches.add(getCacheByName(cacheName));
		}
		return caches;
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

	public void addCachingModel(String id, EhCacheCachingModel cachingModel) {
		cachingModels.put(id, cachingModel);
	}

	public void addFlushingModel(String id, EhCacheFlushingModel flushingModel) {
		flushingModels.put(id, flushingModel);
	}
}
