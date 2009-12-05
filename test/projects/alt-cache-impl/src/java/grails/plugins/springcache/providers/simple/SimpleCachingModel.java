package grails.plugins.springcache.providers.simple;

import grails.plugins.springcache.cache.CachingModel;

class SimpleCachingModel extends CachingModel {

	private final String cacheName;

	public SimpleCachingModel(String id, String cacheName) {
		super(id);
		this.cacheName = cacheName;
	}

	public String getCacheName() {
		return cacheName;
	}
}
