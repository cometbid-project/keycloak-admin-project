/**
 * 
 */
package com.keycloak.admin.client.oauth.service;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.keycloak.admin.client.models.AuthenticationResponse;
import com.keycloak.admin.client.redis.service.ReactiveRedisComponent;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 * 
 *         Helper class handling runtime dynamic evaluation of User Permissions
 * 
 */
@Log4j2
@Service
public class AuthServiceRedisCache {

	private final Environment environment;
	private Long totpSessionTTL;

	private final ReactiveRedisComponent<AuthenticationResponse> cacheUtil;

	public AuthServiceRedisCache(@Qualifier("redis") ReactiveRedisComponent<AuthenticationResponse> cacheUtil,
			Environment environment) {
		this.cacheUtil = cacheUtil;
		this.environment = environment;

		init();
	}

	private void init() {
		String configTotpTimeout = environment.getProperty("totp.session.ttl");
		this.totpSessionTTL = StringUtils.isNotBlank(configTotpTimeout) ? Long.valueOf(configTotpTimeout) : 1800L;
		log.info("Totp Session Time to live {}", totpSessionTTL);
	}

	/**
	 * 
	 * @param totpSessionId
	 * @param authResponse
	 * @return
	 */
	public Mono<Boolean> saveAuthenticationResponse(String totpSessionId, AuthenticationResponse authResponse) {

		return cacheUtil.putPojo(totpSessionId, authResponse, totpSessionTTL);
	}

	/**
	 * 
	 * @param totpSessionId
	 * @return
	 */
	public Mono<AuthenticationResponse> getAuthenticationResponse(String totpSessionId) {

		return cacheUtil.getPojo(totpSessionId).flatMap(obj -> Mono.just(Optional.of(obj)))
				.defaultIfEmpty(Optional.empty()).flatMap(valueOptional -> {
					if (valueOptional.isPresent()) {
						AuthenticationResponse authResponse = (AuthenticationResponse) valueOptional.get();

						return Mono.just(authResponse);
					}

					return Mono.just(new AuthenticationResponse());
				});
	}
}
