package grails.plugins.springcache.cache;

import java.util.Collection;

public interface CacheProvider<C extends CachingModel, F extends FlushingModel> {

	CacheFacade getCache(C model) throws CacheNotFoundException;

	Collection<CacheFacade> getCaches(F model) throws CacheNotFoundException;

}
