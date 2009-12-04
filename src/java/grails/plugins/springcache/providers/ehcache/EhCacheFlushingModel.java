package grails.plugins.springcache.providers.ehcache;

import java.util.Collection;
import java.util.Collections;
import grails.plugins.springcache.cache.FlushingModel;

public class EhCacheFlushingModel implements FlushingModel {

	private final Collection<String> cacheNames;

	public EhCacheFlushingModel(Collection<String> cacheNames) {
		this.cacheNames = Collections.unmodifiableCollection(cacheNames);
	}

	public Collection<String> getCacheNames() {
		return cacheNames;
	}
}
