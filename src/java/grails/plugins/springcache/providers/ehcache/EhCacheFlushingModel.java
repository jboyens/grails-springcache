package grails.plugins.springcache.providers.ehcache;

import java.util.Collection;
import java.util.Collections;

public class EhCacheFlushingModel {

	private final Collection<String> cacheNames;

	public EhCacheFlushingModel(Collection<String> cacheNames) {
		this.cacheNames = Collections.unmodifiableCollection(cacheNames);
	}

	public Collection<String> getCacheNames() {
		return cacheNames;
	}
}
