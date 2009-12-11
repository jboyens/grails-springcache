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

/**
 * Builder used to compute the hash and checksum used for an immutable cache key.
 * <p/>
 * Based on org.springmodules.cache.key.HashCodeCalculator from Spring Modules Cache (see
 * https://springmodules.dev.java.net/) by Omar Irbouh and Alex Ruiz.
 */
public class CacheKeyBuilder {

	private static final int INITIAL_HASH = 17;
	private static final int MULTIPLIER = 31;

	private int count = 0;
	private long checksum = 0L;
	private int hash = INITIAL_HASH;

	public CacheKeyBuilder append(int i) {
		hash = hash * MULTIPLIER + i;
		count++;
		checksum += (count * i);
		return this;
	}

	public CacheKeyBuilder append(Object o) {
		return append(o.hashCode());
	}

	public CacheKeyBuilder append(Object[] oarr) {
		for (Object o : oarr) {
			append(o.hashCode());
		}
		return this;
	}

	public CacheKey toCacheKey() {
		return new CacheKey(hash, checksum);
	}

}
