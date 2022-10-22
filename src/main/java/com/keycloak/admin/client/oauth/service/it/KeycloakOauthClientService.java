/**
 * 
 */
package com.keycloak.admin.client.oauth.service.it;

import java.util.List;

import org.keycloak.representations.idm.UserRepresentation;

import com.keycloak.admin.client.models.AuthenticationRequest;
import com.keycloak.admin.client.models.AuthenticationResponse;
import com.keycloak.admin.client.models.PagingModel;
import com.keycloak.admin.client.models.PasswordUpdateRequest;
import com.keycloak.admin.client.models.ProfileStatusUpdateRequest;
import com.keycloak.admin.client.models.SearchUserRequest;
import com.keycloak.admin.client.models.SocialLink;
import com.keycloak.admin.client.models.StatusUpdateRequest;
import com.keycloak.admin.client.models.UserDetailsUpdateRequest;
import com.keycloak.admin.client.models.UserVO;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
public interface KeycloakOauthClientService {

	/**
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	Mono<AuthenticationResponse> passwordGrantLogin(AuthenticationRequest authRequest);

	/**
	 * 
	 * @param username
	 * @param targetClientId
	 * @return
	 */
	Mono<String> generateToken(String username, String targetClientId);

	/**
	 * 
	 * @param username
	 * @return
	 */
	Mono<UserRepresentation> findUserByUsername(String username);

	/**
	 * 
	 * @param passwdUpd
	 * @param userRepresentation
	 * @return
	 */
	Mono<String> saveNewPassword(PasswordUpdateRequest passwdUpd, UserRepresentation userRepresentation);

	/**
	 * Create a new User
	 * 
	 * @param request
	 * @param roleList
	 * @return
	 */
	Mono<String> createOauthUser(UserRepresentation newUser, String password, SocialLink socialLink);

	/**
	 * Create a new User
	 * 
	 * @param request
	 * @param roleList
	 * @return
	 */
	Mono<String> updateUserMfa(String username, boolean enable2fa);

	/**
	 * 
	 * @param userRepresentation
	 * @return
	 */
	boolean isTotpEnabled(UserRepresentation userRepresentation);

	/**
	 * 
	 * @param username
	 */
	Mono<String> assignClientRole(String id, String clientId, String roleName);

	/**
	 * 
	 * @param username
	 * @return
	 */
	Mono<String> assignRealmRole(String id, String roleName);

	/**
	 * 
	 * @param username
	 * @param groupId
	 * @return
	 */
	Mono<String> assignUserToGroup(String id, String groupId);

	/**
	 * 
	 * @param status
	 */
	Mono<Void> logout(String username, String refreshToken);

	/**
	 * Update User details
	 * 
	 * @return
	 */
	//Mono<UserVO> updateOauthUser(UserRepresentation userDetails, String username);

	/**
	 * 
	 * @param userDetails
	 * @param email
	 * @return
	 */
	Mono<UserVO> updateOauthUser(UserDetailsUpdateRequest userDetails, String username);

	/**
	 * 
	 * @param status
	 * @return
	 */
	Mono<String> updateEmailStatus(String username, boolean setVerified);

	/**
	 * @return
	 * 
	 */
	Mono<String> resetPassword(String username, String newPassword);

	/**
	 * 
	 * @param username
	 * @param status
	 * @return
	 */
	Mono<String> updateUserStatus(StatusUpdateRequest statusRequest);

	/**
	 * 
	 * @param username
	 * @return
	 */
	Mono<List<UserRepresentation>> findAllUsers(PagingModel pageModel);
	
	/**
	 * 
	 * @param pageModel
	 * @param searchFields
	 * @return
	 */
	Mono<List<UserRepresentation>> search(PagingModel pageModel, SearchUserRequest searchFields);

	/**
	 * 
	 * @param username
	 * @return
	 */
	Flux<UserRepresentation> findAllUsers();  

	/**
	 * 
	 * @return
	 */
	Mono<String> doMfaValidation(AuthenticationResponse authResponse, String totpCode);
	
	/**
	 * 
	 * @param username
	 * @return
	 */
	Mono<UserRepresentation> findUserById(String id);

	/**
	 * 
	 * @param email
	 * @return
	 */
	Mono<List<UserRepresentation>> findUserByEmail(String email, PagingModel pageModel);
	
	/**
	 * 
	 * @param email
	 * @return
	 */
	Mono<UserRepresentation> findUserByEmail(String email);

	/**
	 * 
	 * @param emailVerified
	 * @param pageModel
	 * @return
	 */
	Mono<List<UserRepresentation>> findUsersWithVerifiedEmails(boolean emailVerified, PagingModel pageModel);
	
	/**
	 * 
	 * @param userDetails
	 * @return
	 */
	Mono<String> enableOauthUser(ProfileStatusUpdateRequest profileStatus);

	/**
	 * 
	 * @param otpCode
	 * @param totpCode
	 * @param username
	 * @return
	 */
	//Mono<String> validateMFACode(String otpCode, String totpCode, String username);

}
