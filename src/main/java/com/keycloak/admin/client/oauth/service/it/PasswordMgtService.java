/**
 * 
 */
package com.keycloak.admin.client.oauth.service.it;

import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.server.ServerRequest;

import com.keycloak.admin.client.models.ForgotUsernameRequest;
import com.keycloak.admin.client.models.PasswordResetTokenResponse;
import com.keycloak.admin.client.models.PasswordUpdateRequest;
import com.keycloak.admin.client.models.ResetPasswordFinalRequest;
import com.keycloak.admin.client.models.ResetPasswordRequest;
import lombok.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
public interface PasswordMgtService {

	/**
	 * 
	 * @param userModel
	 * @return
	 */
	Mono<String> recoverUsername(ForgotUsernameRequest userModel, ServerHttpRequest httpRequest);

	/**
	 * User must be Authenticated.
	 * 
	 * differs from {@link #resetUserPassword(ResetPasswordRequest)} in that, this method
	 * is used by authenticated users who is intending to change it
	 * 
	 * @param passwdUpd
	 * @return
	 */
	Mono<String> changePassword(@NonNull PasswordUpdateRequest passwdUpd, String username, ServerHttpRequest r);

	/**
	 * Step 1 of Password Reset sequence
	 * 
	 * @param newPasswd
	 * @return
	 */
	Mono<String> initiateResetPasswd(@NonNull ResetPasswordRequest newPasswordRequest, ServerHttpRequest r);

	/**
	 * Step 2 of Password Reset sequence
	 * 
	 * @param token
	 * @return
	 */
	Mono<PasswordResetTokenResponse> validatePasswordResetToken(String token);  

	/**
	 * Step 3 of Password Reset sequence
	 * 
	 * differs from {@link #changePassword(PasswordUpdateRequest)} in that, this
	 * method is used by unauthenticated users who forgot their login credentials
	 * 
	 * @param passwdUpd
	 * @return
	 */
	Mono<String> resetUserPassword(@NonNull ResetPasswordFinalRequest passwordUpd, ServerHttpRequest r);

	/**
	 * Invoked by the Cleanup Scheduler to rid the database of Expired Password
	 * Reset token records
	 */
	Mono<Long> removeExpiredPasswordResetTokenRecords();

	/**
	 * Invoked by the Scheduler to expire Password Reset token records to mark them
	 * for removal
	 */  
	Mono<Long> expirePasswordResetTokenRecords(); 

	/**
	 * Invoked by the Scheduler to expire Batch of Users due for Password
	 * expiration. Expired Users will be required to login 
	 */
	Flux<Flux<String>> expireUserProfilePasswordRecords();  

}
