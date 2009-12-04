package grails.plugins.springcache.providers.ehcache;

public class EhCacheCachingModel {

	private final String cacheName;

	public EhCacheCachingModel(String cacheName) {
		this.cacheName = cacheName;
	}

	public String getCacheName() {
		return cacheName;
	}
}
