package grails.plugins.springcache.annotations;

import org.springframework.stereotype.Component;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.ProceedingJoinPoint;

@Component
@Aspect
public class FlushAspect {

	@Around("@annotation(cacheFlush)")
	public Object aroundAdvice(ProceedingJoinPoint pjp, CacheFlush cacheFlush) throws Throwable {
		System.out.println("Flush something");
		return pjp.proceed();
	}

}
