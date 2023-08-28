/**
 * 
 */
package com.keycloak.admin.client.oauth.service;

import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.UserInfo;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.SocialLinkRepresentation;
import static com.keycloak.admin.client.config.AuthProperties.*;
import static com.keycloak.admin.client.error.helpers.ErrorPublisher.*;
import static com.keycloak.admin.client.error.handlers.ExceptionHandler.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.keycloak.admin.client.common.enums.StatusType;
import com.keycloak.admin.client.common.utils.DateUtil;
import com.keycloak.admin.client.common.utils.RandomGenerator;
import com.keycloak.admin.client.common.utils.StringUtil;
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;
import com.keycloak.admin.client.config.AuthProfile;
import com.keycloak.admin.client.config.AuthProperties;
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
import com.keycloak.admin.client.models.mappers.UserMapper;
import com.keycloak.admin.client.oauth.service.it.KeycloakOauthClientService;
import com.keycloak.admin.client.token.utils.KeycloakJwtTokenUtil;
import com.keycloak.admin.client.token.utils.TotpManager;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.core.Response;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Validated
@Component("keycloak-client")
public class KeycloakOauthClient implements KeycloakOauthClientService {

	private final PasswordEncoder passwordEncoder;
	private final KeycloakRestService keycloakService;
	private final TotpManager totpManager;
	private final CustomMessageSourceAccessor i8nMessageAccessor;
	private final AuthProfile dataStore;
	private final AuthProperties authProperties;
	private final GatewayRedisCache redisCacheUtil;
	private final Keycloak keycloak;

	public KeycloakOauthClient(Keycloak keycloak, AuthProperties authProperties,
			CustomMessageSourceAccessor i8nMessageAccessor, @Qualifier("TotpManager") TotpManager totpManager,
			GatewayRedisCache redisCacheUtil, KeycloakRestService keycloakService, PasswordEncoder passwordEncoder,
			AuthProfile dataStore) {

		this.passwordEncoder = passwordEncoder;
		this.keycloakService = keycloakService;
		this.totpManager = totpManager;
		this.i8nMessageAccessor = i8nMessageAccessor;
		this.dataStore = dataStore;
		this.authProperties = authProperties;
		this.redisCacheUtil = redisCacheUtil;
		this.keycloak = keycloak;
	}

	// @PostConstruct
	private RealmResource realmResource() {
		// Get realm role "tester" (requires view-realm role)
		final String appRealm = this.authProperties.getAppRealm();

		// keycloak.tokenManager().getAccessToken();
		return keycloak.realm(appRealm);
	}

	private UserRepresentation getUserRepresentation(String username) {
		UserResource userResource = getUserResource(username);

		return Objects.isNull(userResource) ? null : userResource.toRepresentation();
	}

	private UserResource getUserResource(String username) {
		boolean exact = true;
		List<UserRepresentation> userRepresentationList = this.realmResource().users().search(username, exact);

		if (!userRepresentationList.isEmpty()) {
			String userId = userRepresentationList.get(0).getId();

			return this.realmResource().users().get(userId);
		}

		return null;
	}

	private UserResource getUserResourceById(String id) {

		return this.realmResource().users().get(id);
	}

	private RoleRepresentation getRealmRoleRepresentation(String roleName) {
		// Get realm role "tester" (requires view-realm role)
		return realmResource().roles()
				//
				.get(roleName).toRepresentation();
	}

	private GroupRepresentation getGroupRepresentation(String groupId) {
		// Get realm role "tester" (requires view-realm role)
		return realmResource().groups()
				//
				.group(groupId).toRepresentation();
	}

	private RoleRepresentation getClientRoleRepresentation(ClientRepresentation app1Client, String roleName) {
		// Get client level role (requires view-clients role)
		return realmResource().clients().get(app1Client.getId())
				//
				.roles().get(roleName).toRepresentation();
	}

	private SocialLinkRepresentation createSocialLinkRepresentation(String providerUserId, String providerType) {
		SocialLinkRepresentation socialLink = new SocialLinkRepresentation();
		socialLink.setSocialProvider(providerUserId);
		socialLink.setSocialUserId(providerType);
		socialLink.setSocialUsername(null);

		return socialLink;
	}

	private ClientRepresentation getClientRepresentation(String clientId) {
		// Get client
		return realmResource().clients()
				//
				.findByClientId(clientId).get(0);
	}

	/**
	 * 
	 * @param username
	 * @param targetClientId
	 * @return
	 */
	@Override
	public Mono<AuthenticationResponse> generateToken(final String username, final String targetClientId) {

		String startingClientId = authProperties.getAdminClientId();
		String startingClientSecret = authProperties.getAdminClientSecret();

		log.info("Admin Client id {}", startingClientId);
		log.info("Admin Client Secret {}", startingClientSecret);

		return this.getAdminClientToken(username).flatMap(accessToken -> keycloakService
				.doTokenExchange(accessToken, startingClientId, startingClientSecret, username, targetClientId)
				.map(tokenGen -> KeycloakJwtTokenUtil.generateLoginResponse(tokenGen, new ArrayList<>(), username)));

	}

