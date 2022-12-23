/**
 * 
 */
package com.keycloak.admin.client.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.ReactiveKeyCommands;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.ReactiveStringCommands;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.annotation.PreDestroy;

/**
 * @author Gbenga
 *
 */
@Configuration
public class ReactiveRedisConfig {

	@Autowired
	ReactiveRedisConnectionFactory reactiveRedisConnectionFactory;

	@Bean
	public ReactiveRedisOperations<String, Object> reactiveRedisOperations() {

		Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
		RedisSerializationContext.RedisSerializationContextBuilder<String, Object> builder = RedisSerializationContext
				.newSerializationContext(new StringRedisSerializer());

		RedisSerializationContext<String, Object> context = builder.value(serializer).hashValue(serializer)
				.hashKey(serializer).build();
		return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, context);
	}

	@Bean
	public ReactiveKeyCommands keyCommands(final ReactiveRedisConnectionFactory reactiveRedisConnectionFactory) {
		return reactiveRedisConnectionFactory.getReactiveConnection().keyCommands();
	}

	@Bean
	public ReactiveStringCommands stringCommands(final ReactiveRedisConnectionFactory reactiveRedisConnectionFactory) {
		return reactiveRedisConnectionFactory.getReactiveConnection().stringCommands();
	}

	@PreDestroy
	public void cleanRedis() {
		reactiveRedisConnectionFactory.getReactiveConnection().close();
	}
}
