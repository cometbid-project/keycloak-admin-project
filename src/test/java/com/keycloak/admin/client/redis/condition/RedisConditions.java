/**
 * 
 */
package com.keycloak.admin.client.redis.condition;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;
import io.lettuce.core.models.command.CommandDetail;
import io.lettuce.core.models.command.CommandDetailParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.springframework.data.util.Version;

/**
 * Collection of utility methods to test conditions during test execution.
 *
 * @author Mark Paluch
 */
class RedisConditions {

	private final Map<String, Integer> commands;
	private final Version version;

	private RedisConditions(RedisClusterCommands<String, String> commands) {

		var result = CommandDetailParser.parse(commands.command());

		this.commands = result.stream()
				.collect(Collectors.toMap(commandDetail -> commandDetail.getName().toUpperCase(), CommandDetail::getArity));

		var info = commands.info("server");

		try {

			var inputStream = new ByteArrayInputStream(info.getBytes());
			var p = new Properties();
			p.load(inputStream);

			version = Version.parse(p.getProperty("redis_version"));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Create {@link RedisCommands} given {@link StatefulRedisConnection}.
	 *
	 * @param connection must not be {@code null}.
	 * @return
	 */
	public static RedisConditions of(StatefulRedisConnection<String, String> connection) {
		return new RedisConditions(connection.sync());
	}

	/**
	 * Create {@link RedisCommands} given {@link StatefulRedisClusterConnection}.
	 *
	 * @param connection must not be {@code null}.
	 * @return
	 */
	public static RedisConditions of(StatefulRedisClusterConnection<String, String> connection) {
		return new RedisConditions(connection.sync());
	}

	/**
	 * Create {@link RedisConditions} given {@link RedisCommands}.
	 *
	 * @param commands must not be {@code null}.
	 * @return
	 */
	public static RedisConditions of(RedisClusterCommands<String, String> commands) {
		return new RedisConditions(commands);
	}

	/**
	 * @return the Redis {@link Version}.
	 */
	public Version getRedisVersion() {
		return version;
	}

	/**
	 * @param command
	 * @return {@code true} if the command is present.
	 */
	public boolean hasCommand(String command) {
		return commands.containsKey(command.toUpperCase());
	}

	/**
	 * @param command command name.
	 * @param arity expected arity.
	 * @return {@code true} if the command is present with the given arity.
	 */
	public boolean hasCommandArity(String command, int arity) {

		if (!hasCommand(command)) {
			throw new IllegalStateException("Unknown command: " + command + " in " + commands);
		}

		return commands.get(command.toUpperCase()) == arity;
	}

	/**
	 * @param versionNumber
	 * @return {@code true} if the version number is met.
	 */
	public boolean hasVersionGreaterOrEqualsTo(String versionNumber) {
		return version.isGreaterThanOrEqualTo(Version.parse(versionNumber));
	}

}