	/**
	 * 
	 * @param authRequest
	 * @return
	 */
	private Mono<String> getAdminClientToken(String username) {

		return Mono.fromCallable(() -> this.keycloak.tokenManager().getAccessTokenString())
				.switchIfEmpty(raiseBadCredentials("invalid.credentials", new Object[] {}));
	}

	/**
	 * 
	 * @param username
	 * @return
	 */
	@Override
	public Mono<List<UserRepresentation>> search(final PagingModel pageModel, final SearchUserRequest searchFields) {

		int pageNo = pageModel.getPageNo();
		int pageSize = pageModel.getPageSize();
		int firstResult = pageNo * pageSize;

		boolean userEnabled = true;
		boolean briefRepresentation = true;

		String username = searchFields.getUsername();
		String firstName = searchFields.getFirstName();
		String lastName = searchFields.getLastName();
		String email = searchFields.getEmail();
		boolean emailVerified = searchFields.isEmailVerified();

		// Mono<List<UserRepresentation>> emptyUserList = Mono.just(new ArrayList<>());
		// Create the user resource
		return Mono
				.fromCallable(() -> this.realmResource().users().search(username, firstName, lastName, email,
						emailVerified, firstResult, pageSize, userEnabled, briefRepresentation))
				.switchIfEmpty(Mono.just(new ArrayList<>()));
	}

	/**
	 * 
	 * @param username
	 * @return
	 */
	@Override
	public Mono<List<UserRepresentation>> findUsersWithVerifiedEmails(boolean emailVerified,
			final PagingModel pageModel) {
		// String appRealm = authProperties.getAppRealm();

		int pageNo = pageModel.getPageNo();
		int pageSize = pageModel.getPageSize();
		int firstResult = pageNo * pageSize;

		boolean userEnabled = true;
		boolean briefRepresentation = true;

		// Create the user resource
		return Mono.fromCallable(() -> this.realmResource().users().search(emailVerified, firstResult, pageSize,
				userEnabled, briefRepresentation));
	}

	/**
	 * 
	 * @param username
	 * @return
	 */
	@Override
	public Mono<List<UserRepresentation>> findAllUsers(final PagingModel pageModel) {
		// String appRealm = authProperties.getAppRealm();

		int pageNo = pageModel.getPageNo();
		int pageSize = pageModel.getPageSize();
		int firstResult = pageNo * pageSize;

		return Mono.fromCallable(() -> this.realmResource().users().list(firstResult, pageSize));
	}

