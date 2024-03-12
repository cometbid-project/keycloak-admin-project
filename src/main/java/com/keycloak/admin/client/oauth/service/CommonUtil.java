/**
 * 
 */
package com.keycloak.admin.client.oauth.service;

import static com.keycloak.admin.client.error.helpers.ErrorPublisher.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.keycloak.admin.client.common.geo.GeolocationUtils;
import com.keycloak.admin.client.common.utils.GeneralUtils;
import com.keycloak.admin.client.common.utils.RandomGenerator;
import com.keycloak.admin.client.entities.ActivationToken;
import com.keycloak.admin.client.entities.UserloginLocation;
import com.keycloak.admin.client.events.CustomUserAuthActionEvent;
import com.keycloak.admin.client.events.UserAuthEventTypes;
import com.keycloak.admin.client.events.dto.UserDTO;
import com.keycloak.admin.client.models.ActivationTokenModel;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.models.mappers.UserActivationMapper;
import com.keycloak.admin.client.oauth.service.it.UserLocationService;
import com.keycloak.admin.client.repository.UserActivationTokenRepository;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.retry.Repeat;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Validated
@Component
@PreAuthorize("isAnonymous() or isAuthenticated()")
public class CommonUtil {

	private Environment environment;
	private final ApplicationEventPublisher eventPublisher;
	private final UserActivationTokenRepository tokenRepository;
	private final UserLocationService userLocationService;

	public CommonUtil(UserLocationService userLocationService, ApplicationEventPublisher eventPublisher,
			UserActivationTokenRepository tokenRepository, Environment environment) {
		this.eventPublisher = eventPublisher;
		this.tokenRepository = tokenRepository;
		this.userLocationService = userLocationService;
		this.environment = environment;
	}

	/**
	 * 
	 * @param username
	 * @param r
	 * @return
	 */
	public Mono<UserloginLocation> recordNewUserLocation(@NotBlank final String username,
			@NotNull final ServerHttpRequest r) {

		return this.userLocationService.recordNewUserLocation(username, r).log("User signup location recorded");
	}

	/**
	 * 
	 * @param userVo
	 * @param tokenModel
	 * @param eventType
	 * @param r
	 * @return
	 */
	public Mono<Void> sendEmailVerificationEvent(@Valid final UserVO userVo, @NotBlank final String token,
			@NotNull final UserAuthEventTypes eventType, @NotNull final ServerHttpRequest httpRequest) {

		return this.userLocationService.decodeUserLocation(httpRequest).flatMap(userLoc -> {
			String emailVerificationPath = environment.getProperty("app.auth.emailVerification.path");
			Map<String, String> parameters = new HashMap<>();
			parameters.put("token", token);

			String location = GeolocationUtils.getFullIpLocation(userLoc);
			String sessionId = RandomGenerator.generateSessionId();
			log.info("Geo-Location {}", location);
			log.info("User VO {}", userVo);

			UserDTO userDto = UserDTO.builder().email(userVo.getEmail()).name(userVo.getDisplayName())
					.username(userVo.getUsername()).sessionId(sessionId).deviceDetails(userLoc.getDeviceDetails())
					.location(location).ip(userLoc.getIpAddr()).build();
			log.info("User DTO {}", userDto);

			String appUrl = GeneralUtils.getAppUrl(httpRequest.getURI(), emailVerificationPath, parameters);
			log.info("App Url {}", appUrl);

			this.eventPublisher.publishEvent(new CustomUserAuthActionEvent(userDto, eventType, Arrays.asList(appUrl)));

			return Mono.empty();
		});
	}

	/**
	 * 
	 * @param username
	 * @return
	 */
	public Mono<ActivationToken> getNewActivationToken(@NotBlank final String username) {

		return generateToken().switchIfEmpty(raiseServiceUnavailableError("token.generation.error", new Object[] {}))
				.map(token -> {
					log.info("Generated token {}", token);
					ActivationTokenModel tokenModel = ActivationTokenModel.builder().username(username).token(token)
							.build();

					return UserActivationMapper.create(tokenModel);

				}).flatMap(tokenRepository::save);
	}

	private Mono<String> generateToken() {

		Mono<String> generatedToken = Mono.fromSupplier(RandomGenerator::generateNewToken);

		return generatedToken.flatMap(this::taken).repeatWhenEmpty(Repeat.times(5));
	}

	private Mono<String> taken(String newToken) {
		return tokenRepository.existsById(newToken).flatMap(exist -> {
			log.info("Supplied token already exist? {}", exist);

			if (exist) {
				return Mono.empty();
			}
			return Mono.just(newToken);
		});
	}

}
