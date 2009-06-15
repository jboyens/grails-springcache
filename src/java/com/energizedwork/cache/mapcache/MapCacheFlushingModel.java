package com.energizedwork.cache.mapcache;

import org.springmodules.cache.provider.AbstractFlushingModel;
import org.springmodules.util.Objects;

class MapCacheFlushingModel extends AbstractFlushingModel {

	private String[] cacheNames;

	public String[] getCacheNames() {
		return cacheNames;
	}

	public void setCacheNames(String[] cacheNames) {
		this.cacheNames = cacheNames;
	}

	public String toString() {
		return Objects.identityToString(this)
				.append("[cacheNames=")
				.append(Objects.nullSafeToString(cacheNames))
				.append(", flushBeforeMethodExecution=")
				.append(flushBeforeMethodExecution())
				.append("]")
				.toString();
	}
}
