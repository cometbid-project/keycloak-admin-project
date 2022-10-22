/**
 * 
 */
package com.keycloak.admin.client.repository;

import java.util.Date;
import org.springframework.stereotype.Repository;

import com.keycloak.admin.client.entities.PasswordResetToken;
import com.keycloak.admin.client.repository.base.BaseRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Repository
public interface PasswordResetTokenRepository extends BaseRepository<PasswordResetToken, String> {

	Mono<PasswordResetToken> findByUsernameIgnoreCaseAndToken(String username, String token);

	Mono<PasswordResetToken> findByUsernameIgnoreCase(String username);

	Flux<PasswordResetToken> findAllByExpiryTimeLessThan(Date now);

	void deleteByExpiryTimeLessThan(Date now);

	//Mono<PasswordResetToken> findByToken(String token);

	/*
	 * @Modifying
	 * 
	 * @Query("delete from PasswordResetToken t where t.expiryDate <= ?1") void
	 * deleteAllExpiredSince(Date now);
	 */
}
