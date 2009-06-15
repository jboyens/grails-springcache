package com.energizedwork.cache.mapcache;

import com.energizedwork.common.util.CurrentTimeUtils;

class MapCacheEntry {

	private final Object value;
	private final long created;
	private final long expires;
	private final String stringValue;

	public MapCacheEntry(Object value, long ttl) {
		if (value == null) throw new NullPointerException("null value not permitted");
		this.value = value;
		this.created = CurrentTimeUtils.currentTimeMillis();
		this.expires = created + ttl;

		// pre-create string value (everything's final)
		StringBuilder buffy = new StringBuilder();
		buffy.append("CacheEntry[created=").append(created);
		buffy.append(", expired=").append(expires);
		buffy.append(", value=").append(value.toString());
		buffy.append("]");
		this.stringValue = buffy.toString();
	}

	public Object getValue() {
		return value;
	}

	public long getCreated() {
		return created;
	}

	public boolean isExpired() {
		return expires <= CurrentTimeUtils.currentTimeMillis();
	}

	public String toString() {
		return stringValue;
	}
}
