package grails.plugins.springcache.providers.simple;

import java.util.Collection;
import java.util.Collections;
import grails.plugins.springcache.cache.FlushingModel;

class SimpleFlushingModel extends FlushingModel {

	private final Collection<String> cacheNames;

	public SimpleFlushingModel(String id, Collection<String> cacheNames) {
		super(id);
		this.cacheNames = Collections.unmodifiableCollection(cacheNames);
	}

	public Collection<String> getCacheNames() {
		return cacheNames;
	}
}
