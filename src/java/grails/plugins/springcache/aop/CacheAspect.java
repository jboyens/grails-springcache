package grails.plugins.springcache.aop;

import grails.plugins.springcache.annotations.Cacheable;
import grails.plugins.springcache.cache.CacheFacade;
import grails.plugins.springcache.cache.CacheKey;
import grails.plugins.springcache.cache.CacheProvider;
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
		CacheFacade cache = cacheProvider.getCache(cacheable.model());
		CacheKey key = CacheKey.generate(pjp);
		return getFromCacheOrInvoke(pjp, cache, key);
	}

	Object getFromCacheOrInvoke(ProceedingJoinPoint pjp, CacheFacade cache, CacheKey key) throws Throwable {
		Object value;
		if (cache.containsKey(key)) {
			if (log.isDebugEnabled()) log.debug(String.format("Cache hit for %s", key.toString()));
			value = cache.get(key);
		} else {
			if (log.isDebugEnabled()) log.debug(String.format("Cache miss for %s", key.toString()));
			value = pjp.proceed();
			cache.put(key, value);
		}
		return value;
	}

	@Autowired
	public void setCacheProvider(CacheProvider cacheProvider) {
		this.cacheProvider = cacheProvider;
	}
}