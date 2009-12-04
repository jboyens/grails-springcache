package grails.plugins.springcache.cache;

import java.util.Collection;

public interface CacheProvider {

	CacheFacade getCache(String cacheModelId) throws CacheNotFoundException;

	Collection<CacheFacade> getCaches(String flushModelId) throws CacheNotFoundException;

}
