/**
 * 
 */
package com.keycloak.admin.client.service;


import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;

import com.github.javafaker.Faker;
import com.keycloak.admin.client.common.utils.LocaleContextUtils;
import com.keycloak.admin.client.common.utils.RandomGenerator;
import com.keycloak.admin.client.config.AppConfiguration;
import com.keycloak.admin.client.config.AuthProperties;
import com.keycloak.admin.client.config.MessageConfig;
import com.keycloak.admin.client.dataacess.AuthBuilder;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.models.AuthenticationResponse;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.oauth.service.GatewayRedisCache;
import com.keycloak.admin.client.redis.service.ReactiveRedisComponent;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author Gbenga
 *
 */
@Log4j2
@WebFluxTest
@DisplayName("Gateway Cache Service")
@ContextConfiguration(classes = { AppConfiguration.class, MessageConfig.class,
		AuthProperties.class })
@Import({ LocaleContextUtils.class, GatewayRedisCache.class })
class GatewayRedisCacheServiceTest {

	@MockBean(name="redis")
	private ReactiveRedisComponent redisComponent;

	@Autowired
	private GatewayRedisCache redisCache;
	
	private static Faker faker;
	
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		
	}
	
	@BeforeAll
	static void initializeData() {
		faker = Faker.instance();
	}
	
	
	/**
	 * 
	 */
	@DisplayName("to test init method")
	@Test
	void verifyInit() {

		redisCache.init();
	}

	/**
	 * 
	 */
	@DisplayName("to test save as Blocked ips")
	@Test
	void verifySaveAsBlockedIp() {
		// cacheUtil.putIfAbsent(ipAddr, Integer.valueOf(0), blockedIpTTL)
		when(redisComponent.putIfAbsent(anyString(), any(), anyLong()))
					.thenReturn(Mono.just(Boolean.TRUE));
		
		when(redisComponent.incrementThis(anyString()))
					.thenReturn(Mono.just(1L));

		String ipAddress = faker.internet().ipV4Address();
		Mono<Long> result = redisCache.saveAsBlockedIp(ipAddress);		
		StepVerifier.create(result).expectNext(1L).verifyComplete();		
	}
	
	/**
	 * 
	 */
	/*
	@DisplayName("to test get Authentication response")
	@Test
	void verifyGetAuthenticationResponse() {

		UUID userId = UUID.randomUUID();
		UserVO userVO = UserBuilder.user().userVo(userId);
		
		AuthenticationResponse authResponse = AuthBuilder.auth(userVO).authResponse();
		
		String totpSessionId = RandomGenerator.generateSessionId();
		when(redisComponent.getPojo(anyString()))
					.thenReturn(Mono.just(authResponse));

		Mono<AuthenticationResponse> authResult = redisCache.getAuthenticationResponse(totpSessionId);		
		StepVerifier.create(authResult).expectNext(authResponse).verifyComplete();
		
		when(redisComponent.getPojo(anyString()))
					.thenReturn(Mono.empty());
		
		authResponse = new AuthenticationResponse();
		authResult = redisCache.getAuthenticationResponse(totpSessionId);			
		StepVerifier.create(authResult).expectNext(authResponse).verifyComplete();
	}
	*/

	/**
	 * 
	 */
	@DisplayName("to test check blocked ip")
	@Test
	void verifyCheckIfBlockedIpExist() {
		
		when(redisComponent.getPojo(anyString()))
					.thenReturn(Mono.just(1L));

		String ipAddress = faker.internet().ipV4Address();
		
		Mono<Boolean> result = redisCache.isBlockedIp(ipAddress);		
		StepVerifier.create(result).expectNext(Boolean.TRUE).verifyComplete(); 	
		
		when(redisComponent.getPojo(anyString()))
				.thenReturn(Mono.empty());
		
		result = redisCache.isBlockedIp(ipAddress);		
		StepVerifier.create(result).expectNext(Boolean.FALSE).verifyComplete(); 	
	}
	
	/**
	 * 
	 */
	@DisplayName("to test get Password reset session id")
	@Test
	void verifyGetPasswordResetSessionId() {
				
		String passwordResetSessionId = RandomGenerator.generateSessionId();
		when(redisComponent.getPojo(anyString()))
					.thenReturn(Mono.just(passwordResetSessionId));

		Mono<String> authResult = redisCache.getPasswordResetSession(passwordResetSessionId);		
		StepVerifier.create(authResult).expectNext(passwordResetSessionId).verifyComplete();
		
		when(redisComponent.getPojo(anyString()))
					.thenReturn(Mono.empty());
		
		//String authResponse = null;
		authResult = redisCache.getPasswordResetSession(passwordResetSessionId);			
		StepVerifier.create(authResult).expectError();
	}
	
}
