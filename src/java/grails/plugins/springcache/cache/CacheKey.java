package grails.plugins.springcache.cache;

import java.util.Collection;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.aspectj.lang.JoinPoint;

public final class CacheKey {

	private final int hash;

	public static CacheKey generate(JoinPoint joinPoint) {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(joinPoint.getTarget());
		builder.append(joinPoint.getSignature().getName());
		builder.append(joinPoint.getArgs());
		return new CacheKey(builder.toHashCode());
	}

	public static CacheKey generate(Collection c) {
		HashCodeBuilder builder = new HashCodeBuilder();
		for (Object element : c) {
			builder.append(element);
		}
		return new CacheKey(builder.toHashCode());
	}

	private CacheKey(int hashCode) {
		this.hash = hashCode;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		return hash == ((CacheKey) o).hash;
	}

	@Override
	public int hashCode() {
		return hash;
	}
}
