/**
 * 
 */
package com.keycloak.admin.client.oauth.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import static com.keycloak.admin.client.common.geo.GeolocationUtils.*;
import static com.keycloak.admin.client.error.handlers.ExceptionHandler.*;
import static com.keycloak.admin.client.error.helpers.ErrorPublisher.*;

import java.util.Arrays;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;
import com.keycloak.admin.client.config.AuthProfile;
import com.keycloak.admin.client.config.AuthProperties;
import com.keycloak.admin.client.models.AuthenticationRequest;
import com.keycloak.admin.client.models.AuthenticationResponse;
import com.keycloak.admin.client.models.EmailStatusUpdateRequest;
import com.keycloak.admin.client.models.EnableMfaResponse;
import com.keycloak.admin.client.models.LoginLocation;
import com.keycloak.admin.client.models.SendOtpRequest;
import com.keycloak.admin.client.models.ProfileActivationUpdateRequest;
import com.keycloak.admin.client.models.StatusUpdateRequest;
import com.keycloak.admin.client.models.TotpRequest;
import com.keycloak.admin.client.models.UserDetailsUpdateRequest;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.oauth.service.it.KeycloakOauthClientService;
import com.keycloak.admin.client.oauth.service.it.UserAuthenticationService;
import com.keycloak.admin.client.common.activity.enums.ContentType;
import com.keycloak.admin.client.common.activity.enums.ObjectType;
import com.keycloak.admin.client.common.enums.SendModeType;
import com.keycloak.admin.client.common.events.ActivityEventTypes;
import com.keycloak.admin.client.common.events.GenericSpringEvent;
import com.keycloak.admin.client.common.geo.GeolocationUtils;
import com.keycloak.admin.client.common.utils.RandomGenerator;
import com.keycloak.admin.client.events.CustomUserAuthActionEvent;
import com.keycloak.admin.client.events.UserAuthEventTypes;
import com.keycloak.admin.client.events.dto.UserDTO;
import com.keycloak.admin.client.exceptions.AuthenticationError;
import com.keycloak.admin.client.oauth.service.it.UserLocationService;
import com.keycloak.admin.client.token.utils.TotpManager;
import com.keycloak.admin.client.validators.IPValidatorApache;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import ua_parser.Parser;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Service
@Validated
@PreAuthorize("isAuthenticated()")
public class UserAuthenticationServiceImpl implements UserAuthenticationService {

	private final CustomMessageSourceAccessor i8nMessageAccessor;
	private final KeycloakOauthClientService oauthClient;
	private final KeycloakRestService keycloakRestService;
	private final GatewayRedisCache redisCache;
	private final AuthServiceRedisCache authServiceRedisCache;
	private final AuthProfile dataStore;
	private final ApplicationEventPublisher eventPublisher;
	private final TotpManager totpManager;
	private final UserLocationService userLocationService;
	private final Parser parser;

	/**
	 * @param keycloakClient
	 */
	public UserAuthenticationServiceImpl(@Qualifier("keycloak-client") KeycloakOauthClientService oauthClient,
			AuthProfile dataStore, AuthServiceRedisCache authServiceRedisCache, GatewayRedisCache redisCache, 
			ApplicationEventPublisher eventPublisher,
			KeycloakRestService keycloakRestService, @Qualifier("TotpManager") TotpManager totpManager,
			UserLocationService userLocationService, CustomMessageSourceAccessor i8nMessageAccessor, Parser parser) {

		this.i8nMessageAccessor = i8nMessageAccessor;
		this.oauthClient = oauthClient;
		this.keycloakRestService = keycloakRestService;
		this.redisCache = redisCache;
		this.authServiceRedisCache = authServiceRedisCache;
		this.dataStore = dataStore;
		this.eventPublisher = eventPublisher;
		this.totpManager = totpManager;
		this.userLocationService = userLocationService;
		this.parser = parser;
	}

