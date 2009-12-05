package grails.plugins.springcache.cache;

public class InvalidFlushingModelException extends RuntimeException {

	public InvalidFlushingModelException(String flushingModelId) {
		super(String.format("Flushing model %s not found", flushingModelId));
	}

}
