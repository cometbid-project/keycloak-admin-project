/**
 * 
 */
package com.keycloak.admin.client.oauth.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static com.keycloak.admin.client.entities.PasswordResetToken.*;
import static com.keycloak.admin.client.common.geo.GeolocationUtils.*;
import static com.keycloak.admin.client.config.AuthProperties.LAST_EXPIRYDATE;
import static com.keycloak.admin.client.config.AuthProperties.LAST_MODIFIED_DATE;
import static com.keycloak.admin.client.error.helpers.ErrorPublisher.*;
import static com.keycloak.admin.client.error.handlers.ExceptionHandler.*;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.keycloak.admin.client.common.activity.enums.ContentType;
import com.keycloak.admin.client.common.activity.enums.ObjectType;
import com.keycloak.admin.client.common.enums.StatusType;
import com.keycloak.admin.client.common.events.ActivityEventTypes;
import com.keycloak.admin.client.common.events.GenericSpringEvent;
import com.keycloak.admin.client.common.geo.GeolocationUtils;
import com.keycloak.admin.client.common.utils.DateUtil;
import com.keycloak.admin.client.common.utils.RandomGenerator;
import com.keycloak.admin.client.components.AuthenticatedUserMgr;
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;
import com.keycloak.admin.client.config.AuthProfile;
import com.keycloak.admin.client.config.AuthProperties;
import com.keycloak.admin.client.entities.PasswordResetToken;
import com.keycloak.admin.client.events.CustomUserAuthActionEvent;
import com.keycloak.admin.client.events.UserAuthEventTypes;
import com.keycloak.admin.client.events.dto.UserDTO;
import com.keycloak.admin.client.models.ForgotUsernameRequest;
import com.keycloak.admin.client.models.LoginLocation;
import com.keycloak.admin.client.models.PasswordResetTokenResponse;
import com.keycloak.admin.client.models.PasswordUpdateRequest;
import com.keycloak.admin.client.models.ResetPasswordFinalRequest;
import com.keycloak.admin.client.models.ResetPasswordRequest;
import com.keycloak.admin.client.oauth.service.it.KeycloakOauthClientService;
import com.keycloak.admin.client.oauth.service.it.PasswordMgtService;
import com.keycloak.admin.client.oauth.service.it.UserLocationService;
import com.keycloak.admin.client.repository.PasswordResetTokenRepository;
import com.maxmind.geoip2.DatabaseReader;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.retry.Repeat;
import ua_parser.Parser;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Service
@Validated
@PreAuthorize("isAnonymous() or isAuthenticated()")
public class PasswordServiceImpl implements PasswordMgtService {

	// private final Parser parser;
	private final AuthProfile dataStore;
	private final UserLocationService userLocationService;
	private final PasswordResetTokenRepository passwordTokenRepository;
	private final CustomMessageSourceAccessor i8nMessageAccessor;
	// private final DatabaseReader databaseReader;
	private final KeycloakOauthClientService keycloakClient;
	private final GatewayRedisCache redisCache;
	private final ApplicationEventPublisher eventPublisher;
	private final ReactiveMongoTemplate mongoTemplate;
	// private final AuthenticatedUserMgr authUserMgr;

	/**
	 * 
	 * @param eventPublisher
	 * @param authUserMgr
	 * @param userLocationService
	 * @param redisCache
	 * @param passwordTokenRepository
	 * @param keycloakClient
	 * @param i8nMessageAccessor
	 * @param dataStore
	 */
	public PasswordServiceImpl(ApplicationEventPublisher eventPublisher, UserLocationService userLocationService,
			GatewayRedisCache redisCache, // @Qualifier("GeoIPCity") DatabaseReader databaseReader,
			PasswordResetTokenRepository passwordTokenRepository,
			@Qualifier("keycloak-client") KeycloakOauthClientService keycloakClient,
			CustomMessageSourceAccessor i8nMessageAccessor, AuthProfile dataStore, // Parser parser,
			ReactiveMongoTemplate mongoTemplate) {

		// this.parser = parser;
		this.dataStore = dataStore;
		this.userLocationService = userLocationService;
		this.passwordTokenRepository = passwordTokenRepository;
		this.i8nMessageAccessor = i8nMessageAccessor;
		// this.databaseReader = databaseReader;
		this.keycloakClient = keycloakClient;
		this.redisCache = redisCache;
		this.eventPublisher = eventPublisher;
		// this.authUserMgr = authUserMgr;
		this.mongoTemplate = mongoTemplate;
	}

