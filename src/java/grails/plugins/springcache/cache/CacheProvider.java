package grails.plugins.springcache.cache;

public interface CacheProvider {

	CacheFacade getCache(String name) throws CacheNotFoundException;

}
