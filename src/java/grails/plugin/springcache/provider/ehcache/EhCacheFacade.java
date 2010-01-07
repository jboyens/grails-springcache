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
package grails.plugin.springcache.provider.ehcache;

import grails.plugin.springcache.CacheFacade;
import grails.plugin.springcache.CacheKey;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

class EhCacheFacade implements CacheFacade {

	private final Cache cache;

	public EhCacheFacade(Cache cache) {
		this.cache = cache;
	}

	public Object get(CacheKey key) {
		Element element = cache.get(key);
		if (element == null || cache.isExpired(element)) return null;
		return element.getValue();
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
