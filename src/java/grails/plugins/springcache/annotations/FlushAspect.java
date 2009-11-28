package grails.plugins.springcache.annotations;

import grails.plugins.springcache.cache.CacheManager;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class FlushAspect {

	private CacheManager cacheManager;

	@After("@annotation(cacheFlush)")
	public void flushCaches(CacheFlush cacheFlush) throws Throwable {
		for (String cacheName : cacheFlush.cacheNames()) {
			cacheManager.getCache(cacheName).flush();
		}
	}

	@Autowired(required = true)
	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

}
