package grails.plugins.springcache.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.aspectj.lang.JoinPoint;

public class InvocationCacheKey extends DefaultCacheKey {

	private final Object target;
	private final String name;
	private final Object[] args;

	public InvocationCacheKey(Object target, String name, Object[] args) {
		super(toList(target, name, args));
		this.target = target;
		this.name = name;
		this.args = args;
	}

	public InvocationCacheKey(JoinPoint joinPoint) {
		this(joinPoint.getTarget(), joinPoint.getSignature().getName(), joinPoint.getArgs());
	}

	private static List<Object> toList(Object target, String name, Object[] args) {
		List<Object> list = new ArrayList<Object>();
		list.add(target);
		list.add(name);
		list.addAll(Arrays.asList(args));
		return list;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("target", target)
				.append("name", name)
				.append("args", args)
				.toString();
	}
}
