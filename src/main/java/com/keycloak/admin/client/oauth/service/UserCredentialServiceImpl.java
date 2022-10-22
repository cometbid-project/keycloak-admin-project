/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.keycloak.admin.client.oauth.service;

import static com.keycloak.admin.client.error.helpers.ErrorPublisher.*;
import static com.keycloak.admin.client.error.handlers.ExceptionHandler.*;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.ClientErrorException;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.HttpClientErrorException;
import com.keycloak.admin.client.common.activity.enums.ContentType;
import com.keycloak.admin.client.common.activity.enums.ObjectType;
import com.keycloak.admin.client.common.enums.Role;
import com.keycloak.admin.client.common.enums.SocialProvider;
import com.keycloak.admin.client.common.events.ActivityEventTypes;
import com.keycloak.admin.client.common.events.GenericSpringEvent;
import com.keycloak.admin.client.common.utils.GeneralUtils;
import com.keycloak.admin.client.common.utils.RandomGenerator;
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;
import com.keycloak.admin.client.entities.UserloginLocation;
import com.keycloak.admin.client.events.CustomUserAuthActionEvent;
import com.keycloak.admin.client.events.UserAuthEventTypes;
import com.keycloak.admin.client.events.dto.UserDTO;
import com.keycloak.admin.client.exceptions.OAuth2AuthenticationProcessingException;
import com.keycloak.admin.client.models.LocalUser;
import com.keycloak.admin.client.models.SocialLink;
import com.keycloak.admin.client.models.UserDetailsUpdateRequest;
import com.keycloak.admin.client.models.UserRegistrationRequest;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.models.mappers.UserMapper;
import com.keycloak.admin.client.oauth.service.it.ActivationTokenService;
import com.keycloak.admin.client.oauth.service.it.KeycloakOauthClientService;
import com.keycloak.admin.client.oauth.service.it.UserCredentialService;
import com.keycloak.admin.client.social.user.OAuth2UserInfo;
import com.keycloak.admin.client.social.user.OAuth2UserInfoFactory;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 *
 * @author Gbenga
 */
@Log4j2
@Service
@Validated
@PreAuthorize("isAnonymous() or isAuthenticated()")
public class UserCredentialServiceImpl implements UserCredentialService {

	private final KeycloakOauthClientService keycloakClient;
	private final CustomMessageSourceAccessor i8nMessageAccessor;
	private final ApplicationEventPublisher eventPublisher;
	private final ActivationTokenService activationService;
	private final CommonUtil commonUtil;

	public UserCredentialServiceImpl(@Qualifier("keycloak-client") KeycloakOauthClientService keycloakClient,
			ApplicationEventPublisher eventPublisher, CustomMessageSourceAccessor i8nMessageAccessor,
			CommonUtil commonUtil, ActivationTokenService activationService) {

		this.keycloakClient = keycloakClient;
		this.i8nMessageAccessor = i8nMessageAccessor;
		this.eventPublisher = eventPublisher;
		this.activationService = activationService;
		this.commonUtil = commonUtil;
	}

