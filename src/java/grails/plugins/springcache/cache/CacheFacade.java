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

/**
 * Decorates a cache instance from a particular provider.
 */
public interface CacheFacade {

	/**
	 * Returns true if the cache contains a value for the specified key, false otherwise.
	 */
	boolean containsKey(CacheKey key);

	/**
	 * Retrieves a value from the cache, returning null if no value exists for the specified key.
	 */
	Object get(CacheKey key);

	/**
	 * Stores a key, value combination in the cache.
	 */
	void put(CacheKey key, Object value);

	/**
	 * Flushes all key, value pairs from the cache leaving the cache empty.
	 */
	void flush();

	/**
	 * Returns the name of the cache.
	 */
	String getName();

	/**
	 * Returns the current cache size, i.e. the number of key, value pairs currently stored.
	 */
	Number getSize();
	
}