	/**
	 * 
	 * @param username
	 * @return
	 */
	@Override
	public Flux<UserRepresentation> findAllUsers() {

		return Mono.fromCallable(() -> this.realmResource().users().list()).flatMapMany(Flux::fromIterable);
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public Mono<UserRepresentation> findUserById(final String id) {

		return Mono.fromCallable(() -> getUserResourceById(id)).flatMap(userResource -> {
			UserRepresentation user = userResource.toRepresentation();
			// Do Profile checks on status
			return doProfileValidation(user).thenReturn(user);
		});
	}

	/**
	 * 
	 * @param username
	 * @return
	 */
	@Override
	public Mono<UserRepresentation> findUserByUsername(final String username) {

		return Mono.fromCallable(() -> getUserResource(username)).flatMap(userResource -> {
			UserRepresentation user = userResource.toRepresentation();
			// Do Profile checks on status
			return doProfileValidation(user).thenReturn(user);
		}).switchIfEmpty(raiseResourceNotFoundError("user.notFound", new Object[] { username }));
	}

	/**
	 * 
	 * @param token
	 * @return
	 * @throws Exception
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Mono<List<String>> getUserRealmRoles(@NotBlank final String accessToken) {

		Mono<UserInfo> userInfoResponse = keycloakService.getUserInfo(accessToken);

		return userInfoResponse.flatMap(userInfo -> findUserById(userInfo.getSubject())).map(p -> {
			log.info("UserRep username: {}", p.getUsername());
			log.info("UserRep roles: {}", p.getRealmRoles());

			List<String> roles = p.getRealmRoles() == null ? new ArrayList<>() : p.getRealmRoles();
			return roles;
		});// .flatMapMany(Flux::fromIterable);
	}

	/**
	 * 
	 * @param token
	 * @return
	 * @throws Exception
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Mono<Boolean> validateToken(@NotBlank final String accessToken) {

		return keycloakService.checkValidity(accessToken);
	}

	/**
	 * 
	 * @param token
	 * @return
	 * @throws Exception
	 */

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Mono<Map<String, List<String>>> getUserClientRoles(@NotBlank final String accessToken) {

		Mono<UserInfo> userInfoResponse = keycloakService.getUserInfo(accessToken);

		return userInfoResponse.flatMap(userInfo -> findUserById(userInfo.getSubject())).map(p -> {
			log.info("UserRep username: {}", p.getUsername());
			log.info("UserRep roles: {}", p.getClientRoles());

			Map<String, List<String>> roles = p.getClientRoles() == null ? new HashMap<>() : p.getClientRoles();
			return roles;
		});
	}

	/**
	 * 
	 * @param email
	 * @return
	 */
	@Override
	public Mono<List<UserRepresentation>> findUserByEmail(final String email, final PagingModel pageModel) {

		int firstResult = pageModel.getPageNo();
		int maxResult = pageModel.getPageSize();

		// Create the user resource
		return Mono.fromCallable(() -> this.realmResource().users().search(email, firstResult, maxResult));
	}

	/**
	 * 
	 * @param email
	 * @return
	 */
	@Override
	public Mono<UserRepresentation> findUserByEmail(String email) {
		// Create the user resource

		return Mono.fromCallable(() -> {
			boolean exact = true;
			List<UserRepresentation> userRepresentationList = this.realmResource().users().searchByEmail(email, exact);

			if (!userRepresentationList.isEmpty()) {
				return userRepresentationList.get(0);
			}
			return null;
		});
	}

	/**
	 * 
	 * @param email
	 * @return
	 */
	@Override
	public Mono<List<UserRepresentation>> findUserByFirstName(final String firstName) {

		// Create the user resource
		return Mono.fromCallable(() -> {
			boolean exact = true;
			return this.realmResource().users().searchByFirstName(firstName, exact);
		});
	}

	/**
	 * 
	 * @param email
	 * @return
	 */
	@Override
	public Mono<List<UserRepresentation>> findUserByLastName(final String lastName) {

		// Create the user resource
		return Mono.fromCallable(() -> {
			boolean exact = true;
			return this.realmResource().users().searchByLastName(lastName, exact);
		});
	}

	/**
	 * 
	 * @param passwdUpd
	 * @param userRepresentation
	 * @return
	 */
	@Override
	public Mono<String> saveNewPassword(final PasswordUpdateRequest passwdUpd,
			final UserRepresentation userRepresentation) {
		// Checking Old Password matches current User password
		List<CredentialRepresentation> credentialList = userRepresentation.getCredentials();
		CredentialRepresentation currentCredential = credentialList.get(0);

		String hashedPassword = currentCredential.getValue();
		if (!isPasswordEqual(passwdUpd.getOldPassword(), hashedPassword)) {
			return raisePasswordUnacceptableException("user.password.notMatchCurrent", new Object[] {});
		}

		String newPlainTextPassword = passwdUpd.getNewPassword();
		if (isPasswordUsedBefore(userRepresentation, newPlainTextPassword)) {
			return raisePasswordUnacceptableException("user.password.matchPrevious", new Object[] {});
		}

		addNewPasswordToHistory(userRepresentation, newPlainTextPassword);

		// Assumption: Successful change of password make Expired Accounts change to
		// Valid State
		// PreCondition is now met
		userRepresentation.singleAttribute(PROFILE_EXPIRED, Boolean.FALSE.toString());

		return Mono.fromCallable(() -> {
			String username = userRepresentation.getUsername();
			UserResource userResource = getUserResource(username);

			if (userResource == null) {
				return raiseResourceNotFoundError("user.notFound", new Object[] { username });
			}

			userResource.update(userRepresentation);
			return "done";
		}).thenReturn(i8nMessageAccessor.getLocalizedMessage("change.password.message"));
	}

	private boolean isPasswordEqual(String plainTextPassword, String currentPassword) {

		boolean result = passwordEncoder.matches(plainTextPassword, currentPassword);
		log.info("Password match**************** {}", result);
		return result;
	}

	private boolean isPasswordUsedBefore(UserRepresentation user, String newPlainTextPassword) {

		Set<String> pastUsedPasswords = user.getDisableableCredentialTypes();

		if (pastUsedPasswords != null) {
			return pastUsedPasswords.stream()
					.anyMatch(hashedPassword -> isPasswordEqual(newPlainTextPassword, hashedPassword));
		}

		return false;
	}

	private void addNewPasswordToHistory(UserRepresentation userRepresentation, String newPlainPassword) {

		final int maxAllowedPasswordHistory = (int) dataStore.getMaximumPasswordHistory();

		Set<String> pastCredentials = userRepresentation.getDisableableCredentialTypes();
		if (pastCredentials == null) {
			pastCredentials = new HashSet<>();
		}

		Collection<String> passwdHistory = pastCredentials;
		log.info("Password history {}", passwdHistory);

		String encodedPassword = passwordEncoder.encode(newPlainPassword);
		log.info("Encoded password {}", encodedPassword);

		if (passwdHistory.size() >= maxAllowedPasswordHistory) {
			CircularFifoQueue<String> fifoQueue = new CircularFifoQueue<>(maxAllowedPasswordHistory);
			fifoQueue.addAll(passwdHistory);
			fifoQueue.add(encodedPassword);

			passwdHistory.clear();
			passwdHistory.addAll(fifoQueue);
		} else {
			passwdHistory.add(encodedPassword);
		}

		Set<String> newCredentialsList = passwdHistory.stream().collect(Collectors.toSet());
		userRepresentation.setDisableableCredentialTypes(newCredentialsList);
	}

	/**
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	@Override
	public Mono<AuthenticationResponse> passwordGrantLogin(final AuthenticationRequest authRequest) {

		Mono<AccessTokenResponse> monoAccessCode = login(authRequest)
				.switchIfEmpty(raiseBadCredentials("invalid.credentials", new Object[] {}));

		return monoAccessCode.flatMap(accessCode -> {
			String username = authRequest.getUsername();

			Mono<UserRepresentation> userRep = Mono.fromCallable(() -> getUserRepresentation(username));

			return userRep.flatMap(user -> {
				// Do Profile checks on status
				return doProfileValidation(user).then(buildAuthentication(user, accessCode));
			});
		});
	}

	private Mono<AuthenticationResponse> buildAuthentication(UserRepresentation userRep,
			AccessTokenResponse accessCode) {
		String username = userRep.getUsername();
		List<String> roles = userRep.getRealmRoles();

		AuthenticationResponse authResponse = null;

		if (isTotpEnabled(userRep)) {
			final String totpSessionId = RandomGenerator.generateSessionId();

			AuthenticationResponse authToSave = KeycloakJwtTokenUtil.generateLoginResponse(accessCode, roles, username);

			// Save totpSessionId & Original Authentication details in RedisCache
			redisCacheUtil.saveAuthenticationResponse(totpSessionId, authToSave);

			// Send back totpsession id excluding token
			authResponse = new AuthenticationResponse(username, totpSessionId);
		} else {

			// Send back access token
			authResponse = KeycloakJwtTokenUtil.generateLoginResponse(accessCode, roles, username);
		}
		return Mono.just(authResponse);
	}

	/**
	 * Create a new User
	 * 
	 * @param request
	 * @param roleList
	 * @return
	 */
	@Override
	public Mono<String> createOauthUser(final UserRepresentation newUser, final String password,
			final SocialLink socialLink) {

		CredentialRepresentation credentialRep = preparePasswordRepresentation(password);
		UserRepresentation userRepresentation = prepareUserRepresentation(newUser, credentialRep, socialLink);

		return Mono.fromCallable(() -> {
			Response response = this.realmResource().users().create(userRepresentation);

			log.info("Response: {} {}", response.getStatus(), response.getStatusInfo());
			return response;
		}).map(response -> getCreatedUserId(response));
	}

	private String enableMFA(UserRepresentation newUser) {
		String totpSecret = this.totpManager.generateSecret();

		newUser.singleAttribute(TOTP_SECRET, totpSecret);
		newUser.singleAttribute(TOTP_ENABLED, Boolean.TRUE.toString());

		return totpSecret;
	}

	private String disableMFA(UserRepresentation newUser) {

		newUser.singleAttribute(TOTP_SECRET, null);
		newUser.singleAttribute(TOTP_ENABLED, Boolean.FALSE.toString());

		return null;
	}

	/**
	 * Create a new User
	 * 
	 * @param request
	 * @param roleList
	 * @return
	 */
	@Override
	public Mono<String> updateUserMfa(final String username, boolean enable2FA) {

		// find the user resource
		Mono<UserResource> userRep = Mono.fromCallable(() -> getUserResource(username))
				.switchIfEmpty(raiseResourceNotFoundError("user.notFound", new Object[] { username }));

		return userRep.flatMap(user -> doMfaUpdate(user, enable2FA));
	}

	private Mono<String> doMfaUpdate(UserResource userResource, boolean enable2FA) {

		return Mono.fromCallable(() -> {
			UserRepresentation userRepresentation = userResource.toRepresentation();

			String result = null;
			if (enable2FA) {
				result = enableMFA(userRepresentation);
			} else {
				result = disableMFA(userRepresentation);
			}

			userResource.update(userRepresentation);
			return result;
		});
	}

	/**
	 * 
	 * @param username
	 * @param status
	 * @return
	 */
	@Override
	public Mono<String> updateUserStatus(final String username, final StatusUpdateRequest statusRequest) {

		String status = statusRequest.getStatus();

		// Create the user resource
		Mono<UserResource> userRep = Mono.fromCallable(() -> getUserResource(username))
				.switchIfEmpty(raiseResourceNotFoundError("user.notFound", new Object[] { username }));

		return userRep.flatMap(user -> doStatusUpdate(user, status));
	}

	/**
	 * 
	 * @param userId
	 * @param statusRequest
	 * @return
	 */
	@Override
	public Mono<String> updateUserByIdStatus(final String userId, final StatusUpdateRequest statusRequest) {

		String status = statusRequest.getStatus();

		// Create the user resource
		Mono<UserResource> userRep = Mono.fromCallable(() -> getUserResourceById(userId))
				.switchIfEmpty(raiseResourceNotFoundError("user.notFound", new Object[] { userId }));

		return userRep.flatMap(user -> doStatusUpdate(user, status));
	}

	private Mono<String> doStatusUpdate(UserResource userResource, String status) {

		return Mono.fromRunnable(() -> {
			StatusType statusType = StatusType.fromString(status);
			UserRepresentation userRepresentation = userResource.toRepresentation();

			switch (statusType) {
			case LOCKED:
				userRepresentation.singleAttribute(PROFILE_EXPIRED, Boolean.FALSE.toString());
				userRepresentation.singleAttribute(PROFILE_LOCKED, Boolean.TRUE.toString());
				break;
			case EXPIRED:
				userRepresentation.singleAttribute(PROFILE_LOCKED, Boolean.FALSE.toString());
				userRepresentation.singleAttribute(PROFILE_EXPIRED, Boolean.TRUE.toString());
				break;
			case VALID:
				userRepresentation.singleAttribute(PROFILE_LOCKED, Boolean.FALSE.toString());
				userRepresentation.singleAttribute(PROFILE_EXPIRED, Boolean.FALSE.toString());
				break;
			default:
				log.warn("Invalid Status type passed as parameter to #updateUserStatus");
			}
			userResource.update(userRepresentation);
		});
	}

	/**
	 * @return
	 * 
	 */
	@Override
	public Mono<String> resetPassword(final String username, final String newPassword) {

		Mono<UserResource> userResource = Mono.fromCallable(() -> getUserResource(username))
				.switchIfEmpty(raiseResourceNotFoundError("user.notFound", new Object[] { username }));

		return userResource.flatMap(user -> doPasswordReset(user, newPassword));
	}

	private Mono<String> doPasswordReset(UserResource userResource, String newPassword) {
		// Define password credential
		CredentialRepresentation credential = new CredentialRepresentation();
		credential.setTemporary(false);
		credential.setType(CredentialRepresentation.PASSWORD);
		credential.setValue(newPassword);

		UserRepresentation userRepresentation = userResource.toRepresentation();
		addNewPasswordToHistory(userRepresentation, newPassword);

		return Mono.fromRunnable(() -> {
			userResource.resetPassword(credential);
			userResource.update(userRepresentation);
		}).thenReturn(i8nMessageAccessor.getLocalizedMessage("reset.password.success"));
		// "User password was reset successful"
	}

	/**
	 * 
	 * @param status
	 * @return
	 */
	@Override
	public Mono<String> updateEmailStatus(final String username, boolean setVerified) {

		Mono<UserResource> userResource = Mono.fromCallable(() -> getUserResource(username))
				.switchIfEmpty(raiseResourceNotFoundError("user.notFound", new Object[] { username }));

		return userResource.flatMap(user -> doEmailStatusUpdate(user, setVerified))
				.thenReturn(i8nMessageAccessor.getLocalizedMessage("auth.message.success"))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("change.emailstatus.error")));
	}

	private Mono<String> doEmailStatusUpdate(UserResource userResource, boolean setVerified) {
		// Create the user representation
		UserRepresentation userRepresentation = userResource.toRepresentation();
		userRepresentation.setEmailVerified(setVerified);

		return Mono.fromRunnable(() -> userResource.update(userRepresentation));
	}

	/**
	 * 
	 * @param status
	 */
	@Override
	public Mono<AuthenticationResponse> refreshToken(final String username, final String refreshToken) {

		return keycloakService.refreshAccessToken(username, refreshToken);
	}

	/**
	 * 
	 * @param status
	 */
	@Override
	public Mono<String> revokeAccessToken(final String accessToken) {

		return keycloakService.revokeAccessToken(accessToken)
				.thenReturn(i8nMessageAccessor.getLocalizedMessage("message.token-revocation.success"));
	}

	/**
	 * 
	 * @param status
	 */
	@Override
	public Mono<UserInfo> userInfo(final String accessToken) {

		return keycloakService.getUserInfo(accessToken);
	}

	/**
	 * 
	 * @param status
	 */
	@Override
	public Mono<Void> signout(final String username, final String refreshToken) {

		Mono<UserResource> userRep = Mono.fromCallable(() -> getUserResource(username))
				.switchIfEmpty(raiseResourceNotFoundError("user.notFound", new Object[] { username }));

		return userRep.flatMap(this::doLogout).and(keycloakService.logout(refreshToken));
	}

	/**
	 * 
	 * @param status
	 */
	@Override
	public Mono<Void> logout(final String userId, final String refreshToken) {

		Mono<UserResource> userRep = Mono.fromCallable(() -> getUserResourceById(userId))
				.switchIfEmpty(raiseResourceNotFoundError("user.notFound", new Object[] { userId }));

		return userRep.flatMap(this::doLogout).and(keycloakService.logout(refreshToken));
	}

	private Mono<Void> doLogout(UserResource userResource) {

		return Mono.fromRunnable(() -> userResource.logout());
	}

	/**
	 * 
	 * @param username
	 * @return
	 */
	@Override
	public Mono<String> assignUserRealmRole(final String id, final String roleName) {

		Mono<UserResource> userRep = Mono.fromCallable(() -> getUserResourceById(id))
				.switchIfEmpty(raiseResourceNotFoundError("user.notFound", new Object[] { id }));

		return userRep.flatMap(userResource -> assignRole(userResource, roleName)).thenReturn(i8nMessageAccessor
				.getLocalizedMessage("message.realmrole.assign.success", new Object[] { id, roleName }));
	}

	private Mono<String> assignRole(UserResource userResource, String roleName) {

		Mono<RoleRepresentation> roleRep = Mono.fromCallable(() -> getRealmRoleRepresentation(roleName))
				.switchIfEmpty(raiseResourceNotFoundError("role.notFound", new Object[] { roleName }));

		return roleRep.flatMap(newRealmRole -> {
			return Mono.fromRunnable(() -> userResource.roles().realmLevel() //
					.add(Arrays.asList(newRealmRole)));
		});
	}

	/**
	 * 
	 * @param groupId
	 * @return
	 */
	public Mono<String> assignUserToGroup(final String id, final String groupId) {

		Mono<UserResource> userRep = Mono.fromCallable(() -> getUserResourceById(id))
				.switchIfEmpty(raiseResourceNotFoundError("user.notFound", new Object[] { id }));

		return userRep.flatMap(userResource -> assignGroup(userResource, groupId)).thenReturn(
				i8nMessageAccessor.getLocalizedMessage("message.group.assign.success", new Object[] { id, groupId }));
	}

	private Mono<String> assignGroup(UserResource userResource, String groupId) {

		Mono<GroupRepresentation> groupRep = Mono.fromCallable(() -> getGroupRepresentation(groupId))
				.switchIfEmpty(raiseResourceNotFoundError("group.notFound", new Object[] { groupId }));

		return groupRep.flatMap(newGroup -> Mono.fromRunnable(() -> userResource.joinGroup(groupId)));
	}

	/**
	 * 
	 * @param username
	 */
	@Override
	public Mono<String> assignUserClientRole(final String id, final String clientId, final String roleName) {

		Mono<UserResource> userRep = Mono.fromCallable(() -> getUserResourceById(id))
				.switchIfEmpty(raiseResourceNotFoundError("user.notFound", new Object[] { id }));

		return userRep.flatMap(userResource -> assignClient(userResource, clientId, roleName))
				.thenReturn(i8nMessageAccessor.getLocalizedMessage("message.clientrole.assign.success",
						new Object[] { id, clientId, roleName }));
	}

	private Mono<Void> assignClient(UserResource userResource, String clientId, String roleName) {
		// Get client
		Mono<ClientRepresentation> monoApp1Client = Mono.fromCallable(() -> getClientRepresentation(clientId))
				.switchIfEmpty(raiseResourceNotFoundError("client.notFound", new Object[] { clientId }));

		return monoApp1Client.flatMap(app1Client -> assignRole(userResource, app1Client, roleName));
	}

	private Mono<Void> assignRole(UserResource userResource, ClientRepresentation app1Client, String roleName) {

		Mono<RoleRepresentation> roleRep = Mono.fromCallable(() -> getClientRoleRepresentation(app1Client, roleName))
				.switchIfEmpty(raiseResourceNotFoundError("clientRole.assign.error", new Object[] { roleName }));

		return roleRep.flatMap(userClientRole -> doRoleAssignment(userResource, app1Client, userClientRole));
	}

	private Mono<Void> doRoleAssignment(UserResource userResource, ClientRepresentation app1Client,
			RoleRepresentation userClientRole) {

		return Mono.fromRunnable(
				() -> userResource.roles().clientLevel(app1Client.getId()).add(Arrays.asList(userClientRole)));
	}

	private UserRepresentation prepareUserRepresentation(UserRepresentation user,
			CredentialRepresentation credRepresentation, SocialLink socialLink) {

		// Define the user
		UserRepresentation newUser = new UserRepresentation();
		newUser.setUsername(user.getUsername());
		newUser.setFirstName(user.getFirstName());
		newUser.setLastName(user.getLastName());
		newUser.setCreatedTimestamp(System.currentTimeMillis());
		newUser.setRequiredActions(new ArrayList<>());
		newUser.setEmail(newUser.getEmail());
		newUser.setEnabled(true);
		newUser.setCredentials(Arrays.asList(credRepresentation));

		List<String> roleNames = user.getRealmRoles().stream()
				.map(roleName -> getRealmRoleRepresentation(roleName).getName()).collect(Collectors.toList());
		newUser.setRealmRoles(roleNames);

		newUser.singleAttribute(PROFILE_LOCKED, Boolean.FALSE.toString());

		String socialProviderId = socialLink.getProviderUserId();
		if (StringUtils.isNotBlank(socialProviderId)) {
			newUser.setSocialLinks(Arrays.asList(createSocialLinkRepresentation(socialProviderId,
					socialLink.getSocialProvider().getProviderType())));
			// Set as True so that Social SignupUser are forced to change their password
			newUser.singleAttribute(PROFILE_EXPIRED, Boolean.TRUE.toString());
			// We assume the Provider did Email Verification
			newUser.setEmailVerified(true);
		} else {
			newUser.singleAttribute(PROFILE_EXPIRED, Boolean.FALSE.toString());
			newUser.setEmailVerified(false);
		}

		Set<String> pastCredentials = newUser.getDisableableCredentialTypes();
		if (CollectionUtils.isEmpty(pastCredentials)) {
			pastCredentials = new HashSet<>();
		}
		// add password to history
		String newPassword = credRepresentation.getValue();
		pastCredentials.add(passwordEncoder.encode(newPassword));
		newUser.setDisableableCredentialTypes(pastCredentials);

		return newUser;
	}

	private CredentialRepresentation preparePasswordRepresentation(String plainPassword) {
		CredentialRepresentation credentials = new CredentialRepresentation();
		credentials.setTemporary(false);
		credentials.setType(CredentialRepresentation.PASSWORD);

		String hashedPassword = passwordEncoder.encode(plainPassword);
		credentials.setValue(hashedPassword);

		credentials.setCreatedDate(System.currentTimeMillis());

		return credentials;
	}

	private String getCreatedUserId(Response response) {
		// URI location = response.getLocation();
		log.info("Response: {}-{}", response.getStatus(), response.getStatusInfo());
		log.info("Resource location {}", response.getLocation());

		return CreatedResponseUtil.getCreatedId(response);
	}

	/**
	 * 
	 * @param authRequest
	 * @return
	 */
	private Mono<AccessTokenResponse> login(AuthenticationRequest authRequest) {

		String username = authRequest.getUsername();
		String password = authRequest.getPassword();
		log.info("Username {}", username);
		log.info("Password {}", password);

		return keycloakService.login(username, password);
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Mono<String> doMfaValidation(final AuthenticationResponse authResponse, final String totpCode) {
		String otpCode = authResponse.getOtpCode();
		String username = authResponse.getUsername();

		Mono<UserRepresentation> userRepresentation = findUserByUsername(username);

		return userRepresentation
				.flatMap(user -> this.isTokenValid(user, otpCode, totpCode) ? Mono.just(AuthProperties.SUCCESS)
						: raiseBadCredentials("Invalid Totp code!", new Object[] {}));
	}

	/**
	 * 
	 * @param userRepresentation
	 * @return
	 */
	@Override
	public boolean isTotpEnabled(final UserRepresentation userRepresentation) {
		if (MapUtils.isEmpty(userRepresentation.getAttributes())) {
			return false;
		}

		List<String> totpEnabled = userRepresentation.getAttributes().getOrDefault(TOTP_ENABLED, new ArrayList<>());

		boolean enabled = false;
		if (!CollectionUtils.isEmpty(totpEnabled)) {
			enabled = Boolean.getBoolean(totpEnabled.get(0));
		}

		return enabled;
	}

	private boolean isTokenValid(UserRepresentation userRepresentation, final String otpCode, final String totpCode) {
		if (MapUtils.isEmpty(userRepresentation.getAttributes())) {
			return false;
		}

		List<String> totpSecret = userRepresentation.getAttributes().getOrDefault(TOTP_SECRET, new ArrayList<>());

		// String userId = userRepresentation.getId();
		Boolean validTotpCode = false;
		if (totpSecret.isEmpty()) {
			log.warn("Totp Secret for user {} not found", userRepresentation.getId());
			return validTotpCode;
		}
		// OTP Code is extracted from Cache entry
		// TOTP Code can either be an OTP Code or TOTP Code generated by custom library
		String formattedAuthCode = StringUtil.removeWhiteSpaces(totpCode);
		if (StringUtils.isNotBlank(otpCode)) {
			validTotpCode = otpCode.equals(formattedAuthCode);
		} else {
			validTotpCode = totpManager.validateCode(formattedAuthCode, totpSecret.get(0));
		}

		return validTotpCode;
	}

	private Mono<String> doProfileValidation(final UserRepresentation userRepresentation) {
		if (!userRepresentation.isEmailVerified()) {
			log.info("User's email is unverified");
			return raiseUserProfileUnverifiedException(new Object[] {});
		}

		log.info("User is Enabled? {}", userRepresentation.isEnabled());

		if (!userRepresentation.isEnabled()) {
			log.info("User's account is inactve");
			return raiseUserProfileDisabledException(new Object[] {});
		}

		/*
		if (MapUtils.isEmpty(userRepresentation.getAttributes())) {
			return raiseUserHasNoAttributesException(new Object[] {});
		}

		List<String> profileLocked = userRepresentation.getAttributes().getOrDefault(PROFILE_LOCKED, new ArrayList<>());
		List<String> profileExpired = userRepresentation.getAttributes().getOrDefault(PROFILE_EXPIRED,
				new ArrayList<>());

		if (profileLocked != null && !profileLocked.isEmpty()) {
			if (Boolean.getBoolean(profileLocked.get(0))) {
				log.info("User's account has been locked");
				return raiseUserProfileLockedException(new Object[] {});
			}
		}

		if (profileExpired != null && !profileExpired.isEmpty()) {
			if (Boolean.getBoolean(profileExpired.get(0))) {
				log.info("User's account has expired");
				return raiseUserProfileExpiredException(new Object[] {});
			}
		}
		*/

		return Mono.just("SUCCESS");
	}

	/**
	 * Update User details
	 * 
	 * @return
	 */
	@Override
	public Mono<UserVO> updateOauthUserById(final UserDetailsUpdateRequest userDetails, final String userId) {

		Mono<UserResource> userRep = Mono.fromCallable(() -> getUserResourceById(userId))
				.switchIfEmpty(raiseResourceNotFoundError("user.notFound", new Object[] { userId }));

		return userRep.flatMap(userResource -> doUserDetailsUpdate(userResource, userDetails));
	}

	/**
	 * 
	 * @param userDetails
	 * @param email
	 * @return
	 */
	@Override
	public Mono<UserVO> updateOauthUser(final UserDetailsUpdateRequest userDetails, final String username) {

		Mono<UserResource> userRep = Mono.fromCallable(() -> getUserResource(username))
				.switchIfEmpty(raiseResourceNotFoundError("user.notFound", new Object[] { username }));

		return userRep.flatMap(userResource -> {

			return doProfileValidation(userResource.toRepresentation())
					.then(doUserDetailsUpdate(userResource, userDetails));
		});
	}

	private Mono<UserVO> doUserDetailsUpdate(UserResource userResource, UserDetailsUpdateRequest userDetails) {
		// Create the user representation
		UserRepresentation userRepresentation = userResource.toRepresentation();

		return Mono.fromRunnable(() -> {

			userRepresentation.setLastName(userDetails.getLastName());
			userRepresentation.setFirstName(userDetails.getFirstName());
			userRepresentation.singleAttribute(LAST_MODIFIED_DATE, String.valueOf(DateUtil.currentTimestamp()));

			userResource.update(userRepresentation);
		}).thenReturn(UserMapper.toViewObject(userRepresentation))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("update.user.error")));
	}

	/**
	 * 
	 * @param userDetails
	 * @param email
	 * @return
	 */
	@Override
	public Mono<String> enableOauthUser(final ProfileActivationUpdateRequest profileStatus) {
		Mono<UserResource> userRep = Mono.fromCallable(() -> getUserResourceById(profileStatus.getUserId()));

		return userRep.flatMap(userResource -> doUserProfileUpdate(userResource, profileStatus));
	}

	private Mono<String> doUserProfileUpdate(UserResource userResource,
			ProfileActivationUpdateRequest userProfileUpdate) {

		return Mono.fromRunnable(() -> {
			// Create the user representation
			UserRepresentation userRepresentation = userResource.toRepresentation();
			userRepresentation.setEnabled(userProfileUpdate.isEnable());
			userRepresentation.singleAttribute(LAST_MODIFIED_DATE, String.valueOf(DateUtil.currentTimestamp()));
			userResource.update(userRepresentation);
		}).thenReturn(userProfileUpdate.isEnable() ? i8nMessageAccessor.getLocalizedMessage("message.user.enabled")
				: i8nMessageAccessor.getLocalizedMessage("message.user.disabled")).onErrorResume(
						handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("profileStatus.update.error")));
	}

	/**
	 * 
	 * @param user
	 * @return
	 */
	@Override
	public Mono<String> expireUserPassword(final UserRepresentation userRepresentation) {

		String username = userRepresentation.getUsername();
		log.info("Username to expire Password: {}", username);

		// Set as True so that User is forced to change their password
		userRepresentation.singleAttribute(PROFILE_EXPIRED, Boolean.TRUE.toString());
		userRepresentation.singleAttribute(LAST_MODIFIED_DATE, String.valueOf(DateUtil.currentTimestamp()));

		return Mono.fromCallable(() -> {
			UserResource userResource = getUserResource(username);

			if (Objects.isNull(userResource)) {
				log.info("User-Resource: {}", userResource);
				return null;
				// return raiseResourceNotFoundError("user.notFound", new Object[] { username
				// });
			}

			userResource.update(userRepresentation);
			return i8nMessageAccessor.getLocalizedMessage("auth.message.success");
		}).switchIfEmpty(raiseResourceNotFoundError("user.notFound", new Object[] { username }));
	}

	/**
	 * 
	 */
	@Override
	public Mono<String> deleteAppUser(String userId) {

		return enableOauthUser(new ProfileActivationUpdateRequest(userId, false));
	}

}
