/**
 * 
 */
package com.keycloak.admin.client.oauth.service.it;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.keycloak.admin.client.models.AuthenticationRequest;
import com.keycloak.admin.client.models.AuthenticationResponse;
import com.keycloak.admin.client.models.EmailStatusUpdateRequest;
import com.keycloak.admin.client.models.EnableTwoFactorAuthResponse;
import com.keycloak.admin.client.models.SendOtpRequest;
import com.keycloak.admin.client.models.ProfileStatusUpdateRequest;
import com.keycloak.admin.client.models.StatusUpdateRequest;
import com.keycloak.admin.client.models.TotpRequest;
import com.keycloak.admin.client.models.UserDetailsUpdateRequest;
import com.keycloak.admin.client.models.UserVO;

import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
public interface UserAuthenticationService {

	/**
	 * 
	 * @param id
	 * @param username
	 * @param password
	 * @return Mono<UserVO>
	 */
	Mono<AuthenticationResponse> authenticate(AuthenticationRequest authRequest, ServerHttpRequest r);

	/**
	 * Signal failed login attempts to minimize Denial of Service attack. As soon as
	 * maximum attempt is reached throws a RuntimeException
	 *
	 * @param ipAddress
	 * @return Mono<Void>
	 *
	 */
	Mono<Long> incrementFailedLogins(final String ipAddress, final String username);

	/**
	 * 
	 * @param totpRequest
	 * @param r
	 * @return
	 */
	Mono<AuthenticationResponse> verifyTotpCode(TotpRequest totpRequest, ServerHttpRequest r);
	
	/**
	 * 
	 * @param otpRequest
	 * @param r
	 * @return
	 */
	Mono<String> sendOtpCode(SendOtpRequest otpRequest, ServerHttpRequest r);

	/**
	 * User must be authenticated
	 * 
	 * @param authRequest
	 * @return
	 */
	Mono<EnableTwoFactorAuthResponse> updateTwoFactorAuth(String username, boolean enable2fa);

	/**
	 * @return
	 * 
	 */
	Mono<String> logout(String username, String refreshToken);

	/**
	 * 
	 */
	Mono<String> updateUserStatus(StatusUpdateRequest statusUpdate);

	/**
	 * 
	 */
	Mono<UserVO> updateUserDetails(String username, UserDetailsUpdateRequest userDetailsUpdate);

	/**
	 * 
	 * @param statusUpdate
	 * @return
	 */
	Mono<String> updateEmailStatus(EmailStatusUpdateRequest statusUpdate);

	/**
	 * 
	 * @param statusUpdate
	 * @return
	 */
	Mono<String> enableUserProfile(ProfileStatusUpdateRequest statusUpdate);

	/**
	 * 
	 */
	Mono<AuthenticationResponse> refreshToken(String username, String refreshToken);    

}
