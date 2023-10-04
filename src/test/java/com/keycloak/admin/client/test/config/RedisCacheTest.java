/**
 * 
 */
package com.keycloak.admin.client.test.config;


import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author Gbenga
 *
 */
//@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("redisnoauth")
public class RedisCacheTest {

	@TestConfiguration
	static class RedisTestConfiguration {
		
		@Bean
		public RedisConnectionFactory redisConnectionFactory(@Value("${spring.redis.host}") String host,
				@Value("${spring.redis.port}") int port) {
			
			return new LettuceConnectionFactory(host, port);
		}
		
		@Bean
		public RedisOperations <String, String> stringKeyAndStringValueRedisOperation(RedisConnectionFactory redisConnectionFactory) {
			
			RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
			redisTemplate.setConnectionFactory(redisConnectionFactory);
			redisTemplate.setKeySerializer(new StringRedisSerializer(StandardCharsets.UTF_8));
			redisTemplate.setValueSerializer(new StringRedisSerializer(StandardCharsets.UTF_8));
			return redisTemplate;
		}
	}
	
	@Test
	public void myTest() {
		
	}
}
