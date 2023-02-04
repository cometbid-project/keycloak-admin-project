/**
 * 
 */
package com.keycloak.admin.client.redis.condition;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Utility to detect Redis operation modes.
 *
 * @author Mark Paluch
 */
public class RedisDetector {

	public static boolean canConnectToPort(String host, int port) {

		try (var socket = new Socket()) {
			socket.connect(new InetSocketAddress(host, port), 100);

			return true;
		} catch (IOException e) {
			return false;
		}
	}

}
