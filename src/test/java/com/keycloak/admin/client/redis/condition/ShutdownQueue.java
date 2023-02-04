/**
 * 
 */
package com.keycloak.admin.client.redis.condition;

import java.io.Closeable;
import java.util.LinkedList;

/**
 * Shutdown queue allowing ordered resource shutdown (LIFO).
 *
 * @author Mark Paluch
 */
enum ShutdownQueue {

	INSTANCE;

	static {

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {

				Closeable closeable;
				while ((closeable = INSTANCE.closeables.pollLast()) != null) {
					try {
						closeable.close();
					} catch (Exception o_O) {
						// ignore
						o_O.printStackTrace();
					}
				}

			}
		});
	}

	private final LinkedList<Closeable> closeables = new LinkedList<>();

	public static void register(Closeable closeable) {
		INSTANCE.closeables.add(closeable);
	}
}