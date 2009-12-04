package grails.plugins.springcache.aop;

import java.util.Map;
import grails.plugins.springcache.annotations.Cacheable;
import grails.plugins.springcache.cache.CacheFacade;
import grails.plugins.springcache.cache.CacheKey;
import grails.plugins.springcache.cache.CacheProvider;
import grails.plugins.springcache.cache.CachingModel;
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
	private Map<String, CachingModel> models;

	@Around("@annotation(cacheable)")
	public Object invokeCachedMethod(ProceedingJoinPoint pjp, Cacheable cacheable) throws Throwable {
		CachingModel model = models.get(cacheable.model());
		CacheFacade cache = cacheProvider.getCache(model);
		CacheKey key = CacheKey.generate(pjp);
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

	@Autowired
	public void setCacheProvider(CacheProvider cacheProvider) {
		this.cacheProvider = cacheProvider;
	}

	@Autowired
	public void setModels(Map<String, CachingModel> models) {
		this.models = models;
	}
}