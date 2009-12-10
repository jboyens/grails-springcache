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
package grails.plugins.springcache.aop;

import grails.plugins.springcache.annotations.CacheFlush;
import grails.plugins.springcache.cache.CacheFacade;
import grails.plugins.springcache.cache.CacheProvider;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class FlushingAspect {

	private final Logger log = LoggerFactory.getLogger(FlushingAspect.class);

	private CacheProvider cacheProvider;

	@After("@annotation(cacheFlush)")
	public void flushCaches(CacheFlush cacheFlush) throws Throwable {
		for (CacheFacade cache : cacheProvider.getCaches(cacheFlush.modelId())) {
			try {
				cache.flush();
			} catch (Exception e) {
				log.error(String.format("Exception caught when flushing cache '%s'", cache.getName()), e);
			}
		}
	}

	public void setCacheProvider(CacheProvider cacheProvider) {
		this.cacheProvider = cacheProvider;
	}
}
