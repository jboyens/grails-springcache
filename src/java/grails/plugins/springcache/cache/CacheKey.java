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

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.aspectj.lang.JoinPoint;

/**
 * A generic key for storing items in and retrieving them from a cache.
 */
public final class CacheKey {

	private final int hash;

	public static CacheKey generate(JoinPoint joinPoint) {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(joinPoint.getTarget());
		builder.append(joinPoint.getSignature().getName());
		builder.append(joinPoint.getArgs());
		return new CacheKey(builder.toHashCode());
	}

	CacheKey(int hashCode) {
		this.hash = hashCode;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		return hash == ((CacheKey) o).hash;
	}

	@Override
	public int hashCode() {
		return hash;
	}
}
