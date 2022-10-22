/**
 * 
 */
package com.keycloak.admin.client.repository;

import org.springframework.stereotype.Repository;

import com.keycloak.admin.client.entities.ActivationToken;
import com.keycloak.admin.client.repository.base.BaseRepository;

import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Repository
public interface UserActivationTokenRepository extends BaseRepository<ActivationToken, String> {

	Mono<ActivationToken> findByUsernameIgnoreCase(String username);

	Mono<ActivationToken> findByUsernameAndStatusAllIgnoreCase(String username, String status);

	Mono<ActivationToken> findByUsernameAndTokenAndStatusAllIgnoreCase(String username, String token, String status);

	//Mono<ActivationToken> findByUsernameIgnoreCaseAndToken(String username, String token);

}
