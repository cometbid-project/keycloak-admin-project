/**
 *
 */
package com.keycloak.admin.client.oauth.service;

import org.apache.commons.lang3.StringUtils;
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
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;
import com.keycloak.admin.client.config.AuthProfile;
import com.keycloak.admin.client.entities.ActivationToken;
import static com.keycloak.admin.client.entities.ActivationToken.*;
import com.keycloak.admin.client.events.UserAuthEventTypes;
import com.keycloak.admin.client.exceptions.ActivationTokenValidationException;
import com.keycloak.admin.client.models.mappers.UserMapper;
import com.keycloak.admin.client.oauth.service.it.ActivationTokenService;
import com.keycloak.admin.client.oauth.service.it.KeycloakOauthClientService;
import com.keycloak.admin.client.repository.UserActivationTokenRepository;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import static com.keycloak.admin.client.error.handlers.ExceptionHandler.*;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Service
@Validated
@PreAuthorize("isAnonymous() or isAuthenticated()")
public class ActivationTokenServiceImpl implements ActivationTokenService {

	private final KeycloakOauthClientService keycloakClient;
	private final ApplicationEventPublisher eventPublisher;
	private final CustomMessageSourceAccessor i8nMessageAccessor;
	private final UserActivationTokenRepository tokenRepository;
	private final CommonUtil commonUtil;
	private final AuthProfile dataStore;
	private final ReactiveMongoTemplate mongoTemplate;

	/**
	 * 
	 * @param tokenRepository
	 * @param publisher
	 */
	public ActivationTokenServiceImpl(UserActivationTokenRepository tokenRepository,
			ApplicationEventPublisher eventPublisher, CustomMessageSourceAccessor i8nMessageAccessor,
			@Qualifier("keycloak-client") KeycloakOauthClientService keycloakClient, CommonUtil commonUtil,
			AuthProfile dataStore, ReactiveMongoTemplate mongoTemplate) {

		this.keycloakClient = keycloakClient;
		this.i8nMessageAccessor = i8nMessageAccessor;
		this.tokenRepository = tokenRepository;
		this.eventPublisher = eventPublisher;
		this.commonUtil = commonUtil;
		this.dataStore = dataStore;
		this.mongoTemplate = mongoTemplate;
	}

	/**
	 *
	 * @param username
	 * @return
	 */
	@Override
	@Transactional
	public Mono<ActivationToken> generateEmailVerificationToken(@NotBlank final String username) {

		return findActivationToken(username, commonUtil.getNewActivationToken(username));
	}

