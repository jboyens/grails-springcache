package grails.plugins.springcache.annotations;

import grails.plugins.springcache.cache.CacheFacade;
import grails.plugins.springcache.cache.CacheKey;
import grails.plugins.springcache.cache.CacheProvider;
import grails.plugins.springcache.cache.InvocationCacheKey;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class CacheAspect {

	private final Logger log = LoggerFactory.getLogger(CacheAspect.class);

	private CacheProvider cacheProvider;

	@Around("@annotation(cacheable)")
	public Object invokeCachedMethod(ProceedingJoinPoint pjp, Cacheable cacheable) throws Throwable {
		CacheFacade cache = cacheProvider.getCache(cacheable.cacheName());
		CacheKey key = new InvocationCacheKey(pjp);
		return getFromCacheOrInvoke(pjp, cache, key);
	}

	Object getFromCacheOrInvoke(ProceedingJoinPoint pjp, CacheFacade cache, CacheKey key) throws Throwable {
		Object value;
		if (cache.containsKey(key)) {
			log.debug("Cache hit for %s", key.toString());
			value = cache.get(key);
		} else {
			log.debug("Cache miss for %s", key.toString());
			value = pjp.proceed();
			cache.put(key, value);
		}
		return value;
	}

	@Autowired(required = true)
	public void setCacheProvider(CacheProvider cacheProvider) {
		this.cacheProvider = cacheProvider;
	}

}