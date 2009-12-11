package grails.plugin.springcache.provider.simple;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import grails.plugin.springcache.AbstractCacheProvider;
import grails.plugin.springcache.CacheFacade;
import grails.plugin.springcache.CacheNotFoundException;
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

	public SimpleCachingModel createCachingModel(String id, Properties properties) {
		return new SimpleCachingModel(id, getRequiredProperty(properties, "cacheName"));
	}

	public SimpleFlushingModel createFlushingModel(String id, Properties properties) {
		List<String> cacheNames = Arrays.asList(StringUtils.split(getRequiredProperty(properties, "cacheNames"), ","));
		return new SimpleFlushingModel(id, cacheNames);
	}

}
