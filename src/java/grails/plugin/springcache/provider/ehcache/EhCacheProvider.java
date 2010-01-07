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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import grails.plugin.springcache.AbstractCacheProvider;
import grails.plugin.springcache.CacheFacade;
import grails.plugin.springcache.CacheNotFoundException;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.apache.commons.lang.StringUtils;

public class EhCacheProvider extends AbstractCacheProvider<EhCacheCachingModel, EhCacheFlushingModel> {

	private CacheManager cacheManager;
	private boolean createCachesOnDemand;

	protected CacheFacade getCache(EhCacheCachingModel cachingModel) {
		return getCacheByName(cachingModel.getCacheName());
	}

	protected Collection<CacheFacade> getCaches(EhCacheFlushingModel flushingModel) {
		Collection<CacheFacade> caches = new HashSet<CacheFacade>();
		for (String cacheName : flushingModel.getCacheNames()) {
			caches.add(getCacheByName(cacheName));
		}
		return caches;
	}

	private CacheFacade getCacheByName(String name) {
		if (createCachesOnDemand && !cacheManager.cacheExists(name)) {
			log.info(String.format("No cache named '%s' found - creating using defaults", name));
			cacheManager.addCache(name);
		}		

		if (cacheManager.cacheExists(name)) {
			Cache cache = cacheManager.getCache(name);
			return new EhCacheFacade(cache);
		} else {
			throw new CacheNotFoundException(name);
		}
	}

	public EhCacheCachingModel createCachingModel(String id, Properties properties) {
		String cacheName = getRequiredProperty(properties, "cacheName");
		return new EhCacheCachingModel(id, cacheName);
	}

	public EhCacheFlushingModel createFlushingModel(String id, Properties properties) {
		String cacheNames = getRequiredProperty(properties, "cacheNames");
		return new EhCacheFlushingModel(id, Arrays.asList(StringUtils.split(cacheNames, ",")));
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
	
	public void setCreateCachesOnDemand(boolean createCachesOnDemand) {
		this.createCachesOnDemand = createCachesOnDemand;
	}

}
