package grails.plugins.springcache.cache;

public interface Cache {

	boolean containsKey(CacheKey key);

	Object get(CacheKey key);

	void put(CacheKey key, Object value);
}
