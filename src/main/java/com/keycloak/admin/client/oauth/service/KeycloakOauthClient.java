/**
 * 
 */
package com.keycloak.admin.client.oauth.service;

import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.SocialLinkRepresentation;
import static com.keycloak.admin.client.config.AuthProperties.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
import com.keycloak.admin.client.models.ProfileStatusUpdateRequest;
import com.keycloak.admin.client.models.SearchUserRequest;
import com.keycloak.admin.client.models.SocialLink;
import com.keycloak.admin.client.models.StatusUpdateRequest;
import com.keycloak.admin.client.models.UserDetailsUpdateRequest;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.models.mappers.UserMapper;
import com.keycloak.admin.client.oauth.service.it.KeycloakOauthClientService;
import com.keycloak.admin.client.token.utils.TotpManager;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

		return keycloak.realm(appRealm);
	}

	private UserRepresentation getUserRepresentation(String username) {
		return getUserResource(username).toRepresentation();
	}

	private UserResource getUserResource(String username) {
		// String appRealm = authProperties.getAppRealm();

		boolean exact = true;
		List<UserRepresentation> userRepresentationList = this.realmResource().users().search(username, exact);

		if (!userRepresentationList.isEmpty()) {
			String userId = userRepresentationList.get(0).getId();

			return this.realmResource().users().get(userId);
		}

		return null;
	}

	private UserResource getUserResourceById(String id) {
		// String appRealm = authProperties.getAppRealm();

		return this.realmResource().users().get(id);
	}

	private RoleRepresentation getRealmRoleRepresentation(String roleName) {
		// Get realm role "tester" (requires view-realm role)
		return realmResource().roles()//
				.get(roleName).toRepresentation();
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
	public Mono<String> generateToken(@NotBlank final String username, @NotBlank final String targetClientId) {

		String startingClientId = authProperties.getAdminClientId();
		String startingClientSecret = authProperties.getAdminClientSecret();

		return this.getAdminClientToken()
				.flatMap(authToken -> keycloakService.doTokenExchange(authToken.getAccessToken(), startingClientId,
						startingClientSecret, username, targetClientId));
	}

	/**
	 * 
	 * @param authRequest
	 * @return
	 */
	private Mono<AuthenticationResponse> getAdminClientToken() {
		String appRealm = authProperties.getAppRealm();
		String baseUrl = authProperties.getBaseUrl();
		String clientId = authProperties.getAdminClientId();
		String clientSecret = authProperties.getAdminClientSecret();

		String username = authProperties.getAdminUsername();
		String password = authProperties.getAdminPassword();

		List<String> roles = Collections.emptyList();

		Keycloak localKeycloak = KeycloakBuilder.builder().grantType(OAuth2Constants.PASSWORD).serverUrl(baseUrl)
				.realm(appRealm).username(username).password(password).clientId(clientId).clientSecret(clientSecret)
				.build();

		return Mono.fromCallable(() -> localKeycloak.tokenManager().getAccessTokenString())
				.switchIfEmpty(raiseBadCredentials("invalid.credentials", new Object[] {}))
				.map(accessCode -> KeycloakJwtTokenUtil.generateLoginResponse(accessCode, roles, username));
	}

	/**
	 * 
	 * @param username
	 * @return
	 */
	@Override
	public Mono<List<UserRepresentation>> search(@NotNull final PagingModel pageModel,
			@Valid final SearchUserRequest searchFields) {
		// String appRealm = authProperties.getAppRealm();

		int pageNo = pageModel.getPgNo();
		int pageSize = pageModel.getPgSize();
		int firstResult = pageNo * pageSize;

		boolean userEnabled = true;
		boolean briefRepresentation = true;

		String username = searchFields.getEmail();
		String firstName = searchFields.getFirstName();
		String lastName = searchFields.getLastName();
		String email = searchFields.getEmail();
		boolean emailVerified = searchFields.isEmailVerified();

		// Create the user resource
		return Mono.fromCallable(() -> this.realmResource().users().search(username, firstName, lastName, email,
				emailVerified, firstResult, pageSize, userEnabled, briefRepresentation));
	}

	/**
	 * 
	 * @param username
	 * @return
	 */
	@Override
	public Mono<List<UserRepresentation>> findUsersWithVerifiedEmails(boolean emailVerified,
			@NotNull final PagingModel pageModel) {
		// String appRealm = authProperties.getAppRealm();

		int pageNo = pageModel.getPgNo();
		int pageSize = pageModel.getPgSize();
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
	public Mono<List<UserRepresentation>> findAllUsers(@NotNull final PagingModel pageModel) {
		// String appRealm = authProperties.getAppRealm();

		int pageNo = pageModel.getPgNo();
		int pageSize = pageModel.getPgSize();
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
		// String appRealm = authProperties.getAppRealm();

		return Mono.fromCallable(() -> this.realmResource().users().list()).flatMapMany(Flux::fromIterable);
	}

	/**
	 * 
	 * @param username
	 * @return
	 */
	@Override
	public Mono<UserRepresentation> findUserById(@NotBlank final String id) {

		Mono<UserResource> userResource = Mono.fromCallable(() -> getUserResourceById(id));

		return userResource.map(user -> {

			UserRepresentation userRepresentation = user.toRepresentation();
			// Do Profile checks on status
			doProfileValidation(userRepresentation);

			return userRepresentation;
		});
	}

	/**
	 * 
	 * @param username
	 * @return
	 */
	@Override
	public Mono<UserRepresentation> findUserByUsername(@NotBlank final String username) {

		// Create the user resource
		Mono<UserResource> userResource = Mono.fromCallable(() -> getUserResource(username));

		return userResource.map(user -> {

			UserRepresentation userRepresentation = user.toRepresentation();
			// Do Profile checks on status
			doProfileValidation(userRepresentation);

			return userRepresentation;
		});
	}

	/**
	 * 
	 * @param email
	 * @return
	 */
	@Override
	public Mono<List<UserRepresentation>> findUserByEmail(@NotBlank final String email,
			@NotNull final PagingModel pageModel) {
		// String appRealm = authProperties.getAppRealm();

		int firstResult = pageModel.getPgNo();
		int maxResult = pageModel.getPgSize();

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
		return findUserByUsername(email);
	}

	/**
	 * 
	 * @param passwdUpd
	 * @param userRepresentation
	 * @return
	 */
	@Override
	public Mono<String> saveNewPassword(@Valid final PasswordUpdateRequest passwdUpd,
			final UserRepresentation userRepresentation) {
		// Checking Old Password matches current User password
		List<CredentialRepresentation> credentialList = userRepresentation.getCredentials();
		CredentialRepresentation currentCredential = credentialList.get(0);

		String hashedPassword = passwordEncoder.encode(currentCredential.getValue());

		if (!isPasswordEqual(passwdUpd.getOldPassword(), hashedPassword)) {
			raisePasswordUnacceptableException("user.password.notMatchCurrent", new Object[] {});
		}

		String newPlainTextPassword = passwdUpd.getNewPassword();
		if (isPasswordUsedBefore(userRepresentation, newPlainTextPassword)) {
			raisePasswordUnacceptableException("user.password.matchPrevious", new Object[] {});
		}

		addNewPasswordToHistory(userRepresentation, newPlainTextPassword);

		// Assumption: successful change of password make Expired Accounts change to
		// Valid State
		// PreCondition is now met
		userRepresentation.singleAttribute(PROFILE_EXPIRED, Boolean.FALSE.toString());

		String username = userRepresentation.getUsername();

		return Mono.fromRunnable(() -> {
			UserResource userResource = getUserResource(username);

			if (userResource == null) {
				raiseResourceNotFoundError("user.notFound", new Object[] { username });
			}
			userResource.update(userRepresentation);
		}).thenReturn("User password was changed successful");
		// .onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("password.update.error")));
	}

	private boolean isPasswordEqual(String plainTextPassword, String currentPassword) {

		boolean result = passwordEncoder.matches(plainTextPassword, currentPassword);
		log.info("Password match**************** {}", result);
		return result;
	}

	private boolean isPasswordUsedBefore(UserRepresentation user, String newPlainTextPassword) {

		Set<String> pastUsedPasswords = user.getDisableableCredentialTypes();

		return pastUsedPasswords.stream()
				.anyMatch(hashedPassword -> isPasswordEqual(newPlainTextPassword, hashedPassword));
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
	public Mono<AuthenticationResponse> passwordGrantLogin(@Valid final AuthenticationRequest authRequest) {

		Mono<String> monoAccessCode = login(authRequest)
				.switchIfEmpty(raiseBadCredentials("invalid.credentials", new Object[] {}));

		return monoAccessCode.flatMap(accessCode -> {
			String username = authRequest.getUsername();

			Mono<UserRepresentation> userRep = Mono.fromCallable(() -> getUserRepresentation(username));

			return userRep.flatMap(user -> {
				// Do Profile checks on status
				doProfileValidation(user);

				return buildAuthentication(user, accessCode);
			});
		});
	}

	private Mono<AuthenticationResponse> buildAuthentication(UserRepresentation userRep, String accessCode) {
		String username = userRep.getUsername();

		AuthenticationResponse authResponse = null;
		List<String> roles = userRep.getRealmRoles();

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
	public Mono<String> createOauthUser(final UserRepresentation newUser, @NotBlank final String password,
			@NotNull final SocialLink socialLink) {

		CredentialRepresentation credentialRep = preparePasswordRepresentation(password);
		UserRepresentation userRepresentation = prepareUserRepresentation(newUser, credentialRep, socialLink);

		// String appRealm = authProperties.getAppRealm();

		return Mono.fromCallable(() -> this.realmResource().users().create(userRepresentation))
				.map(response -> getCreatedUserId(response));
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
	public Mono<String> updateUserMfa(@NotBlank final String username, boolean enable2FA) {

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
	public Mono<String> updateUserStatus(@Valid final StatusUpdateRequest statusRequest) {

		String status = statusRequest.getStatus();
		String username = statusRequest.getUsername();

		// Create the user resource
		Mono<UserResource> userRep = Mono.fromCallable(() -> getUserResource(username))
				.switchIfEmpty(raiseResourceNotFoundError("user.notFound", new Object[] { username }));

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
	public Mono<String> resetPassword(@NotBlank final String username, @NotBlank final String newPassword) {

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
		}).thenReturn("User password was reset successful");
	}

	/**
	 * 
	 * @param status
	 * @return
	 */
	@Override
	public Mono<String> updateEmailStatus(@NotBlank final String username, boolean setVerified) {

		Mono<UserResource> userRep = Mono.fromCallable(() -> getUserResource(username))
				.switchIfEmpty(raiseResourceNotFoundError("user.notFound", new Object[] { username }));

		return userRep.flatMap(user -> doEmailStatusUpdate(user, setVerified))
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
	public Mono<Void> logout(@NotBlank final String username, @NotBlank final String refreshToken) {

		Mono<UserResource> userRep = Mono.fromCallable(() -> getUserResource(username))
				.switchIfEmpty(raiseResourceNotFoundError("user.notFound", new Object[] { username }));

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
	public Mono<String> assignRealmRole(@NotBlank final String id, @NotBlank final String roleName) {

		Mono<UserResource> userRep = Mono.fromCallable(() -> getUserResourceById(id))
				.switchIfEmpty(raiseResourceNotFoundError("user.notFound", new Object[] { id }));

		return userRep.flatMap(userResource -> assignRole(userResource, roleName))
				.thenReturn(String.format("User with id: %s has a new role of '%s'", id, roleName));
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
	public Mono<String> assignUserToGroup(@NotBlank final String id, @NotBlank final String groupId) {

		return Mono.fromRunnable(() -> getUserResourceById(id).joinGroup(groupId))
				.switchIfEmpty(raiseResourceNotFoundError("user.notFound", new Object[] { id }))
				.thenReturn(String.format("User %s has been added to new group '%s'", id, groupId));
	}

	/**
	 * 
	 * @param username
	 */
	@Override
	public Mono<String> assignClientRole(@NotBlank final String id, @NotBlank final String clientId,
			@NotBlank final String roleName) {

		Mono<UserResource> userRep = Mono.fromCallable(() -> getUserResourceById(id))
				.switchIfEmpty(raiseResourceNotFoundError("user.notFound", new Object[] { id }));

		return userRep.flatMap(userResource -> assignClient(userResource, clientId, roleName)).thenReturn(
				String.format("User with id: %s has a new Client(%s) role of '%s'", id, clientId, roleName));
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

		List<String> roleNames = user.getRealmRoles().stream().map(roleName -> {
			RoleRepresentation roleRep = getRealmRoleRepresentation(roleName);
			return roleRep.getName();
		}).collect(Collectors.toList());
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
		if (pastCredentials == null || pastCredentials.isEmpty()) {
			pastCredentials = Collections.emptySet();
		}
		// add password to history
		String newPassword = credRepresentation.getValue();
		pastCredentials.add(passwordEncoder.encode(newPassword));
		newUser.setDisableableCredentialTypes(pastCredentials);

		return newUser;
	}

	private CredentialRepresentation preparePasswordRepresentation(String password) {
		CredentialRepresentation credentials = new CredentialRepresentation();
		credentials.setTemporary(false);
		credentials.setType(CredentialRepresentation.PASSWORD);
		credentials.setValue(password);
		credentials.setCreatedDate(System.currentTimeMillis());

		return credentials;
	}

	private String getCreatedUserId(Response response) {
		// URI location = response.getLocation();
		log.info("Repsonse: {}-{}", response.getStatus(), response.getStatusInfo());
		log.info("Resource location {}", response.getLocation());

		return CreatedResponseUtil.getCreatedId(response);
	}

	/**
	 * 
	 * @param authRequest
	 * @return
	 */
	private Mono<String> login(AuthenticationRequest authRequest) {
		String appRealm = authProperties.getAppRealm();
		String baseUrl = authProperties.getBaseUrl();
		String clientId = authProperties.getAdminClientId();
		String clientSecret = authProperties.getAdminClientSecret();

		String username = authRequest.getUsername();
		String password = authRequest.getPassword();

		Keycloak localKeycloak = KeycloakBuilder.builder().grantType(OAuth2Constants.PASSWORD).serverUrl(baseUrl)
				.realm(appRealm).username(username).password(password).clientId(clientId).clientSecret(clientSecret)
				.build();

		return Mono.fromCallable(() -> localKeycloak.tokenManager().getAccessTokenString());
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Mono<String> doMfaValidation(@Valid final AuthenticationResponse authResponse,
			@NotBlank final String totpCode) {
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
		List<String> totpEnabled = userRepresentation.getAttributes().getOrDefault(TOTP_ENABLED,
				Collections.emptyList());

		boolean enabled = false;
		if (totpEnabled != null && !totpEnabled.isEmpty()) {
			enabled = Boolean.getBoolean(totpEnabled.get(0));
		}

		return enabled;
	}

	private boolean isTokenValid(UserRepresentation userRep, final String otpCode, final String totpCode) {
		List<String> totpSecret = userRep.getAttributes().getOrDefault(TOTP_SECRET, Collections.emptyList());

		String userId = userRep.getId();

		Boolean validTotpCode = false;
		if (totpSecret.isEmpty()) {
			log.warn("Totp Secret for user {} not found", userId);
			return validTotpCode;
		}
		// OTP Code is extracted from Cache entry
		// TOTP Code can either be an OTP Code or TOTP Code generated by custom library
		if (StringUtils.isNotBlank(otpCode)) {
			validTotpCode = otpCode.equals(totpCode);
		} else {
			String formattedAuthCode = StringUtil.removeWhiteSpaces(totpCode);
			validTotpCode = totpManager.validateCode(formattedAuthCode, totpSecret.get(0));
		}

		return validTotpCode;
	}

	private void doProfileValidation(final UserRepresentation userRepresentation) {
		List<String> profileLocked = userRepresentation.getAttributes().getOrDefault(PROFILE_LOCKED,
				Collections.emptyList());
		List<String> profileExpired = userRepresentation.getAttributes().getOrDefault(PROFILE_EXPIRED,
				Collections.emptyList());

		if (profileLocked != null && !profileLocked.isEmpty()) {
			if (Boolean.getBoolean(profileLocked.get(0))) {
				log.info("User's account has been locked");
				raiseUserProfileLockedException(new Object[] {});
			}
		}

		if (profileExpired != null && !profileExpired.isEmpty()) {
			if (Boolean.getBoolean(profileExpired.get(0))) {
				log.info("User's account has expired");
				raiseUserProfileExpiredException(new Object[] {});
			}
		}

		if (!userRepresentation.isEmailVerified()) {
			log.info("User's email is Unverified");
			raiseUserProfileUnverifiedException(new Object[] {});
		}

		if (!userRepresentation.isEnabled()) {
			log.info("User's account is inactve");
			raiseUserProfileDisabledException(new Object[] {});
		}
	}

	/**
	 * Update User details
	 * 
	 * @return
	 */
	/*
	 * @Override public Mono<UserVO> updateOauthUser(UserRepresentation userDetails,
	 * String username) {
	 * 
	 * Mono<UserResource> userRep = Mono.fromCallable(() ->
	 * getUserResource(username))
	 * .switchIfEmpty(raiseResourceNotFoundError("user.notFound", new Object[] {
	 * username }));
	 * 
	 * return userRep.flatMap(userResource -> doUserDetailsUpdate(userResource,
	 * userDetails)); }
	 * 
	 * private Mono<UserVO> doUserDetailsUpdate(UserResource userResource,
	 * UserRepresentation userDetails) { // Create the user representation
	 * UserRepresentation userRepresentation = userResource.toRepresentation(); //
	 * userRepresentation.setEnabled(true);
	 * userRepresentation.setFirstName(userDetails.getFirstName());
	 * userRepresentation.setLastName(userDetails.getLastName());
	 * userRepresentation.singleAttribute(LAST_MODIFIED_DATE,
	 * String.valueOf(DateUtil.currentTimestamp()));
	 * 
	 * return Mono.fromRunnable(() -> userResource.update(userRepresentation))
	 * .thenReturn(UserMapper.toViewObject(userRepresentation))
	 * .onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage(
	 * "update.user.error"))); }
	 */

	/**
	 * 
	 * @param userDetails
	 * @param email
	 * @return
	 */
	@Override
	public Mono<UserVO> updateOauthUser(@Valid final UserDetailsUpdateRequest userDetails,
			@NotBlank final String username) {
		// TODO Auto-generated method stub
		Mono<UserResource> userRep = Mono.fromCallable(() -> getUserResource(username))
				.switchIfEmpty(raiseResourceNotFoundError("user.notFound", new Object[] { username }));

		return userRep.flatMap(userResource -> doUserDetailsUpdate(userResource, userDetails));
	}

	private Mono<UserVO> doUserDetailsUpdate(UserResource userResource, UserDetailsUpdateRequest userDetails) {
		// Create the user representation
		UserRepresentation userRepresentation = userResource.toRepresentation();
		// Do Profile checks on status
		doProfileValidation(userRepresentation);

		userRepresentation.setLastName(userDetails.getLastName());
		userRepresentation.setFirstName(userDetails.getFirstName());
		userRepresentation.singleAttribute(LAST_MODIFIED_DATE, String.valueOf(DateUtil.currentTimestamp()));

		return Mono.fromRunnable(() -> userResource.update(userRepresentation))
				.thenReturn(UserMapper.toViewObject(userRepresentation))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("update.user.error")));
	}

	/**
	 * 
	 * @param userDetails
	 * @param email
	 * @return
	 */
	@Override
	public Mono<String> enableOauthUser(@Valid final ProfileStatusUpdateRequest profileStatus) {
		// TODO Auto-generated method stub
		Mono<UserResource> userRep = Mono.fromCallable(() -> getUserResource(profileStatus.getEmail()));

		return userRep.flatMap(userResource -> doUserProfileUpdate(userResource, profileStatus));
	}

	private Mono<String> doUserProfileUpdate(UserResource userResource, ProfileStatusUpdateRequest userProfileUpdate) {
		// Create the user representation
		UserRepresentation userRepresentation = userResource.toRepresentation();
		userRepresentation.setEnabled(userProfileUpdate.isEnable());
		userRepresentation.singleAttribute(LAST_MODIFIED_DATE, String.valueOf(DateUtil.currentTimestamp()));

		return Mono.fromRunnable(() -> userResource.update(userRepresentation))
				.thenReturn(userProfileUpdate.isEnable() ? "User Profile is enabled" : "User Profile is disabled")
				.onErrorResume(
						handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("profileStatus.update.error")));
	}

	/**
	 * 
	 * @param user
	 * @return
	 */
	public Mono<String> expireUserPassword(final UserRepresentation userRepresentation) {
		// TODO Auto-generated method stub
		String username = userRepresentation.getUsername();
		// Set as True so that User is forced to change their password
		userRepresentation.singleAttribute(PROFILE_EXPIRED, Boolean.TRUE.toString());
		userRepresentation.singleAttribute(LAST_MODIFIED_DATE, String.valueOf(DateUtil.currentTimestamp()));

		return Mono.fromRunnable(() -> {
			UserResource userResource = getUserResource(username);

			if (userResource == null) {
				raiseResourceNotFoundError("user.notFound", new Object[] { username });
			}
			userResource.update(userRepresentation);
		})

				.thenReturn(i8nMessageAccessor.getLocalizedMessage("auth.message.success"));
		// .onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("expire.password.error")));
	}

}
