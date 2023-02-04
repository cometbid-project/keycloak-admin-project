/**
 * 
 */
package com.keycloak.admin.client.redis.condition;


import static org.junit.jupiter.api.extension.ConditionEvaluationResult.*;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

import java.time.Duration;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;

/**
 * {@link ExecutionCondition} for {@link EnabledOnCommandCondition @EnabledOnCommand}.
 *
 * @see EnabledOnCommandCondition
 * @author Mark Paluch
 * @author Christoph Strobl
 */
class EnabledOnCommandCondition implements ExecutionCondition {

	private static final ConditionEvaluationResult ENABLED_BY_DEFAULT = enabled("@EnabledOnCommand is not present");
	private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(RedisConditions.class);

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {

		var optional = AnnotationUtils.findAnnotation(context.getElement(), EnabledOnCommand.class);

		if (!optional.isPresent()) {
			return ENABLED_BY_DEFAULT;
		}

		var annotation = optional.get();
		var command = annotation.value();

		var store = context.getStore(NAMESPACE);
		var conditions = store.getOrComputeIfAbsent(RedisConditions.class, ignore -> {

			var redisClient = RedisClient.create(LettuceTestClientResources.getSharedClientResources(),
					RedisURI.create(annotation.host(), annotation.port()));

			var connection = redisClient.connect();
			var redisConditions = RedisConditions.of(connection);

			connection.close();
			redisClient.shutdown(Duration.ZERO, Duration.ZERO);
			return redisConditions;
		}, RedisConditions.class);

		var hasCommand = conditions.hasCommand(command);
		return hasCommand ? enabled("Enabled on command " + command)
				: disabled("Disabled, command " + command + " not available on Redis version " + conditions.getRedisVersion());
	}

}