	private Mono<ActivationToken> findActivationToken(final String username, Mono<ActivationToken> alternativeMono) {

		return this.tokenRepository.findByUsernameAndStatusAllIgnoreCase(username, StatusType.VALID.name())
				.switchIfEmpty(alternativeMono)// .map(UserActivationMapper::toViewObject)
				.doOnError(e -> log.error("Email verification token generation service encountered an error", e))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("token.generation.error")))
				.doOnSuccess(profile -> this.eventPublisher.publishEvent(new GenericSpringEvent<>(
						ActivityEventTypes.ACTIVATION_TOKEN_CREATED_EVENT, StringUtils.EMPTY,
						"Email activation token generation successful", ObjectType.USER_AUTH, ContentType.TOKEN)));
	}

	/**
	 *
	 * @param token
	 * @return
	 */
	@Override
	@Transactional
	public Mono<String> renewActivationToken(@NotBlank final String token, @NotNull final ServerHttpRequest r) {

		// 1. Find token record by token
		return this.tokenRepository.findById(token).defaultIfEmpty(new ActivationToken()).flatMap(existingToken -> {
			if (StringUtils.isBlank(existingToken.getToken())) {
				throw new ActivationTokenValidationException("auth.message.invalidToken", new Object[] {});
			}

			String username = existingToken.getUsername();

			return this.generateEmailVerificationToken(username).flatMap(newToken -> resendActivationToken(username,
					newToken.getToken(), UserAuthEventTypes.ON_ACTIVATION_TOKEN_RENEWAL_REQUEST, r));

		}).onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("token.renew.error")))
				.doOnSuccess(profile -> this.eventPublisher.publishEvent(
						new GenericSpringEvent<>(ActivityEventTypes.ACTIVATION_TOKEN_RENEWED_EVENT, StringUtils.EMPTY,
								"Email activation token renewal successful", ObjectType.USER_AUTH, ContentType.TOKEN)))
				.thenReturn(i8nMessageAccessor.getLocalizedMessage("auth.message.resendToken"));
	}

	private Mono<Void> resendActivationToken(String username, String token, UserAuthEventTypes eventType,
			ServerHttpRequest r) {
		log.info("Username: {}", username);
		log.info("Generated token: {}", token);
		log.info("UserAuthEventTypes: {}", eventType);

		return keycloakClient.findUserByUsername(username).map(UserMapper::toViewObject)
				.flatMap(userVo -> commonUtil.sendEmailVerificationEvent(userVo, token, eventType, r));
	}

	/**
	 *
	 * @param userId
	 * @param token
	 * @return
	 */
	@Override
	@Transactional
	public Mono<String> validateEmailActivationToken(@NotBlank final String activationToken) {
		// TODO Auto-generated method stub
		log.info("ValidateEmailActivationToken is called");

		return this.tokenRepository.findById(activationToken).defaultIfEmpty(new ActivationToken()).flatMap(p -> {
			if (StringUtils.isBlank(p.getToken())) {
				log.info("No token found");
				return Mono.fromSupplier(() -> i8nMessageAccessor.getLocalizedMessage("auth.message.invalidToken"));
			} else if (p.getStatus().equalsIgnoreCase(StatusType.EXPIRED.toString())) {
				log.info("Token has expired");
				return Mono.fromSupplier(() -> i8nMessageAccessor.getLocalizedMessage("auth.message.tokenExpired"));
			}

			return doPostValidationUpdate(p);
		}).thenReturn(i8nMessageAccessor.getLocalizedMessage("token.validation.success"))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("token.validation.error")));				
	}

	private Mono<String> doPostValidationUpdate(ActivationToken activationToken) {
		String username = activationToken.getUsername();

		if (StringUtils.isBlank(username)) {
			log.info("The username for the token is not found");
			return Mono.fromSupplier(() -> i8nMessageAccessor.getLocalizedMessage("token.message.invalidUser"));
		}

		boolean setEmailVerified = true;
		return keycloakClient.updateEmailStatus(username, setEmailVerified).map(response -> {
			this.tokenRepository.delete(activationToken);

			log.info("Response built {}", response);
			return response;
		});
	}

	/**
	 * Invoked by the Scheduler to expire Activation token records to mark them for
	 * removal
	 */
	@Override
	@Transactional
	@Async("threadPoolTaskExecutor")
	public Mono<Long> expireActivationTokenRecords() {
		// To do:
		int EXPIRATION_PERIOD = dataStore.getActivationTokenExpirationPeriod();
		log.info("ACTIVATION TOKEN EXPIRATION_PERIOD Value: {} in hours", EXPIRATION_PERIOD);

		LocalDateTime lastExpiryDate = LocalDateTime.now(ZoneOffset.UTC).minusHours(EXPIRATION_PERIOD)
				.truncatedTo(ChronoUnit.HOURS);

		log.info("Last Expiry Date {}", lastExpiryDate);

		Query query = new Query();
		query.addCriteria(Criteria.where(CREATION_DATE_FIELD).lte(lastExpiryDate).and(STATUS_FIELD)
				.is(StatusType.VALID.toString()));

		log.info("{}", query);

		Update update = new Update();
		// update age to 11
		update.set(EXPIRY_DATE_FIELD, LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS).withNano(0));
		update.set(STATUS_FIELD, StatusType.EXPIRED.toString());

		log.info("{}", update);

		// update all matched, both 1004 and 1005
		Mono<UpdateResult> monoResponse = this.mongoTemplate.updateMulti(query, update, ActivationToken.class);

		return monoResponse.map(p -> p.getModifiedCount())
				.doOnError(e -> log.error("An unexpected error occured while expiring activation token records", e))
				.onErrorContinue(
						(t, c) -> log.error("An unexpected error occured while expiring activation token records"));
	}

	/**
	 * Invoked by the Cleanup Scheduler to rid the database of Expired records
	 */
	@Override
	@Transactional
	@Async("threadPoolTaskExecutor")
	public Mono<Long> removeExpiredActivationTokenRecords() {

		// To do:
		int DELETION_PERIOD = dataStore.getActivationTokenDeletionPeriod();
		log.info("ACTIVATION TOKEN DELETION_PERIOD Value: {} days", DELETION_PERIOD);

		// LocalDateTime now = DateUtil.NOW;
		LocalDateTime lastRemovalDate = LocalDateTime.now(ZoneOffset.UTC).minusDays(DELETION_PERIOD)
				.truncatedTo(ChronoUnit.DAYS);

		log.info("Last Removal Date {}", lastRemovalDate);

		Query query = new Query();
		query.addCriteria(Criteria.where(EXPIRY_DATE_FIELD).lte(lastRemovalDate).and(STATUS_FIELD)
				.is(StatusType.EXPIRED.toString())); //

		log.info("{}", query);

		Mono<DeleteResult> monoResponse = this.mongoTemplate.remove(query, ActivationToken.class);

		return monoResponse.map(p -> p.getDeletedCount())
				.doOnError(e -> log.error("Job deleting activation token records failed with error", e))
				.onErrorContinue(
						(t, c) -> log.error("Error occured while deleting activation token records. {}",
								t.getLocalizedMessage()));
	}

}
