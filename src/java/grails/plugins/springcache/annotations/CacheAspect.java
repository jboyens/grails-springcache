package grails.plugins.springcache.annotations;

import java.util.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import grails.plugins.springcache.cache.CacheKey;
import grails.plugins.springcache.cache.Cache;
import grails.plugins.springcache.cache.CacheManager;
import grails.plugins.springcache.cache.DefaultCacheKey;

@Component
@Aspect
public class CacheAspect {

	private final Logger log = LoggerFactory.getLogger(CacheAspect.class);

	private CacheManager cacheManager;

	@Around("@annotation(cacheable)")
	public Object aroundAdvice(ProceedingJoinPoint pjp, Cacheable cacheable) throws Throwable {
		Cache cache = cacheManager.getCache(cacheable);

		CacheKey key = toCacheKey(pjp);

		return getFromCacheOrProceed(pjp, cache, key);
	}

	CacheKey toCacheKey(ProceedingJoinPoint pjp) {
		List<Object> values = new ArrayList<Object>();
		values.add(pjp.getSignature().getName());
		values.addAll(Arrays.asList(pjp.getArgs()));
		return new DefaultCacheKey(values);
	}

	Object getFromCacheOrProceed(ProceedingJoinPoint pjp, Cache cache, CacheKey key) throws Throwable {
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
	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

}