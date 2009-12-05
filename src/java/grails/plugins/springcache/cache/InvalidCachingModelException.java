package grails.plugins.springcache.cache;

public class InvalidCachingModelException extends RuntimeException {

	public InvalidCachingModelException(String cachingModelId) {
		super(String.format("Caching model %s not found", cachingModelId));
	}

}
