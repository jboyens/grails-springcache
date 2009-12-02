package grails.plugins.springcache.cache;

public interface CacheFacade {

	boolean containsKey(CacheKey key);

	Object get(CacheKey key);

	void put(CacheKey key, Object value);

	void flush();

	String getName();

	Number getSize();
	
}
