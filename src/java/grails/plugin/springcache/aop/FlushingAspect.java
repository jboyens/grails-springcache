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

import grails.plugin.springcache.SpringcacheService;
import grails.plugin.springcache.annotations.CacheFlush;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class FlushingAspect {

	private final Logger log = LoggerFactory.getLogger(FlushingAspect.class);

	private SpringcacheService springcacheService;

	@After("@annotation(cacheFlush)")
	public void flushCaches(final CacheFlush cacheFlush) throws Throwable {
		springcacheService.flush(cacheFlush.value());
	}

	public void setSpringcacheService(SpringcacheService springcacheService) {
		this.springcacheService = springcacheService;
	}

}
