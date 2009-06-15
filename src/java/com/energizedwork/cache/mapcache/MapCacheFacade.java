package com.energizedwork.cache.mapcache;

import java.beans.PropertyEditor;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springmodules.cache.CacheException;
import org.springmodules.cache.CachingModel;
import org.springmodules.cache.FatalCacheException;
import org.springmodules.cache.FlushingModel;
import org.springmodules.cache.provider.AbstractCacheProviderFacade;
import org.springmodules.cache.provider.CacheModelValidator;
import org.springmodules.cache.provider.ReflectionCacheModelEditor;

public class MapCacheFacade extends AbstractCacheProviderFacade {

	private final CacheModelValidator cacheModelValidator;
	private CacheManager cacheManager;

	public MapCacheFacade() {
		cacheModelValidator = new MapCacheModelValidator();
	}

	protected boolean isSerializableCacheElementRequired() {
		return false;
	}

	protected void onFlushCache(FlushingModel model) throws CacheException {
		for (MapCache cache : getCaches(model)) {
			cache.clear();
		}
	}

	protected Object onGetFromCache(Serializable key, CachingModel model) throws CacheException {
		MapCache cache = getCache(model);
		return cache.get(key);
	}

	protected void onPutInCache(Serializable key, CachingModel model, Object obj) throws CacheException {
		MapCache cache = getCache(model);
		cache.put(key, obj);
	}

	protected void onRemoveFromCache(Serializable key, CachingModel model) throws CacheException {
		MapCache cache = getCache(model);
		cache.remove(key);
	}

	protected void validateCacheManager() throws FatalCacheException {
		assertCacheManagerIsNotNull(cacheManager);
	}

	public CacheModelValidator modelValidator() {
		return cacheModelValidator;
	}

	public PropertyEditor getCachingModelEditor() {
		ReflectionCacheModelEditor editor = new ReflectionCacheModelEditor();
		editor.setCacheModelClass(MapCacheCachingModel.class);
		return editor;
	}

	public PropertyEditor getFlushingModelEditor() {
		Map<String, PropertyEditor> propertyEditors = new HashMap<String, PropertyEditor>();
		propertyEditors.put("cacheNames", new StringArrayPropertyEditor());

		ReflectionCacheModelEditor editor = new ReflectionCacheModelEditor();
		editor.setCacheModelClass(MapCacheFlushingModel.class);
		editor.setCacheModelPropertyEditors(propertyEditors);
		return editor;
	}

	public CacheManager getCacheManager() {
		return cacheManager;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	private MapCache getCache(CachingModel model) {
		MapCacheCachingModel mapCacheCachingModel = (MapCacheCachingModel)model;
		return cacheManager.getCache(mapCacheCachingModel.getCacheName());
	}

	private Iterable<MapCache> getCaches(FlushingModel model) {
		MapCacheFlushingModel mapCacheFlushingModel = (MapCacheFlushingModel)model;
		Collection<MapCache> caches = new HashSet<MapCache>();
		for (String cacheName : mapCacheFlushingModel.getCacheNames()) {
			caches.add(cacheManager.getCache(cacheName));
		}
		return caches;
	}

}
