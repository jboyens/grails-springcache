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
package grails.plugin.springcache.cache;

import java.util.Collection;
import java.util.Properties;

/**
 * An interface decorating a specific caching implementation such as EHCache.
 */
public interface CacheProvider {

	/**
	 * Retrieves a cache based on a caching model.
	 */
	CacheFacade getCache(String cachingModelId) throws CacheNotFoundException, InvalidCachingModelException;

	/**
	 * Retrieves all the caches that should be flushed by a particular flushing model.
	 */
	Collection<CacheFacade> getCaches(String flushingModelId) throws CacheNotFoundException, InvalidFlushingModelException;

	/**
	 * Registers a caching model using properties from Grails configuration.
	 */
	void addCachingModel(String id, Properties properties);

	/**
	 * Registers a flushing model using properties from Grails configuration.
	 */
	void addFlushingModel(String id, Properties properties);

}
