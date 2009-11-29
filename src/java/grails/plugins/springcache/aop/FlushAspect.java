package grails.plugins.springcache.aop;

import grails.plugins.springcache.annotations.CacheFlush;
import grails.plugins.springcache.cache.CacheProvider;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class FlushAspect {

	private CacheProvider cacheProvider;

	@After("@annotation(cacheFlush)")
	public void flushCaches(CacheFlush cacheFlush) throws Throwable {
		for (String cacheName : cacheFlush.cacheNames()) {
			cacheProvider.getCache(cacheName).flush();
		}
	}

	@Autowired(required = true)
	public void setCacheProvider(CacheProvider cacheProvider) {
		this.cacheProvider = cacheProvider;
	}

}
