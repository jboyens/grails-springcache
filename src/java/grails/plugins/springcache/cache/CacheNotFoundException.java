package grails.plugins.springcache.cache;

public class CacheNotFoundException extends RuntimeException {

	public CacheNotFoundException(String name) {
		super(String.format("No cache named '%s' could be found.", name));
	}

}
