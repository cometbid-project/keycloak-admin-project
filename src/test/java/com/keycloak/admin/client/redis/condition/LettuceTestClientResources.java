/**
 * 
 */
package com.keycloak.admin.client.redis.condition;

import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;

import java.util.concurrent.TimeUnit;

/**
 * Client-Resources suitable for testing. Every time a new {@link LettuceTestClientResources} instance is created, a
 * {@link Runtime#addShutdownHook(Thread) shutdown hook} is added to close the client resources.
 *
 * @author Mark Paluch
 * @author Christoph Strobl
 */
class LettuceTestClientResources {

	private static final ClientResources SHARED_CLIENT_RESOURCES;

	static {

		SHARED_CLIENT_RESOURCES = DefaultClientResources.builder().build();
		ShutdownQueue.INSTANCE.register(() -> SHARED_CLIENT_RESOURCES.shutdown(0, 0, TimeUnit.MILLISECONDS));
	}

	private LettuceTestClientResources() {}

	/**
	 * @return the client resources.
	 */
	public static ClientResources getSharedClientResources() {
		return SHARED_CLIENT_RESOURCES;
	}
}
