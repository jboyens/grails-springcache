package com.energizedwork.cache.mapcache;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springmodules.cache.provider.AbstractCacheModelValidator;
import org.springmodules.cache.provider.InvalidCacheModelException;

class MapCacheModelValidator extends AbstractCacheModelValidator {

	protected Class getCachingModelTargetClass() {
		return MapCacheCachingModel.class;
	}

	protected Class getFlushingModelTargetClass() {
		return MapCacheFlushingModel.class;
	}

	protected void validateCachingModelProperties(Object cachingModel)
			throws InvalidCacheModelException {
		MapCacheCachingModel model = (MapCacheCachingModel) cachingModel;
		if (!StringUtils.hasText(model.getCacheName())) {
			throw new InvalidCacheModelException("Cache name should not be empty");
		}
	}

	protected void validateFlushingModelProperties(Object flushingModel)
			throws InvalidCacheModelException {
		MapCacheFlushingModel model = (MapCacheFlushingModel) flushingModel;
		String[] cacheNames = model.getCacheNames();

		if (ObjectUtils.isEmpty(cacheNames)) {
			throw new InvalidCacheModelException("There should be at least one cache name");
		}
	}
}
