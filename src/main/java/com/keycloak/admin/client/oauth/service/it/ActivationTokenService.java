/**
 * 
 */
package com.keycloak.admin.client.oauth.service.it;

import org.springframework.http.server.reactive.ServerHttpRequest;

import com.keycloak.admin.client.entities.ActivationToken;
//import com.keycloak.admin.client.models.ActivationTokenModel;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
public interface ActivationTokenService {

	/**
	 * 
	 */
	Mono<Long> expireActivationTokenRecords();

	/**
	 * 
	 */
	Mono<Long> removeExpiredActivationTokenRecords();

	/**
	 *
	 * @param userId
	 * @return
	 */
	Mono<ActivationToken> generateEmailVerificationToken(String username);

	/**
	 *
	 * @param token
	 * @return
	 */
	Mono<String> renewActivationToken(String token, ServerHttpRequest r);

	/**
	 *
	 * @param token
	 * @return
	 */
	Mono<String> validateEmailActivationToken(String activationToken);

}
