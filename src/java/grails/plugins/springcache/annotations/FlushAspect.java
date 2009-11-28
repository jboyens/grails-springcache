package grails.plugins.springcache.annotations;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class FlushAspect {

	@After("@annotation(cacheFlush)")
	public void flushCaches(CacheFlush cacheFlush) throws Throwable {
	}

}
