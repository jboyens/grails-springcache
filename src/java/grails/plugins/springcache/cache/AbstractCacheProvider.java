/*
 * Copyright 2009 Rob Fletcher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugins.springcache.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * An abstract base class for cache provider implementations. Implementations just need to define how caching and
 * flushing models are created based on configuration properties and how caches are retrieved based on models.
 *
 * @param <C> The specific CachingModel implementation used by the provider.
 * @param <F> The specific FlushingModel implementation used by the provider.
 */
public abstract class AbstractCacheProvider<C extends CachingModel, F extends FlushingModel> implements CacheProvider {

	protected final Map<String, C> cachingModels = new HashMap<String, C>();
	protected final Map<String, F> flushingModels = new HashMap<String, F>();

	/**
	 * Implementations should create a new CachingModel, applying the specified properties and then call {@link
	 * #addCachingModel(CachingModel)}.
	 */
	public abstract void addCachingModel(String id, Properties properties);

	/**
	 * Implementations should create a new FlushingModel, applying the specified properties and then call {@link
	 * #addFlushingModel(FlushingModel)}.
	 */
	public abstract void addFlushingModel(String id, Properties properties);

	/**
	 * Implementations should return the cache associated with the specified CachingModel.
	 */
	protected abstract CacheFacade getCache(C cachingModel);

	/**
	 * Implementations should return the caches associated with the specified FlushingModel.
	 */
	protected abstract Collection<CacheFacade> getCaches(F flushingModel);

	public final void addCachingModel(C cachingModel) {
		cachingModels.put(cachingModel.getId(), cachingModel);
	}

	public final void addFlushingModel(F flushingModel) {
		flushingModels.put(flushingModel.getId(), flushingModel);
	}

	public final CacheFacade getCache(String cachingModelId) throws CacheNotFoundException {
		C cachingModel = cachingModels.get(cachingModelId);
		if (cachingModel == null) throw new InvalidCachingModelException(cachingModelId);
		return getCache(cachingModel);
	}

	public final Collection<CacheFacade> getCaches(String flushingModelId) throws CacheNotFoundException {
		F flushingModel = flushingModels.get(flushingModelId);
		if (flushingModel == null) throw new InvalidFlushingModelException(flushingModelId);
		return getCaches(flushingModel);
	}

	protected final String getRequiredProperty(Properties properties, String propertyName) {
		String cacheName = properties.getProperty(propertyName);
		if (cacheName == null)
			throw new CacheConfigurationException(String.format("Required property %s not found in %s", propertyName, properties));
		return cacheName;
	}

}
