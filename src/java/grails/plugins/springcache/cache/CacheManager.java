package grails.plugins.springcache.cache;

import grails.plugins.springcache.annotations.Cacheable;

public interface CacheManager {

	Cache getCache(Cacheable cacheable);

}
