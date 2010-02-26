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
package grails.plugin.springcache.aop;

import grails.plugin.springcache.annotations.CacheFlush;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class FlushingAspect {

	private final Logger log = LoggerFactory.getLogger(FlushingAspect.class);

	private CacheManager cacheManager;

	@After("@annotation(cacheFlush)")
	public void flushCaches(CacheFlush cacheFlush) throws Throwable {
		for (String name : cacheFlush.value()) {
			Ehcache cache = cacheManager.getEhcache(name);
			try {
				if (log.isDebugEnabled()) log.debug(String.format("Flushing cache %s", cache.getName()));
				cache.flush();
			} catch (Exception e) {
				log.error(String.format("Exception caught when flushing cache '%s'", cache.getName()), e);
			}
		}
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
}
