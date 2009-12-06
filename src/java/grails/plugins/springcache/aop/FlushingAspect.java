package grails.plugins.springcache.aop;

import grails.plugins.springcache.annotations.CacheFlush;
import grails.plugins.springcache.cache.CacheFacade;
import grails.plugins.springcache.cache.CacheProvider;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class FlushingAspect {

	private final Logger log = LoggerFactory.getLogger(FlushingAspect.class);

	private CacheProvider cacheProvider;

	@After("@annotation(cacheFlush)")
	public void flushCaches(CacheFlush cacheFlush) throws Throwable {
		for (CacheFacade cache : cacheProvider.getCaches(cacheFlush.model())) {
			try {
				cache.flush();
			} catch (Exception e) {
				log.error(String.format("Exception caught when flushing cache '%s'", cache.getName()), e);
			}
		}
	}

	public void setCacheProvider(CacheProvider cacheProvider) {
		this.cacheProvider = cacheProvider;
	}
}
