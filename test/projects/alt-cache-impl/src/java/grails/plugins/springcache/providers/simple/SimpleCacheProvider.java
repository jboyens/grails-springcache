package grails.plugins.springcache.providers.simple;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import grails.plugins.springcache.cache.AbstractCacheProvider;
import grails.plugins.springcache.cache.CacheFacade;
import grails.plugins.springcache.cache.CacheNotFoundException;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.lang.StringUtils;

public class SimpleCacheProvider extends AbstractCacheProvider<SimpleCachingModel, SimpleFlushingModel> {

	@SuppressWarnings({"unchecked"})
	final Map<String, CacheFacade> caches = LazyMap.decorate(new ConcurrentHashMap(), new Transformer() {
		public Object transform(Object o) {
			return new SimpleCacheFacade(o.toString());
		}
	});

	public CacheFacade getCache(SimpleCachingModel cachingModel) throws CacheNotFoundException {
		String cacheName = cachingModel.getCacheName();
		return caches.get(cacheName);
	}

	public Collection<CacheFacade> getCaches(SimpleFlushingModel flushingModel) throws CacheNotFoundException {
		Collection<CacheFacade> cachesToFlush = new ArrayList<CacheFacade>();
		for (String cacheName : flushingModel.getCacheNames()) {
			cachesToFlush.add(caches.get(cacheName));
		}
		return Collections.unmodifiableCollection(cachesToFlush);
	}

	public void addCachingModel(String id, Properties properties) {
		cachingModels.put(id, new SimpleCachingModel(id, getRequiredProperty(properties, "cacheName")));
	}

	public void addFlushingModel(String id, Properties properties) {
		List<String> cacheNames = Arrays.asList(StringUtils.split(getRequiredProperty(properties, "cacheNames"), ","));
		flushingModels.put(id, new SimpleFlushingModel(id, cacheNames));
	}

}