	/**
	 * Only used if the Username is not same as Email address
	 * 
	 * @param userModel
	 * @return
	 */
	@Override
	public Mono<String> recoverUsername(@Valid final ForgotUsernameRequest userModel,
			@NotNull final ServerHttpRequest httpRequest) {

		return keycloakClient.findUserByEmail(userModel.getEmail()).doOnSuccess(user -> {
			final String username = user.getUsername();
			final String emailAddr = user.getEmail();

			// Send an email to provided emailAddr containing the recovered username
			UserDTO userDto = this.userLocationService.createDTOUser(username, emailAddr, httpRequest);

			eventPublisher.publishEvent(
					new CustomUserAuthActionEvent(userDto, UserAuthEventTypes.ON_USERNAME_REQUEST_COMPLETE));

		}).thenReturn(i8nMessageAccessor.getLocalizedMessage("forgot.username.response"))
				.doOnError(e -> log.error("Error occured while recovering username", e))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("forgot.username.error")));
	}

	/**
	 * User must be Authenticated. Location check not required.
	 * 
	 * differs from {@link #resetUserPassword(ResetPasswordRequest)} in that, this
	 * method is used by authenticated users who is intending to change their
	 * password
	 * 
	 * @param passwdUpd
	 * @return
	 */
	@Override
	@Transactional
	@PreAuthorize("isAuthenticated()")
	public Mono<String> changePassword(@Valid final PasswordUpdateRequest passwdUpd, @NotBlank final String username,
			@NotNull final ServerHttpRequest httpRequest) {

		return this.keycloakClient.findUserByUsername(username).flatMap(userRep -> {
			log.info("My User Representative: {}", userRep);

			userLocationService.recordNewUserLocation(userRep.getUsername(), httpRequest);

			return keycloakClient.saveNewPassword(passwdUpd, userRep);
		}).doOnError(onError -> log.error("Error occured while Updating User Password", onError))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("change.password.error")))
				.doOnSuccess(response -> {

					log.info("Response: {}", response);
					UserDTO userDto = this.userLocationService.createDTOUser(username, username, httpRequest);

					this.publishPasswordUpdateNotificationEvent(userDto);

				}).thenReturn(i8nMessageAccessor.getLocalizedMessage("change.password.message"));
	}

	private void publishPasswordUpdateNotificationEvent(UserDTO userDto) {

		this.eventPublisher.publishEvent(new GenericSpringEvent<>(ActivityEventTypes.USER_PASSWORD_CHANGED_EVENT,
				StringUtils.EMPTY, String.format("UserAuth: %s password was changed", userDto.getUsername()),
				ObjectType.USER_AUTH, ContentType.AUTH));

		// Send message to emailAddr notifying about the Password change
		this.eventPublisher
				.publishEvent(new CustomUserAuthActionEvent(userDto, UserAuthEventTypes.ON_PASSWORD_CHANGED_COMPLETE));
	}

	/**
	 * Step 1 of Password Reset sequence Location check is required.
	 * 
	 * @param newPasswd
	 * @return
	 */
	@Override
	@Transactional
	public Mono<String> initiateResetPasswd(@Valid final ResetPasswordRequest newPasswordRequest,
			@NotNull final ServerHttpRequest httpRequest) {

		String email = newPasswordRequest.getEmail();

		return this.keycloakClient.findUserByEmail(email).flatMap(userRepresentation -> {
			log.info("My User Representative: {}", userRepresentation);

			userLocationService.processNewLocationCheck(userRepresentation.getUsername(), httpRequest);

			return createPasswordResetTokenForUser(userRepresentation.getUsername());
		}).doOnError(onError -> log.error("Error occured during reset password initiation processing", onError))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("reset.password.init.error")))
				.doOnSuccess(generatedToken -> {

					log.info("Response: {}", generatedToken);
					String username = generatedToken.getUsername();

					UserDTO userDto = this.userLocationService.createDTOUser(username, username, httpRequest);
					log.info("User DTO: {}", userDto);

					this.publishPasswordResetNotificationEvent();
					this.publishPasswordResetNotificationEvent(userDto);

				}).thenReturn(i8nMessageAccessor.getLocalizedMessage("reset.password.init.message"));
	}

	private Mono<PasswordResetToken> createPasswordResetTokenForUser(final String username) {

		final String newToken = RandomGenerator.generate6RandomDigits();
		final PasswordResetToken passwordToken = PasswordResetToken.builder().username(username).token(newToken)
				.status(StatusType.VALID.toString()).creationDate(LocalDateTime.now()).build();

		return passwordTokenRepository.save(passwordToken);
	}

	private void publishPasswordResetNotificationEvent() {

		this.eventPublisher.publishEvent(new GenericSpringEvent<>(ActivityEventTypes.PASSWORD_RESET_EVENT,
				StringUtils.EMPTY, "Reset password action event initiated", ObjectType.USER_AUTH, ContentType.AUTH));
	}

	private void publishPasswordResetNotificationEvent(UserDTO userDto) {

		this.eventPublisher
				.publishEvent(new CustomUserAuthActionEvent(userDto, UserAuthEventTypes.ON_PASSWORD_RESET_REQUEST));
	}

	/**
	 * Step 2 of Password Reset sequence
	 * 
	 * @param token
	 * @return
	 */
	@Override
	public Mono<PasswordResetTokenResponse> validatePasswordResetToken(@NotBlank final String token) {

		final Mono<PasswordResetToken> monoPasswordToken = passwordTokenRepository.findById(token)
				.defaultIfEmpty(new PasswordResetToken());

		return monoPasswordToken.flatMap(passwordToken -> this.generateResetPasswordResponse(passwordToken))
				.doOnError(onError -> log.error("Error occured during reset password token validation", onError))
				.onErrorResume(
						handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("reset.password.token.error")));

	}

	private boolean isTokenFound(String token) {
		return StringUtils.isNotBlank(token);
	}

	private boolean isTokenExpired(PasswordResetToken passToken) {
		return !passToken.getStatus().equalsIgnoreCase(StatusType.VALID.toString());
	}

	private Mono<PasswordResetTokenResponse> generateResetPasswordResponse(final PasswordResetToken passToken) {

		String result = !isTokenFound(passToken.getToken()) ? AuthProperties.INVALID_TOKEN
				: isTokenExpired(passToken) ? AuthProperties.EXPIRED_TOKEN : AuthProperties.SUCCESS;

		if (result.equals(AuthProperties.SUCCESS)) {

			return generateSessionId(passToken)
					.switchIfEmpty(
							raiseServiceUnavailableError("reset.password.sessionId.generation.error", new Object[] {}))
					.map(PasswordResetTokenResponse::new);
		} else {
			return raiseResetPasswordTokenError(result, new Object[] {});
		}
	}

	private Mono<String> generateSessionId(PasswordResetToken passToken) {

		Mono<String> generatedSessionId = Mono.fromSupplier(() -> RandomGenerator.generateSessionId());

		return generatedSessionId.flatMap(sessionId -> cached(sessionId, passToken)).repeatWhenEmpty(Repeat.times(5));
	}

	private Mono<String> cached(String sessionId, PasswordResetToken passToken) {
		log.info("Generated Session id {}", sessionId);

		// Save totpSessionId & Original Authentication details in RedisCache
		return redisCache.savePasswordResetSession(sessionId, passToken.getUsername()).flatMap(result -> {
			if (!result) {
				return Mono.empty();
			}

			passwordTokenRepository.deleteById(passToken.getToken());

			return Mono.just(sessionId);
		});
	}

	/**
	 * Step 3 of Password Reset sequence
	 * 
	 * differs from {@link #changePassword(PasswordUpdateRequest)} in that, this
	 * method is used by unauthenticated users who forgot their password
	 * 
	 * @param passwdUpd
	 * @return
	 */
	@Override
	@Transactional
	public Mono<String> resetUserPassword(@Valid final ResetPasswordFinalRequest passwdUpd,
			@NotNull final ServerHttpRequest r) {

		String sessionId = passwdUpd.getSessionId();
		String newPassword = passwdUpd.getNewPassword();

		return redisCache.getPasswordResetSession(sessionId).flatMap(username -> {
			String requestInitiator = passwdUpd.getUsername();

			if (username.equalsIgnoreCase(requestInitiator)) {
				return this.keycloakClient.resetPassword(username, newPassword).thenReturn(username);
			} else {
				return raiseBadCredentials("reset.password.credentials.notmatch", new Object[] {});
			}

		}).doOnError(onError -> log.error("Error occured while doing User Password reset"))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("reset.password.error")))
				.doOnSuccess(username -> {
					// delete session from Cache
					redisCache.deletePasswordResetSession(sessionId);

					publishPasswordResetCompleteEvent();
					doPasswordChangedCompleteNotificationEvent(username, r);
				}).thenReturn(i8nMessageAccessor.getLocalizedMessage("reset.password.message", new Object[] {}));
	}

	private void publishPasswordResetCompleteEvent() {

		this.eventPublisher
				.publishEvent(new GenericSpringEvent<>(ActivityEventTypes.PASSWORD_RESET_EVENT, StringUtils.EMPTY,
						"Reset password steps of action event complete", ObjectType.USER_AUTH, ContentType.AUTH));
	}

	private void doPasswordChangedCompleteNotificationEvent(String username, ServerHttpRequest httpRequest) {
		log.info("Response: {}", username);
		UserDTO userDto = this.userLocationService.createDTOUser(username, username, httpRequest);

		this.eventPublisher
				.publishEvent(new CustomUserAuthActionEvent(userDto, UserAuthEventTypes.ON_PASSWORD_RESET_COMPLETE));
	}

	/**
	 * Invoked by the Scheduler to expire Batch of Users due for Password
	 * expiration. Expired Users passwords account will thereafter be required to
	 * login
	 */
	@Transactional
	@Async("threadPoolTaskExecutor")
	@Override
	public Flux<Flux<String>> expireUserProfilePasswordRecords() {
		// To do:
		int EXPIRATION_PERIOD = dataStore.getPasswordExpirationPeriod();
		log.info("PASSWORD EXPIRATION_PERIOD Value: {} days", EXPIRATION_PERIOD);

		int batchSize = dataStore.getPasswordExpirationBatchSize();
		log.info("Batch Size: {}", batchSize);

		Flux<Flux<UserRepresentation>> fluxOfFluxResult = keycloakClient.findAllUsers().limitRate(batchSize, 0)
				.windowTimeout(batchSize, Duration.ofSeconds(3L));

		return fluxOfFluxResult.map(result -> processPasswordExpirationFlux(result, EXPIRATION_PERIOD));
	}

	private Flux<String> processPasswordExpirationFlux(Flux<UserRepresentation> fluxResult, int EXPIRATION_PERIOD) {

		Predicate<? super UserRepresentation> predicate = userRepresentation -> this
				.conditionsForPasswordExpiration(userRepresentation, EXPIRATION_PERIOD);

		log.info("processing password Expiration Flux...{}", fluxResult);

		return fluxResult.filter(predicate).flatMap(keycloakClient::expireUserPassword)
				.doOnError(e -> log.error("Job expiring User password profile failed with error", e)).onErrorContinue(
						(t, c) -> log.error("Error occured while expiring User Credentials {}", t.getMessage()));
	}

	private boolean conditionsForPasswordExpiration(UserRepresentation userRepresentation, int EXPIRATION_PERIOD) {

		Map<String, List<String>> attr = userRepresentation.getAttributes();

		if (attr == null) {
			userRepresentation.singleAttribute(LAST_EXPIRYDATE, String.valueOf(DateUtil.currentTimestamp()));
		}

		List<String> lastExpiryDate = userRepresentation.getAttributes().getOrDefault(LAST_EXPIRYDATE,
				Collections.emptyList());
		log.info("Expiry Date {} millis", lastExpiryDate);

		long daysSinceLastExpiration = 0;
		if (!lastExpiryDate.isEmpty()) {
			Long lastExpiration = Long.valueOf(lastExpiryDate.get(0));
			LocalDate lastExpirationDate = DateUtil.getLocalDateFromLongMillisecs(lastExpiration);
			log.info("Last Expiration Date {}", lastExpirationDate);

			LocalDate today = LocalDate.now(ZoneOffset.UTC);
			// Period period = Period.between(lastExpirationDate, today);
			daysSinceLastExpiration = ChronoUnit.DAYS.between(lastExpirationDate, today);

			log.info("Days since last Expiration {} days", daysSinceLastExpiration);
		}

		return (userRepresentation.isEnabled() && daysSinceLastExpiration >= EXPIRATION_PERIOD);
	}

	/**
	 * Invoked by the Scheduler to expire Password Reset token records to mark them
	 * for removal
	 */
	@Override
	@Transactional
	@Async("threadPoolTaskExecutor")
	public Mono<Long> expirePasswordResetTokenRecords() {

		// To do:
		int EXPIRATION_PERIOD = dataStore.getPasswordResetTokenExpirationPeriod();
		log.info("PASSWORD RESET TOKEN EXPIRATION_PERIOD Value: {} hours", EXPIRATION_PERIOD);

		// LocalDateTime now = DateUtil.NOW;
		LocalDateTime lastExpiryDate = LocalDateTime.now(ZoneOffset.UTC).minusHours(EXPIRATION_PERIOD)
				.truncatedTo(ChronoUnit.HOURS);

		log.info("Last Expiry Date {}", lastExpiryDate);

		Query query = new Query();
		query.addCriteria(Criteria.where(CREATION_DATE_FIELD).lte(lastExpiryDate).and(STATUS_FIELD)
				.is(StatusType.VALID.toString()));

		Update update = new Update();

		// update age to 11
		update.set(EXPIRY_DATE_FIELD, LocalDateTime.now(ZoneOffset.UTC));
		update.set(STATUS_FIELD, StatusType.EXPIRED.toString());

		log.info("{}", update);

		// update all matched, both 1004 and 1005
		Mono<UpdateResult> monoResponse = this.mongoTemplate.updateMulti(query, update, PasswordResetToken.class);

		// update all matched, both 1004 and 1005
		return monoResponse.map(p -> p.getModifiedCount())
				.doOnError(e -> log.error("Job expiring Password reset tokens failed with error", e)).onErrorContinue(
						(t, c) -> log.error("Error occured while expiring Password reset tokens {}", t.getMessage()));
		// .subscribe(c -> log.info("{} Password reset token record(s) expired!", c));

	}

	/**
	 * Invoked by the Cleanup Scheduler to rid the database of Expired Password
	 * Reset token records
	 */
	@Override
	@Async("threadPoolTaskExecutor")
	@Transactional
	public Mono<Long> removeExpiredPasswordResetTokenRecords() {

		// To do:
		int DELETION_PERIOD = dataStore.getPasswordResetTokenDeletionPeriod();
		log.info("PASSWORD RESET TOKEN DELETION_PERIOD Value: {} days", DELETION_PERIOD);

		// LocalDateTime now = DateUtil.NOW;
		LocalDateTime lastRemovalDate = LocalDateTime.now(ZoneOffset.UTC).minusDays(DELETION_PERIOD)
				.truncatedTo(ChronoUnit.DAYS);

		log.info("Last Removal Date {}", lastRemovalDate);

		Query query = new Query();
		query.addCriteria(Criteria.where(EXPIRY_DATE_FIELD).lte(lastRemovalDate).and(STATUS_FIELD)
				.is(StatusType.EXPIRED.toString()));

		log.info("{}", query);

		Mono<DeleteResult> monoResponse = this.mongoTemplate.remove(query, PasswordResetToken.class);

		return monoResponse.map(p -> p.getDeletedCount())
				.doOnError(e -> log.error("Job deleting Password reset tokens failed with error", e))
				.onErrorContinue((t, c) -> log.error("Error occured while deleting Password reset tokens. {}",
						t.getLocalizedMessage()));
		// .subscribe(c -> log.info("{} Password reset token records deleted
		// successfully!", c));

	}

}
