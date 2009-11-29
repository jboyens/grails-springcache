package grails.plugins.springcache.cache;

import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.builder.ToStringBuilder;

public class DefaultCacheKey implements CacheKey {

	private final List<Object> values;

	public DefaultCacheKey(List<Object> values) {
		this.values = Collections.unmodifiableList(values);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DefaultCacheKey that = (DefaultCacheKey) o;

		return values.equals(that.values);
	}

	@Override
	public int hashCode() {
		return values.hashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(values).toString();
	}
}
