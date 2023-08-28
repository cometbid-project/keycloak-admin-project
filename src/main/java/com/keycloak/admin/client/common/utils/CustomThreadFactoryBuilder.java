/**
 * 
 */
package com.keycloak.admin.client.common.utils;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author ashraf
 *
 */
public class CustomThreadFactoryBuilder {

	private String namePrefix = null;
	private boolean daemon = false;
	private int priority = Thread.NORM_PRIORITY;
	private UncaughtExceptionHandler uncaughtExceptionHandler = null;

	public CustomThreadFactoryBuilder setNamePrefix(String namePrefix) {
		if (namePrefix == null) {
			throw new NullPointerException();
		}
		this.namePrefix = namePrefix;
		return this;
	}

	public CustomThreadFactoryBuilder setDaemon(boolean daemon) {
		this.daemon = daemon;
		return this;
	}

	public CustomThreadFactoryBuilder setUncaughtExceptionHandler(UncaughtExceptionHandler uncaughtExceptionHandler) {
		Objects.requireNonNull(uncaughtExceptionHandler);
		this.uncaughtExceptionHandler = uncaughtExceptionHandler;
		return this;
	}

	public CustomThreadFactoryBuilder setPriority(int priority) {
		if (priority < Thread.MIN_PRIORITY) {
			throw new IllegalArgumentException(
					String.format("Thread priority (%s) must be >= %s", priority, Thread.MIN_PRIORITY));
		}

		if (priority > Thread.MAX_PRIORITY) {
			throw new IllegalArgumentException(
					String.format("Thread priority (%s) must be <= %s", priority, Thread.MAX_PRIORITY));
		}

		this.priority = priority;
		return this;
	}

	public ThreadFactory build() {
		return build(this);
	}

	private static ThreadFactory build(CustomThreadFactoryBuilder builder) {
		final String namePrefix = builder.namePrefix;
		final Boolean daemon = builder.daemon;
		final Integer priority = builder.priority;

		UncaughtExceptionHandler uncaughtExceptionHandler = builder.uncaughtExceptionHandler;

		final AtomicLong count = new AtomicLong(0);

		return new ThreadFactory() {
			@Override
			public Thread newThread(Runnable runnable) {
				Thread thread = new Thread(runnable);
				if (namePrefix != null) {
					thread.setName(namePrefix + "-" + count.getAndIncrement());
				}
				if (daemon != null) {
					thread.setDaemon(daemon);
				}
				if (priority != null) {
					thread.setPriority(priority);
				}

				if (uncaughtExceptionHandler != null) {
					thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
				}
				return thread;
			}
		};
	}

}
