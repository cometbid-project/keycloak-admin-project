/**
 * 
 */
package com.keycloak.admin.client.repository;

import org.springframework.stereotype.Repository;
import com.keycloak.admin.client.entities.NewLocationToken;
import com.keycloak.admin.client.repository.base.BaseRepository;

import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Repository
public interface NewLocationTokenRepository extends BaseRepository<NewLocationToken, String> {

	// Done
	Mono<NewLocationToken> findByToken(String token);

	// Done
	Mono<NewLocationToken> findByUsernameIgnoreCaseAndStatus(String username, String status);

}
