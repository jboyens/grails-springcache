package grails.plugins.springcache.providers.ehcache;

import grails.plugins.springcache.cache.CachingModel;

public class EhCacheCachingModel implements CachingModel {

	private final String cacheName;

	public EhCacheCachingModel(String cacheName) {
		this.cacheName = cacheName;
	}

	public String getCacheName() {
		return cacheName;
	}
}
