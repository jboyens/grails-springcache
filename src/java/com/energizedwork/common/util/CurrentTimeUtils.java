package com.energizedwork.common.util;

import java.util.concurrent.TimeUnit;
import groovy.lang.Closure;

public class CurrentTimeUtils {

	private static CurrentTimeStrategy currentTimeStrategy = SystemCurrentTimeStrategy.getInstance();

	public static long currentTimeMillis() {
		return currentTimeStrategy.currentTimeMillis();
	}

	public static synchronized void useSystemCurrentTime() {
		currentTimeStrategy = SystemCurrentTimeStrategy.getInstance();
	}

	public static synchronized void useOffsetCurrentTime(long offset) {
		currentTimeStrategy = OffsetCurrentTimeStrategy.getInstance(offset);
	}

	public static synchronized void useOffsetCurrentTime(long offset, TimeUnit unit) {
		currentTimeStrategy = OffsetCurrentTimeStrategy.getInstance(unit.toMillis(offset));
	}

	public static synchronized void useFixedCurrentTime(long fixedCurrentTime) {
		currentTimeStrategy = FixedCurrentTimeStrategy.getInstance(fixedCurrentTime);
	}

	public static Object withOffsetCurrentTime(long offset, Closure closure) {
		useOffsetCurrentTime(offset);
		try {
			return closure.call();
		} finally {
			useSystemCurrentTime();
		}
	}

	public static Object withOffsetCurrentTime(long offset, TimeUnit unit, Closure closure) {
		useOffsetCurrentTime(offset, unit);
		try {
			return closure.call();
		} finally {
			useSystemCurrentTime();
		}
	}

	public static Object withFixedCurrentTime(long fixedCurrentTime, Closure closure) {
		useFixedCurrentTime(fixedCurrentTime);
		try {
			return closure.call();
		} finally {
			useSystemCurrentTime();
		}
	}

	private static interface CurrentTimeStrategy {
		long currentTimeMillis();
	}

	private static class SystemCurrentTimeStrategy implements CurrentTimeStrategy {
		private static final CurrentTimeStrategy instance = new SystemCurrentTimeStrategy();

		static CurrentTimeStrategy getInstance() {
			return instance;
		}

		private SystemCurrentTimeStrategy() {}

		public long currentTimeMillis() {
			return System.currentTimeMillis();
		}
	}

	private static class OffsetCurrentTimeStrategy extends SystemCurrentTimeStrategy {
		private final long offset;

		static CurrentTimeStrategy getInstance(long offset) {
			return new OffsetCurrentTimeStrategy(offset);
		}

		private OffsetCurrentTimeStrategy(long offset) {
			this.offset = offset;
		}

		public long currentTimeMillis() {
			return super.currentTimeMillis() + offset;
		}
	}

	private static class FixedCurrentTimeStrategy implements CurrentTimeStrategy {
		private final long fixedCurrentTime;

		static CurrentTimeStrategy getInstance(long fixedCurrentTime) {
			return new FixedCurrentTimeStrategy(fixedCurrentTime);
		}

		private FixedCurrentTimeStrategy(long fixedCurrentTime) {
			this.fixedCurrentTime = fixedCurrentTime;
		}

		public long currentTimeMillis() {
			return fixedCurrentTime;
		}
	}

	private CurrentTimeUtils() {}
}