	/**
	 * 
	 * @param id
	 * @param username
	 * @param password
	 * @return Mono<UserVO>
	 */
	@Override
	@PreAuthorize("isAnonymous() or isAuthenticated()")
	public Mono<AuthenticationResponse> authenticate(@NotNull @Valid final AuthenticationRequest authRequest,
			@NotNull final ServerHttpRequest httpRequest) {
		// log on console
		log.info("Authentication service! ");

		final String username = authRequest.getUsername();
		final String ipAddress = GeolocationUtils.getClientIP(httpRequest);

		// Ensure blockedIPs don't participate in the login process
		Mono<Boolean> isBlockedIP = isBlocked(ipAddress, username);

		Mono<AuthenticationResponse> loginResponse = this.oauthClient.passwordGrantLogin(authRequest)
				.doOnError(AuthenticationError.class,
						onError -> processUnauthorizedEvent(username, ipAddress, httpRequest))
				// .subscribe(c -> log.info("Notify user about authentication error")))
				.doOnSuccess(resp -> {
					// that is, only send notification if MFA is disabled
					if (StringUtils.isNotBlank(resp.getAccessToken())) {
						doSigninSuccessNotification(username, ipAddress, httpRequest);
						// .subscribe(s -> log.info("Notify user about successful login"));
					}
				});

		Mono<String> locationCheckResponse = userLocationService.processNewLocationCheck(username, httpRequest);

		return isBlockedIP.zipWith(locationCheckResponse).flatMap(pair -> {
			log.info("T1 result..{}", pair.getT1());
			log.info("T2 result..{}", pair.getT2());

			if (!pair.getT1() && pair.getT2().equals(AuthProperties.COMPLETED)) {
				return loginResponse;
			}
			return Mono.empty();
		}).doOnError(ex -> log.error("Error occurred while authenticating user", ex))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("login.error")));

	}

	private Mono<Boolean> isBlocked(final String ipAddress, final String username) {

		log.info("Checking if credentials are blocked...{}", ipAddress);

		return redisCache.isBlockedIp(ipAddress).flatMap(result -> {
			log.info("Is IP Address blocked? {}...", result);

			if (!result) {
				log.info("IP is not blocked...");
				return redisCache.isBlockedIp(username);
			}

			log.info("IP is blocked...");
			return Mono.just(Boolean.TRUE);
		}).flatMap(blocked -> {
			if (blocked) {
				log.info("IP/Username has been blocked...");
				raiseBlockedIPAttemptLoginAlert(new Object[] { username, ipAddress });
			}

			log.info("IP/Username is not blocked...");
			return Mono.just(Boolean.FALSE);
		});
	}

	private void doSigninSuccessNotification(String username, String ipAddress, ServerHttpRequest httpRequest) {

		this.eventPublisher.publishEvent(new GenericSpringEvent<>(ActivityEventTypes.USER_AUTHENTICATION_EVENT,
				StringUtils.EMPTY, "User login was successful", ObjectType.USER_AUTH, ContentType.AUTH));

		UserDTO userDto = this.userLocationService.createDTOUser(username, username, httpRequest);

		log.error("Send email to notify user about successful login...");
		this.eventPublisher.publishEvent(
				new CustomUserAuthActionEvent(userDto, UserAuthEventTypes.ON_SUCCESS_LOGIN, Arrays.asList()));

		log.info("Notify user about successful login");
		// return Mono.empty();
	}

	private void processUnauthorizedEvent(String username, String ipAddress, ServerHttpRequest httpRequest) {
		log.error("User Authentication experienced suspicious activity");

		this.incrementFailedLogins(ipAddress, username);

		oauthClient.findUserByUsername(username).flatMap(user -> {
			UserDTO userDto = this.userLocationService.createDTOUser(username, username, httpRequest);

			eventPublisher.publishEvent(new CustomUserAuthActionEvent(userDto, UserAuthEventTypes.ON_FAILED_LOGIN));

			return Mono.empty();
		});
	}

	/**
	 * Signal failed login attempts to minimize Denial of Service attack. As soon as
	 * maximum attempt is reached throws a RuntimeException
	 *
	 * @param ipAddress
	 * @return Mono<Void>
	 *
	 */
	@Override
	@PreAuthorize("isAnonymous() or isAuthenticated()")
	public Mono<Long> incrementFailedLogins(@NotBlank final String ipAddress, @NotBlank final String username) {

		final String cacheKey = IPValidatorApache.isValid(ipAddress) ? ipAddress : username;
		log.info("Failed login Cache key {}", cacheKey);

		return redisCache.incrementFailedLogin(cacheKey).flatMap(attempts -> {
			log.info("Failed login record(s) found! {} ", attempts);

			// To do:
			final Long MAXIMUM_VAL = dataStore.getMaximumLoginAttempt();
			log.info("Max attempt allowed {} ", MAXIMUM_VAL);

			if (attempts >= MAXIMUM_VAL) {
				log.info("Maximum attempt has been reached {} ", attempts);

				return redisCache.saveAsBlockedIp(cacheKey).and(redisCache.removeCacheEntry(cacheKey))
						.thenReturn(attempts);
			}

			return Mono.just(attempts);
		}).doOnSuccess(profile -> this.eventPublisher
				.publishEvent(new GenericSpringEvent<>(ActivityEventTypes.UPDATE_AUTH_PROFILE_EVENT, StringUtils.EMPTY,
						"Failed Login attempt recorded...", ObjectType.USER_AUTH, ContentType.AUTH)))
				.doOnError(ex -> log.error("Error occurred while incrementing failed login count for user", ex))
				.onErrorResume(handleWebFluxError("increment.failedlogin.error"));
	}

	/**
	 * 
	 * @param totpCode
	 */
	@Override
	@PreAuthorize("isAnonymous() or isAuthenticated()")
	public Mono<AuthenticationResponse> verifyTotpCode(@NotNull @Valid final TotpRequest totpRequest,
			@NotNull final ServerHttpRequest httpRequest) {
		log.info("Validate Totp code service! ");

		String totpSessionId = totpRequest.getTotpSessionId();

		Mono<AuthenticationResponse> monoResult = this.authServiceRedisCache.getAuthenticationResponse(totpSessionId);

		return monoResult.flatMap(authResponse -> this.doTotpValidation(authResponse, totpRequest, httpRequest))
				.doOnError(ex -> log.error("Error occured while validating Totp code for user", ex))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("validate.totp.error")))
				.doOnSuccess(c -> {
					redisCache.removeCacheEntry(totpSessionId);

					this.eventPublisher.publishEvent(
							new GenericSpringEvent<>(ActivityEventTypes.UPDATE_AUTH_PROFILE_EVENT, StringUtils.EMPTY,
									"Totp code validation was successful...", ObjectType.USER_AUTH, ContentType.AUTH));
				});
	}

	private Mono<AuthenticationResponse> doTotpValidation(AuthenticationResponse authResponse, TotpRequest totpRequest,
			ServerHttpRequest httpRequest) {

		String username = authResponse.getUsername();
		if (StringUtils.isBlank(username)) {
			log.warn("Totp Session not found or expired");
			return raiseLoginSessionExpiredError("login.session.expired", null);
		}

		String ipAddress = GeolocationUtils.getClientIP(httpRequest);

		// Ensure blockedIPs don't participate in the login process
		Mono<Boolean> isBlockedIP = isBlocked(ipAddress, username);
		Mono<String> locationCheckResponse = userLocationService.processNewLocationCheck(username, httpRequest);

		return isBlockedIP.zipWith(locationCheckResponse).flatMap(pair -> {
			log.info("T1 result..{}", pair.getT1());
			log.info("T2 result..{}", pair.getT2());

			if (!pair.getT1() && pair.getT2().equals(AuthProperties.COMPLETED)) {
				return Mono.defer(() -> mfaCodeValidation(authResponse, totpRequest));
			}
			return Mono.empty();
		}).doOnSuccess(resp ->

		doSigninSuccessNotification(username, ipAddress, httpRequest)
		// .subscribe(s -> log.info("Notify user about successful login"));
		).doOnError(AuthenticationError.class, onError -> processUnauthorizedEvent(username, ipAddress, httpRequest));
		// .subscribe(c -> log.info("Notify user about authentication error")));
	}

	private Mono<AuthenticationResponse> mfaCodeValidation(AuthenticationResponse authResponse,
			TotpRequest totpRequest) {

		String totpCode = totpRequest.getTotpCode();
		String refreshToken = authResponse.getRefreshToken();
		String username = authResponse.getUsername();

		Mono<String> mfaValidationResponse = this.oauthClient.doMfaValidation(authResponse, totpCode);
		log.info("Mfa Response: {}", mfaValidationResponse);

		Mono<AuthenticationResponse> refreshedAuthResponse = keycloakRestService.refreshAccessToken(username,
				refreshToken);
		log.info("Refresh tokenResponse: {}", refreshedAuthResponse);

		return mfaValidationResponse.then(refreshedAuthResponse);
	}

	/**
	 * 
	 * @param totpCode
	 */
	@Override
	@PreAuthorize("isAnonymous() or isAuthenticated()")
	public Mono<String> sendOtpCode(@NotNull @Valid final SendOtpRequest otpRequest,
			@NotNull final ServerHttpRequest httpRequest) {
		log.info("Generate and Send Otp code service!");

		// String totpCode = totpRequest.getTotpCode();
		String otpSessionId = otpRequest.getOtpSessionId();
		String emailOnly = otpRequest.getMode();

		Mono<AuthenticationResponse> monoResult = this.authServiceRedisCache.getAuthenticationResponse(otpSessionId);

		return monoResult.flatMap(authResponse -> {
			String username = authResponse.getUsername();

			if (StringUtils.isBlank(username)) {
				log.warn("Totp Session not found or expired");
				return raiseLoginSessionExpiredError("login.session.expired", null);
			}

			return broadcastOtpCode(authResponse, emailOnly, httpRequest);
		}).thenReturn(i8nMessageAccessor.getLocalizedMessage("message.totpCode.sent"));
	}

	private Mono<Boolean> broadcastOtpCode(AuthenticationResponse authResponse, String emailOnly,
			ServerHttpRequest httpRequest) {
		final String username = authResponse.getUsername();
		final String ipAddress = GeolocationUtils.getClientIP(httpRequest);
		final String userAgent = httpRequest.getHeaders().getFirst("User-Agent");
		final String deviceDetails = getDeviceDetails(userAgent, parser);
		final String newOtpSessionId = RandomGenerator.generateSessionId();
		final String otpCode = totpManager.generateOtp();
		log.warn("Otp code {}", otpCode);

		AuthenticationResponse clonedAuthResp = AuthenticationResponse.cloneWithOtpCode(authResponse, otpCode);
		log.warn("Cloned Authentication Response {}", clonedAuthResp);

		// redisCache.removeCacheEntry(otpSessionId);
		return this.authServiceRedisCache.saveAuthenticationResponse(newOtpSessionId, clonedAuthResp).doOnSuccess(c -> {

			Mono<? extends LoginLocation> userRelativeLoc = userLocationService.decodeUserLocation(httpRequest);

			userRelativeLoc.map(location -> {
				String relativeLocation = GeolocationUtils.getFullIpLocation(location);

				UserDTO userDto = UserDTO.builder().sessionId(newOtpSessionId).ip(ipAddress)
						// .phoneNo(phoneNo)
						.token(otpCode).username(username).location(relativeLocation)
						// .email(username)
						.userAgent(userAgent).deviceDetails(deviceDetails).build();

				// Assuming username is same as Email Addr
				if (SendModeType.EMAIL.toString().equalsIgnoreCase(emailOnly)) {
					userDto.setEmail(username);
				} else {
					// userDto.setPhoneNo(phoneNo);
				}

				log.warn("User DTO {}", userDto);

				this.eventPublisher
						.publishEvent(new CustomUserAuthActionEvent(userDto, UserAuthEventTypes.ON_OTPCODE_REQUEST));

				log.info("OtpCode has been sent to your designated Email/Phone");
				return Mono.empty();
			});

		});
	}

	/**
	 * User must be authenticated
	 * 
	 * @param username
	 * @return
	 */
	@Override
	@Transactional
	public Mono<EnableMfaResponse> updateMFA(@NotBlank final String username, boolean enableMFA) {

		Mono<String> monoUser = this.oauthClient.updateUserMfa(username, enableMFA);

		String mfaEnabledMessage = i8nMessageAccessor.getLocalizedMessage("message.mfa.enabled");
		String mfaDisabledMessage = i8nMessageAccessor.getLocalizedMessage("message.mfa.disabled");
  
		return monoUser
				.map(secret -> new EnableMfaResponse(mfaEnabledMessage,
						this.totpManager.generateQrImage(username, secret)))
				.defaultIfEmpty(new EnableMfaResponse(mfaDisabledMessage, null))
				.doOnSuccess(profile -> this.eventPublisher.publishEvent(
						new GenericSpringEvent<>(ActivityEventTypes.UPDATE_AUTH_PROFILE_EVENT, StringUtils.EMPTY,
								"User profile MFA update was successful", ObjectType.USER_AUTH, ContentType.AUTH)))
				.doOnError(ex -> log.error("Error occured while updating Multi-factor auth for user", ex))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("change.mfa.error")));
	}

	/**
	 * 
	 */
	@Override
	@Transactional
	public Mono<String> updateUserStatus(@NotBlank final String username,
			@NotNull @Valid final StatusUpdateRequest statusUpdate) {

		return this.oauthClient.updateUserStatus(username, statusUpdate)
				.thenReturn(i8nMessageAccessor.getLocalizedMessage("message.user.statusUpdate"))
				.doOnSuccess(profile -> this.eventPublisher.publishEvent(
						new GenericSpringEvent<>(ActivityEventTypes.UPDATE_AUTH_PROFILE_EVENT, StringUtils.EMPTY,
								"User profile status update was successful", ObjectType.USER_AUTH, ContentType.AUTH)))
				.doOnError(ex -> log.error("Error occured while updating user status", ex))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("change.userStatus.error")));
	}
	
	/**
	 * 
	 */
	@Override
	@Transactional
	public Mono<String> updateUserByIdStatus(@NotBlank final String userId,
			@NotNull @Valid final StatusUpdateRequest statusUpdate) {

		return this.oauthClient.updateUserByIdStatus(userId, statusUpdate)
				.thenReturn(i8nMessageAccessor.getLocalizedMessage("message.user.statusUpdate"))
				.doOnSuccess(profile -> this.eventPublisher.publishEvent(
						new GenericSpringEvent<>(ActivityEventTypes.UPDATE_AUTH_PROFILE_EVENT, StringUtils.EMPTY,
								"User profile status update was successful", ObjectType.USER_AUTH, ContentType.AUTH)))
				.doOnError(ex -> log.error("Error occured while updating user status", ex))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("change.userStatus.error")));
	}

	/**
	 * 
	 */
	@Override
	@Transactional
	public Mono<UserVO> updateUserDetails(@NotBlank final String username,
			@NotNull @Valid final UserDetailsUpdateRequest userDetailsUpdate) {

		return this.oauthClient.updateOauthUser(userDetailsUpdate, username)
				.doOnSuccess(profile -> this.eventPublisher.publishEvent(
						new GenericSpringEvent<>(ActivityEventTypes.UPDATE_AUTH_PROFILE_EVENT, StringUtils.EMPTY,
								"User profile update was successful", ObjectType.USER_AUTH, ContentType.AUTH)))
				.doOnError(ex -> log.error("Error occured while updating user details", ex))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("update.userDetails.error")));
	}

	/**
	 * 
	 */
	@Override
	@Transactional
	public Mono<UserVO> updateUserById(@NotBlank final String userId,
			@NotNull @Valid final UserDetailsUpdateRequest userDetailsUpdate) {

		return this.oauthClient.updateOauthUserById(userDetailsUpdate, userId)
				.doOnSuccess(profile -> this.eventPublisher.publishEvent(
						new GenericSpringEvent<>(ActivityEventTypes.UPDATE_AUTH_PROFILE_EVENT, StringUtils.EMPTY,
								"User profile update was successful", ObjectType.USER_AUTH, ContentType.AUTH)))
				.doOnError(ex -> log.error("Error occured while updating user details", ex))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("update.userDetails.error")));
	}

	/**
	 * 
	 */
	@Override
	@Transactional
	public Mono<String> signout(@NotBlank final String username, @NotBlank final String refreshToken) {

		return this.oauthClient.signout(username, refreshToken)
				.thenReturn(i8nMessageAccessor.getLocalizedMessage("logout.success"))
				.doOnSuccess(profile -> this.eventPublisher
						.publishEvent(new GenericSpringEvent<>(ActivityEventTypes.UPDATE_AUTH_PROFILE_EVENT,
								StringUtils.EMPTY, "User logout successfully", ObjectType.USER_AUTH, ContentType.AUTH)))
				.doOnError(ex -> log.error("Error occured while logging out user", ex))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("logout.error")));
	}
	
	/**
	 * 
	 */
	@Override
	@Transactional
	public Mono<String> logout(@NotBlank final String userId, @NotBlank final String refreshToken) {

		return this.oauthClient.logout(userId, refreshToken)
				.thenReturn(i8nMessageAccessor.getLocalizedMessage("logout.success"))
				.doOnSuccess(profile -> this.eventPublisher
						.publishEvent(new GenericSpringEvent<>(ActivityEventTypes.UPDATE_AUTH_PROFILE_EVENT,
								StringUtils.EMPTY, "User logout successfully", ObjectType.USER_AUTH, ContentType.AUTH)))
				.doOnError(ex -> log.error("Error occured while logging out user", ex))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("logout.error")));
	}


	/**
	 * 
	 */
	@Override
	@Transactional
	public Mono<AuthenticationResponse> refreshToken(@NotBlank final String username,
			@NotBlank final String refreshToken) {

		return keycloakRestService.refreshAccessToken(username, refreshToken)
				.doOnSuccess(profile -> this.eventPublisher.publishEvent(
						new GenericSpringEvent<>(ActivityEventTypes.UPDATE_AUTH_PROFILE_EVENT, StringUtils.EMPTY,
								"Token refresh was successful", ObjectType.USER_AUTH, ContentType.AUTH)))
				.doOnError(ex -> log.error("Error occured while refreshing token", ex))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("refresh.token.error")));
	}
	
	/**
	 * 
	 */
	@Override
	@Transactional
	public Mono<String> revokeToken(@NotBlank final String refreshToken) {

		return keycloakRestService.logout(refreshToken)
				.thenReturn(i8nMessageAccessor.getLocalizedMessage("revoke.token.success"))
				.doOnSuccess(profile -> this.eventPublisher.publishEvent(
						new GenericSpringEvent<>(ActivityEventTypes.UPDATE_AUTH_PROFILE_EVENT, StringUtils.EMPTY,
								"Token revocation was successful", ObjectType.USER_AUTH, ContentType.AUTH)))
				.doOnError(ex -> log.error("Error occured while revoking token", ex))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("revoke.token.error")));
	}

	/**
	 * 
	 */
	@Override
	@Transactional
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<String> updateEmailStatus(@NotNull @Valid final EmailStatusUpdateRequest statusUpdate) {

		return this.oauthClient.updateEmailStatus(statusUpdate.getEmail(), statusUpdate.isVerified())
				.doOnSuccess(profile -> this.eventPublisher.publishEvent(
						new GenericSpringEvent<>(ActivityEventTypes.UPDATE_AUTH_PROFILE_EVENT, StringUtils.EMPTY,
								"User's email status updated successfully", ObjectType.USER_AUTH, ContentType.AUTH)))
				.doOnError(ex -> log.error("Error occured while updating user email status", ex))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("update.emailStatus.error")));
	}

	/**
	 * 
	 */
	@Override
	@Transactional
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<String> enableUserProfile(@NotBlank final String userId, boolean enableProfile) {  

		final ProfileActivationUpdateRequest statusUpdate = new ProfileActivationUpdateRequest(userId, enableProfile);
		
		return this.oauthClient.enableOauthUser(statusUpdate)
				.doOnSuccess(profile -> this.eventPublisher.publishEvent(
						new GenericSpringEvent<>(ActivityEventTypes.UPDATE_AUTH_PROFILE_EVENT, StringUtils.EMPTY,
								"User profile enabled successfully", ObjectType.USER_AUTH, ContentType.AUTH)))
				.doOnError(ex -> log.error("Error occured while enabling user profile", ex))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("enable.user.error")));
	}
}