	/**
	 * 
	 */
	@Override
	@Transactional
	public Mono<UserVO> signupUser(@NotNull @Valid UserRegistrationRequest regRequest, Role role,
			@NotNull final ServerHttpRequest r) {

		String username = regRequest.getEmail();
		SocialLink socialLink = new SocialLink();

		UserRepresentation newUser = UserMapper.createUserRepresentation(regRequest, role);

		// Check whether user with same email exist
		Mono<UserRepresentation> userRepresentation = keycloakClient.findUserByUsername(username);

		return userRepresentation.defaultIfEmpty(newUser)
				.flatMap(result -> registerNewUser(result, regRequest.getPassword(), socialLink))
				.retryWhen(Retry.backoff(3, Duration.ofMillis(4))
						.filter(throwable -> throwable instanceof HttpClientErrorException
								|| throwable instanceof ClientErrorException)
						.onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
							log.error("Server error occured...");

							return raiseServiceUnavailableException("user.creation.failed", new Object[] { username });
						}))
				.doOnSuccess(profile -> {
					log.info("User {} signup was successful...", profile.getUsername());

					this.doSignUpSuccessNotification(profile, UserAuthEventTypes.ON_USER_SIGNUP_COMPLETE, r);

				}).doOnError(e -> log.error("Unexpected error occured while registering user profile", e))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("signup.user.error")));
	}

	private Mono<UserVO> registerNewUser(UserRepresentation userDetails, String password, SocialLink socialLink) {
		// User has an existing id
		if (userDetails.getId() != null) {
			return raiseUserAlreadyExist();
		}

		Mono<String> monoResult = keycloakClient.createOauthUser(userDetails, password, socialLink);

		return monoResult.flatMap(id -> keycloakClient.findUserById(id).map(UserMapper::toViewObject));
	}

	private Mono<Void> doSignUpSuccessNotification(UserVO userVo, UserAuthEventTypes eventType, ServerHttpRequest r) {

		Mono<Void> emailVerificationMono = this.activationService.generateEmailVerificationToken(userVo.getUsername())
				.flatMap(tokenModel -> commonUtil.sendEmailVerificationEvent(userVo, tokenModel.getToken(), eventType,
						r));

		return recordSignUpEvent(userVo, r).then(emailVerificationMono);
	}

	private Mono<UserloginLocation> recordSignUpEvent(UserVO userVo, ServerHttpRequest r) {
		// Record User signup location for future reference
		this.eventPublisher.publishEvent(new GenericSpringEvent<>(ActivityEventTypes.AUTH_PROFILE_CREATED_EVENT,
				StringUtils.EMPTY, "User signup successful", ObjectType.USER_AUTH, ContentType.AUTH));

		return commonUtil.recordNewUserLocation(userVo.getUsername(), r);
	}

	/**
	 * 
	 */
	@Override
	@Transactional
	public Mono<LocalUser> processUserRegistration(@NotBlank String registrationId, Map<String, Object> attributes,
			OidcIdToken idToken, OidcUserInfo userInfo) {

		Mono<OAuth2UserInfo> monoOAuth2UserInfo = Mono
				.just(OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes));

		return monoOAuth2UserInfo.flatMap(oAuth2UserInfo -> {
			return processUserSocialRegistration(oAuth2UserInfo).flatMap(userOptional -> processUsers(userOptional,
					registrationId, oAuth2UserInfo, attributes, idToken, userInfo));
		});
	}

	private Mono<LocalUser> processUsers(Optional<UserVO> userOptional, String registrationId,
			OAuth2UserInfo oAuth2UserInfo, Map<String, Object> attributes, OidcIdToken idToken, OidcUserInfo userInfo) {

		if (userOptional.isPresent()) { // business logic
			log.info("User found to have an existing account");

			UserVO currentUser = userOptional.get();

			return updateUserDetails(currentUser, registrationId, oAuth2UserInfo)
					.flatMap(userVo -> createLocalUser(userVo, attributes, idToken, userInfo));
		} else {

			log.info("New user, no existing account");

			return this.signupSocialLoginUser(registrationId, oAuth2UserInfo)
					.flatMap(user -> createLocalUser(user, attributes, idToken, userInfo));
		}
	}

	private Mono<LocalUser> createLocalUser(UserVO user, Map<String, Object> attributes, OidcIdToken idToken,
			OidcUserInfo userInfo) {

		return Mono.just(LocalUser.create(user, attributes, idToken, userInfo));
	}

	private Mono<UserVO> updateUserDetails(UserVO user, String registrationId, OAuth2UserInfo oAuth2UserInfo) {

		if (!user.getSocialProvider().equals(registrationId)
				&& !user.getSocialProvider().equals(SocialProvider.LOCAL.getProviderType())) {

			throw new OAuth2AuthenticationProcessingException(
					String.format("Looks like you're signed up with %s account. Please use your %s account to login.",
							user.getSocialProvider(), user.getSocialProvider()));
		}

		UserDetailsUpdateRequest userDetails = new UserDetailsUpdateRequest(user.getFirstName(), user.getLastName());

		return keycloakClient.updateOauthUser(userDetails, oAuth2UserInfo.getEmail());
	}

	private Mono<Optional<UserVO>> processUserSocialRegistration(OAuth2UserInfo oAuth2UserInfo) {
		if (StringUtils.isEmpty(oAuth2UserInfo.getName())) {
			throw new OAuth2AuthenticationProcessingException("Name not found from OAuth2 Social provider");
		} else if (StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
			throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 Social provider");
		}

		return keycloakClient.findUserByEmail(oAuth2UserInfo.getEmail()).map(UserMapper::toViewObject)
				.flatMap(user -> Mono.just(Optional.of(user))).defaultIfEmpty(Optional.empty());
	}

	public Mono<UserVO> signupSocialLoginUser(String registrationId, OAuth2UserInfo oAuth2UserInfo) {

		String randomPassword = RandomGenerator.generateRandomPassword();
		SocialProvider socialProvider = GeneralUtils.toSocialProvider(registrationId);
		SocialLink socialLink = new SocialLink(oAuth2UserInfo.getId(), socialProvider);

		// Give Users the least role until Registration
		Role role = Role.ROLE_USER;

		UserRepresentation newUser = new UserRepresentation();
		newUser.setEmail(oAuth2UserInfo.getEmail());
		newUser.setUsername(oAuth2UserInfo.getEmail());
		newUser.setRealmRoles(Arrays.asList(role.toString()));
		newUser.setFirstName(oAuth2UserInfo.getName());
		newUser.setLastName(null);
		newUser.setSocialLinks(Collections.emptyList());

		log.info("Save User details to DB...");
		log.info("Role {}", role);

		return registerNewUser(newUser, randomPassword, socialLink).doOnSuccess(profile -> {
			log.info("User {} signup was successful...", profile.getUsername());

			this.doSocialSignUpSuccessNotification(profile, randomPassword, socialProvider.getProviderType(),
					UserAuthEventTypes.ON_SOCIAL_USER_SIGNUP_COMPLETE);
			// .subscribe(c -> log.info("Email Notification Sent..."));

		}).doOnError(e -> log.error("Unexpected error occured while registering user profile", e))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("signup.user.error")));
	}

	private void doSocialSignUpSuccessNotification(UserVO userVo, String password, String socialProvider,
			UserAuthEventTypes eventType) {

		this.eventPublisher.publishEvent(new GenericSpringEvent<>(ActivityEventTypes.AUTH_PROFILE_CREATED_EVENT,
				StringUtils.EMPTY, "User(social) signup successful", ObjectType.USER_AUTH, ContentType.AUTH));

		UserDTO userDto = UserDTO.builder().email(userVo.getEmail()).name(userVo.getDisplayName()).password(password)
				.socialProvider(socialProvider).build();

		this.eventPublisher.publishEvent(new CustomUserAuthActionEvent(userDto, eventType));
	}

	/**
	 * 
	 * @param newRole
	 * @return
	 */
	@Override
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<String> assignToGroup(@NotBlank final String userId, @NotBlank final String groupId) {

		return keycloakClient.assignUserToGroup(userId, groupId)
				.doOnSuccess(profile -> this.eventPublisher.publishEvent(
						new GenericSpringEvent<>(ActivityEventTypes.UPDATE_AUTH_PROFILE_EVENT, StringUtils.EMPTY,
								"User assigned to a group successfully", ObjectType.USER_AUTH, ContentType.AUTH)))
				.doOnError(ex -> log.error("Error occured while finding corresponding User", ex))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("user.group.error")));
	}

	/**
	 * 
	 * @param newRole
	 * @return
	 */
	@Override
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<String> assignRealmRole(@NotBlank final String userId, @NotBlank final String roleName) {

		return keycloakClient.assignRealmRole(userId, roleName)
				.doOnSuccess(profile -> this.eventPublisher.publishEvent(
						new GenericSpringEvent<>(ActivityEventTypes.UPDATE_AUTH_PROFILE_EVENT, StringUtils.EMPTY,
								"User assigned a Realm role successfully", ObjectType.USER_AUTH, ContentType.AUTH)))
				.doOnError(ex -> log.error("Error occured while assigning realm role to User", ex))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("role.assign.error")));
	}

	/**
	 * 
	 * @param newRole
	 * @return
	 */
	@Override
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<String> assignClientRoleToUser(@NotBlank final String userId, @NotBlank final String roleName,
			@NotBlank final String actualClientId) {

		return keycloakClient.assignClientRole(userId, actualClientId, roleName)
				.doOnSuccess(profile -> this.eventPublisher.publishEvent(
						new GenericSpringEvent<>(ActivityEventTypes.UPDATE_AUTH_PROFILE_EVENT, StringUtils.EMPTY,
								"User assigned a Client role successfully", ObjectType.USER_AUTH, ContentType.AUTH)))
				.doOnError(ex -> log.error("Error occured while assigning client role to User", ex))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("clientRole.assign.error")));
	}

}
