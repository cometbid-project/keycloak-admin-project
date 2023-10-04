/**
 * 
 */
package com.keycloak.admin.client.oauth.service.it;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.keycloak.representations.UserInfo;
import org.keycloak.representations.idm.UserRepresentation;

import com.keycloak.admin.client.models.AuthenticationRequest;
import com.keycloak.admin.client.models.AuthenticationResponse;
import com.keycloak.admin.client.models.PagingModel;
import com.keycloak.admin.client.models.PasswordUpdateRequest;
import com.keycloak.admin.client.models.ProfileActivationUpdateRequest;
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
	Mono<AuthenticationResponse> passwordGrantLogin(@Valid AuthenticationRequest authRequest);

	/**
	 * 
	 * @param username
	 * @param targetClientId
	 * @return
	 */
	Mono<AuthenticationResponse> generateToken(@NotBlank String username, @NotBlank String targetClientId);

	/**
	 * 
	 * @param username
	 * @return
	 */
	Mono<UserRepresentation> findUserByUsername(@NotBlank String username);

	/**
	 * 
	 * @param firstName
	 * @return
	 */
	Mono<List<UserRepresentation>> findUserByFirstName(final String firstName);

	/**
	 * 
	 * @param lastName
	 * @return
	 */
	Mono<List<UserRepresentation>> findUserByLastName(final String lastName);

	/**
	 * 
	 * @param passwdUpd
	 * @param userRepresentation
	 * @return
	 */
	Mono<String> saveNewPassword(@Valid PasswordUpdateRequest passwdUpd, UserRepresentation userRepresentation);

	/**
	 * Create a new User
	 * 
	 * @param request
	 * @param roleList
	 * @return
	 */
	Mono<String> createOauthUser(UserRepresentation newUser, @NotBlank String password, @NotNull SocialLink socialLink);

	/**
	 * Create a new User
	 * 
	 * @param request
	 * @param roleList
	 * @return
	 */
	Mono<String> updateUserMfa(@NotBlank String username, boolean enable2fa);

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
	Mono<String> assignUserClientRole(String userId, String clientId, String roleName);

	/**
	 * 
	 * @param username
	 * @return
	 */
	Mono<String> assignUserRealmRole(@NotBlank String userId, @NotBlank String roleName);

	/**
	 * 
	 * @param username
	 * @param groupId
	 * @return
	 */
	Mono<String> assignUserToGroup(@NotBlank String userId, @NotBlank String groupId);

	/**
	 * 
	 * @param status
	 */
	Mono<Void> logout(@NotBlank String userId, @NotBlank String refreshToken);

	/**
	 * 
	 * @param username
	 * @param refreshToken
	 * @return
	 */
	Mono<Void> signout(@NotBlank String username, @NotBlank String refreshToken);

	/**
	 * Update User details
	 * 
	 * @return
	 */
	Mono<UserVO> updateOauthUserById(@Valid UserDetailsUpdateRequest userDetails, @NotBlank String userId);

	/**
	 * 
	 * @param userDetails
	 * @param email
	 * @return
	 */
	Mono<UserVO> updateOauthUser(@Valid UserDetailsUpdateRequest userDetails, @NotBlank String username);

	/**
	 * 
	 * @param status
	 * @return
	 */
	Mono<String> updateEmailStatus(@NotBlank String username, boolean setVerified);

	/**
	 * @return
	 * 
	 */
	Mono<String> resetPassword(@NotBlank String username, @NotBlank String newPassword);

	/**
	 * 
	 * @param username
	 * @param status
	 * @return
	 */
	Mono<String> updateUserStatus(@NotBlank String username, @Valid StatusUpdateRequest statusRequest);

	/*
	 * 
	 * 
	 */
	Mono<String> updateUserByIdStatus(@NotBlank String userId, @Valid StatusUpdateRequest statusRequest);

	/**
	 * 
	 * @param username
	 * @return
	 */
	Mono<List<UserRepresentation>> findAllUsers(@NotNull PagingModel pageModel);

	/**
	 * 
	 * @param pageModel
	 * @param searchFields
	 * @return
	 */
	Mono<List<UserRepresentation>> search(@NotNull PagingModel pageModel, @Valid SearchUserRequest searchFields);

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
	Mono<String> doMfaValidation(@Valid AuthenticationResponse authResponse, @NotBlank String totpCode);

	/**
	 * 
	 * @param username
	 * @return
	 */
	Mono<UserRepresentation> findUserById(@NotBlank String id);

	/**
	 * 
	 * @param email
	 * @return
	 */
	Mono<List<UserRepresentation>> findUserByEmail(@NotBlank String email, @NotNull PagingModel pageModel);

	/**
	 * 
	 * @param email
	 * @return
	 */
	Mono<UserRepresentation> findUserByEmail(@NotBlank String email);

	/**
	 * 
	 * @param emailVerified
	 * @param pageModel
	 * @return
	 */
	Mono<List<UserRepresentation>> findUsersWithVerifiedEmails(boolean emailVerified, @NotNull PagingModel pageModel);

	/**
	 * 
	 * @param userDetails
	 * @return
	 */
	Mono<String> enableOauthUser(@Valid ProfileActivationUpdateRequest profileStatus);

	/**
	 * 
	 * @param userId
	 * @return
	 */
	Mono<String> deleteAppUser(String userId);

	/**
	 * 
	 * @param status
	 */
	Mono<AuthenticationResponse> refreshToken(String username, String refreshToken);

	/**
	 * 
	 * @param status
	 */
	Mono<UserInfo> userInfo(String accessToken);

	/**
	 * 
	 * @param token
	 * @return
	 * @throws Exception 
	 */
	Mono<List<String>> getUserRealmRoles(@NotBlank String accessToken);

	/**
	 * 
	 * @param token
	 * @return
	 * @throws Exception
	 */
	Mono<Boolean> validateToken(@NotBlank String accessToken);

	/**
	 * 
	 * @param token
	 * @return
	 * @throws Exception
	 */
	Mono<Map<String, List<String>>> getUserClientRoles(@NotBlank String accessToken);

	/**
	 * 
	 * @param status
	 */
	Mono<String> revokeAccessToken(@NotBlank String accessToken);

	/**
	 * 
	 * @param user
	 * @return
	 */
	Mono<String> expireUserPassword(UserRepresentation userRepresentation);


	/**
	 * 
	 * @param otpCode
	 * @param totpCode
	 * @param username
	 * @return
	 */
	// Mono<String> validateMFACode(String otpCode, String totpCode, String
	// username);

}
