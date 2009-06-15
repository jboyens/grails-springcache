package com.energizedwork.cache.mapcache;

import org.springmodules.cache.CachingModel;
import org.springmodules.util.Objects;
import org.springframework.util.StringUtils;

class MapCacheCachingModel implements CachingModel {

	private String cacheName;

	public String getCacheName() {
		return cacheName;
	}

	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}

	public String toString() {
		return Objects.identityToString(this)
				.append("[cacheName=")
				.append(StringUtils.quote(cacheName))
				.append("]")
				.toString();
	}
}
