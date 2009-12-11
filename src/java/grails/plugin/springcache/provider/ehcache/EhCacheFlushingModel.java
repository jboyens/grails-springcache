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

import java.util.Collection;
import java.util.Collections;
import grails.plugin.springcache.FlushingModel;

class EhCacheFlushingModel extends FlushingModel {

	private final Collection<String> cacheNames;

	public EhCacheFlushingModel(String id, Collection<String> cacheNames) {
		super(id);
		this.cacheNames = Collections.unmodifiableCollection(cacheNames);
	}

	public Collection<String> getCacheNames() {
		return cacheNames;
	}

}
