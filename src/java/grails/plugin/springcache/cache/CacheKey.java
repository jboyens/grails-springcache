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

import java.io.Serializable;
import org.aspectj.lang.JoinPoint;

/**
 * A generic key for storing items in and retrieving them from a cache.
 */
public final class CacheKey implements Serializable {

	private final int hash;
	private final long checksum;

	public static CacheKey generate(JoinPoint joinPoint) {
		CacheKeyBuilder builder = new CacheKeyBuilder();
		builder.append(joinPoint.getTarget());
		builder.append(joinPoint.getSignature().getName());
		builder.append(joinPoint.getArgs());
		return builder.toCacheKey();
	}

	CacheKey(int hashCode, long checksum) {
		this.hash = hashCode;
		this.checksum = checksum;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CacheKey that = (CacheKey) o;
		return hash == that.hash && checksum == that.checksum;
	}

	@Override
	public int hashCode() {
		int result = hash;
		result = 31 * result + (int) (checksum ^ (checksum >>> 32));
		return result;
	}

	@Override
	public String toString() {
		return String.format("CacheKey[%d|%d]", hash, checksum);
	}
}
