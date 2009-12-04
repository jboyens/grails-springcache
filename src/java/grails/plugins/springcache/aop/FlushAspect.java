package grails.plugins.springcache.aop;

import grails.plugins.springcache.annotations.CacheFlush;
import grails.plugins.springcache.cache.CacheProvider;
import grails.plugins.springcache.cache.CacheFacade;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Aspect
public class FlushAspect {

	private final Logger log = LoggerFactory.getLogger(FlushAspect.class);

	private CacheProvider cacheProvider;

	@After("@annotation(cacheFlush)")
	public void flushCaches(CacheFlush cacheFlush) throws Throwable {
		for (String cacheName : cacheFlush.cacheNames()) {
			CacheFacade cache = cacheProvider.getCache(cacheName);
			try {
				cache.flush();
			} catch (Exception e) {
				System.err.printf("Exception caught when flushing cache '%s'%n", cache.getName());
				log.error("Exception caught when flushing cache '%s'", cache.getName(), e);
			}
		}
	}

	@Autowired
	public void setCacheProvider(CacheProvider cacheProvider) {
		this.cacheProvider = cacheProvider;
	}

}
