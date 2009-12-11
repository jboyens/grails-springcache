package grails.plugin.springcache.provider.simple;

import grails.plugin.springcache.CachingModel;

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
