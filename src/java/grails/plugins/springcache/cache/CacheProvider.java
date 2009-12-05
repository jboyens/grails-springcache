package grails.plugins.springcache.cache;

import java.util.Collection;
import java.util.Properties;

public interface CacheProvider {

	CacheFacade getCache(String cacheModelId) throws CacheNotFoundException;

	Collection<CacheFacade> getCaches(String flushModelId) throws CacheNotFoundException;

	void addCachingModel(String id, Properties properties);

	void addFlushingModel(String id, Properties properties);

}
