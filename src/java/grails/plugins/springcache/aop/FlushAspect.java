package grails.plugins.springcache.aop;

import grails.plugins.springcache.annotations.CacheFlush;
import grails.plugins.springcache.cache.CacheFacade;
import grails.plugins.springcache.cache.CacheProvider;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class FlushAspect {

	private final Logger log = LoggerFactory.getLogger(FlushAspect.class);

	private CacheProvider cacheProvider;

	@After("@annotation(cacheFlush)")
	public void flushCaches(CacheFlush cacheFlush) throws Throwable {
		for (CacheFacade cache : cacheProvider.getCaches(cacheFlush.model())) {
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
