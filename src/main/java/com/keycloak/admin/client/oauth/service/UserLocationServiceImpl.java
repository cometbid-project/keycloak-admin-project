/**
 * 
 */
package com.keycloak.admin.client.oauth.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.keycloak.admin.client.common.activity.enums.ContentType;
import com.keycloak.admin.client.common.activity.enums.ObjectType;
import com.keycloak.admin.client.common.enums.StatusType;
import com.keycloak.admin.client.common.events.ActivityEventTypes;
import com.keycloak.admin.client.common.events.GenericSpringEvent;
import com.keycloak.admin.client.common.geo.GeolocationUtils;
import com.keycloak.admin.client.common.utils.DateUtil;
import com.keycloak.admin.client.common.utils.GeneralUtils;
import com.keycloak.admin.client.common.utils.RandomGenerator;
import com.keycloak.admin.client.common.utils.WebfluxUtils;
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;
import com.keycloak.admin.client.config.AuthProfile;
import com.keycloak.admin.client.config.AuthProperties;
import com.keycloak.admin.client.entities.NewLocationToken;
import com.keycloak.admin.client.entities.UserloginLocation;
import com.keycloak.admin.client.events.CustomUserAuthActionEvent;
import com.keycloak.admin.client.events.UserAuthEventTypes;
import com.keycloak.admin.client.events.dto.UserDTO;

import com.keycloak.admin.client.exceptions.UnusualLocationException;
import com.keycloak.admin.client.models.LoginLocation;
import com.keycloak.admin.client.models.StatusUpdateRequest;
import com.keycloak.admin.client.oauth.service.it.UserLocationService;
import com.keycloak.admin.client.repository.NewLocationTokenRepository;
import com.keycloak.admin.client.repository.UserloginLocationRepository;
import static com.keycloak.admin.client.common.geo.GeolocationUtils.*;
import static com.keycloak.admin.client.error.helpers.ErrorPublisher.*;
import static com.keycloak.admin.client.error.handlers.ExceptionHandler.*;
import com.maxmind.geoip2.DatabaseReader;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import ua_parser.Parser;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Service
@Validated
@PreAuthorize("isAnonymous() or isAuthenticated()")
public class UserLocationServiceImpl implements UserLocationService {

	private final Environment environment;
	private final AuthProfile dataStore;
	private final ApplicationEventPublisher eventPublisher;
	private final Parser parser;
	private final Scheduler scheduler;
	private final DatabaseReader databaseReader;
	private final CustomMessageSourceAccessor i8nMessageAccessor;
	private final KeycloakOauthClient keycloakClient;
	private final UserloginLocationRepository successLoginRepository;
	private final NewLocationTokenRepository newLocationTokenRepository;

	/**
	 * @param keycloakClient
	 */
	public UserLocationServiceImpl(NewLocationTokenRepository newLocationTokenRepository,
			UserloginLocationRepository successLoginRepository, Scheduler scheduler,
			ApplicationEventPublisher eventPublisher, Parser parser,
			@Qualifier("keycloak-client") KeycloakOauthClient keycloakClient,
			@Qualifier("GeoIPCity") DatabaseReader databaseReader, AuthProfile dataStore,
			CustomMessageSourceAccessor i8nMessageAccessor, Environment environment) {

		this.environment = environment;
		this.dataStore = dataStore;
		this.eventPublisher = eventPublisher;
		this.parser = parser;
		this.scheduler = scheduler;
		this.databaseReader = databaseReader;
		this.i8nMessageAccessor = i8nMessageAccessor;
		this.keycloakClient = keycloakClient;
		this.newLocationTokenRepository = newLocationTokenRepository;
		this.successLoginRepository = successLoginRepository;
	}

	/**
	 * 
	 */
	@Override
	public Mono<UserloginLocation> recordNewUserLocation(@NotBlank final String username,
			@NotNull final ServerHttpRequest r) {
		// TODO Auto-generated method stub

		return decodeUserLocation(r).flatMap(loginLocation -> recordLocation(username, loginLocation, r))
				.doOnSuccess(profile -> this.eventPublisher
						.publishEvent(new GenericSpringEvent<>(ActivityEventTypes.USER_LOCATION_SAVED_EVENT,
								StringUtils.EMPTY, "User request location recorded successfully", ObjectType.USER_AUTH,
								ContentType.USER_LOCATION)))
				.doOnError(ex -> log.error("Error occured while recording User location", ex))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("record.location.error")));
	}

	private Mono<UserloginLocation> recordLocation(final String username, LoginLocation loginLocation,
			final ServerHttpRequest httpRequest) {

		// ServerHttpRequest httpRequest = r.exchange().getRequest();
		final String ipAddress = getClientIP(httpRequest);

		Mono<UserloginLocation> resultFound = successLoginRepository.findOneByUsernameIgnoreCase(username)
				.defaultIfEmpty(new UserloginLocation());

		return resultFound.flatMap(existingLogin -> {
			final int MAX_LOGIN_LOC_HIS = (int) dataStore.getMaximumLoginLocationHistory();

			if (existingLogin.getId() != null) {
				existingLogin.addToLoginLocHis(Arrays.asList(loginLocation), MAX_LOGIN_LOC_HIS);

				return successLoginRepository.save(existingLogin);

			} else {
				UserloginLocation newLocation = UserloginLocation.builder().username(username).ipAddr(ipAddress)
						.status(StatusType.VALID.getName()).loginLocHis(Arrays.asList(loginLocation)).build();

				return successLoginRepository.save(newLocation);
			}
		});
	}

	/**
	 * 
	 * @param r
	 * @return
	 */
	@Override
	public Mono<? extends LoginLocation> decodeUserLocation(@NotNull final ServerHttpRequest httpRequest) {

		final String ipAddress = getClientIP(httpRequest);
		final String userAgent = httpRequest.getHeaders().getFirst("User-Agent");
		log.info("User-Agent request: {}, IP Address: {}", userAgent, ipAddress);

		String deviceDetails = getDeviceDetails(userAgent, parser);
		log.info("Device details: {}", deviceDetails);

		return decodeLoginLocationFromIPDatabase(ipAddress, deviceDetails)
				.doOnSuccess(profile -> this.eventPublisher.publishEvent(new GenericSpringEvent<>(
						ActivityEventTypes.USER_LOCATION_SAVED_EVENT, StringUtils.EMPTY,
						"User request location decoded successfully", ObjectType.USER_AUTH, ContentType.USER_LOCATION)))
				.doOnError(ex -> log.error("Error occured while decoding User location", ex))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("decode.location.error")));
	}

	private Mono<? extends LoginLocation> decodeLoginLocationFromIPDatabase(String ipAddress, String deviceDetails) {

		return WebfluxUtils.asyncMono(() -> getUserRelativeLocation(ipAddress, deviceDetails, databaseReader),
				scheduler);
	}

	/**
	 * 
	 * @param username
	 * @param httpRequest
	 * @return
	 */
	@Override
	public Mono<String> processNewLocationCheck(@NotBlank final String username,
			@NotNull final ServerHttpRequest httpRequest) {

		return this.isNewLoginLocation(username, httpRequest).map(newToken -> {

			String token = newToken.getToken();
			if (StringUtils.isNotBlank(token)) {
				log.info("Unusual Location Detected...");

				return unusualLocationDetected(username, newToken, httpRequest);
			}
			return AuthProperties.SUCCESS;

		}).thenReturn(AuthProperties.COMPLETED);
	}

	private Mono<Void> unusualLocationDetected(String username, NewLocationToken newToken,
			ServerHttpRequest httpRequest) {

		String country = newToken.getCountryCode();
		String state = newToken.getStateCode();
		String location = getIpLocation(country, state);

		final String ipAddress = getClientIP(httpRequest);
		final String userAgent = httpRequest.getHeaders().getFirst("User-Agent");
		String deviceDetails = getDeviceDetails(userAgent, parser);

		UserDTO userDto = UserDTO.builder().ip(ipAddress).username(username).userAgent(userAgent)
				.deviceDetails(deviceDetails).location(location).email(username).build();

		log.info("Unusual Location Detected...{}", userDto);

		// Empty parameter map supplied
		String changePasswordPath = environment.getProperty("app.auth.changePassword.path");
		String changePasswordUrl = GeneralUtils.getAppUrl(httpRequest.getURI(), changePasswordPath, null);
		log.info("Change password Url {}", changePasswordUrl);

		// token is add to map, appended to URI as request parameter
		String enableLocationPath = environment.getProperty("app.auth.enableLocation.path");
		String enableLocationUrl = GeneralUtils.getAppUrl(httpRequest.getURI(), enableLocationPath, new HashMap<>() {
			{
				put("token", newToken.getToken());
			}
		});
		log.info("Enable location Url {}", enableLocationUrl);

		this.eventPublisher.publishEvent(new CustomUserAuthActionEvent(userDto,
				UserAuthEventTypes.ON_DIFFERENT_LOGIN_LOCATION, Arrays.asList(changePasswordUrl, enableLocationUrl)));

		return Mono.error(new UnusualLocationException("auth.message.unusual.location", new Object[] {}));
	}

	/**
	 * 
	 */
	@Override
	public Mono<NewLocationToken> isNewLoginLocation(@NotBlank final String username,
			@NotNull final ServerHttpRequest r) {

		if (isGeoIpLibEnabled()) {
			log.info("Geo IP Library is Enabled");
			return decodeUserLocation(r).flatMap(loginLocHis -> doLocationCheck(username, loginLocHis));
		}
		return Mono.empty();
	}

	private Mono<NewLocationToken> doLocationCheck(final String username, LoginLocation loginLocation) {

		log.info("Do location check method...");

		Mono<UserloginLocation> resultFound = successLoginRepository.findOneByUsernameIgnoreCase(username)
				.defaultIfEmpty(new UserloginLocation());

		return isExistingLocation(resultFound, loginLocation).flatMap(alreadyExist -> {

			if (!alreadyExist) {
				log.info("No existing login Record found? {}", loginLocation);

				return recordAsDisabledLocation(resultFound, username, loginLocation);
			}
			return Mono.empty();

		}).doOnError(e -> log.error("Processing new login location failed with an error", e))
				.onErrorReturn(new NewLocationToken());
	}

	private Mono<NewLocationToken> generateNewLocationToken(String username, LoginLocation loginLocation) {
		String country = loginLocation.getCountryCode();
		String stateCode = loginLocation.getStateCode();

		log.info("Generating token 222 Username: {}, Location: {}", username, loginLocation);
		return createNewLocationToken(country, stateCode, username);
	}

	private Mono<NewLocationToken> createNewLocationToken(String country, String state, String username) {
		String token = RandomGenerator.generateNewToken();

		log.info("Generated token - Country: {}, State: {}, Token: {}", country, state, token);

		final NewLocationToken newLocToken = NewLocationToken.builder().id(UUID.randomUUID().toString())
				.username(username).token(token).creationDate(DateUtil.now()).status(StatusType.VALID.toString())
				.countryCode(country).stateCode(state).build();

		return newLocationTokenRepository.save(newLocToken);
	}

	private Mono<NewLocationToken> recordAsDisabledLocation(Mono<UserloginLocation> resultFound, String username,
			LoginLocation loginLocation) {

		log.info("record As Disabled Location...{}", loginLocation);

		return resultFound.flatMap(existingLogin -> {

			log.info("User Disabled Login Details...id: {}, details: {}", existingLogin.getId(),
					existingLogin.getDisabledLocations());

			final int MAX_LOGIN_LOC_HIS = (int) dataStore.getMaximumLoginLocationHistory();
			if (existingLogin.getId() != null) {
				existingLogin.addToDisabledLocations(loginLocation, MAX_LOGIN_LOC_HIS);

				return successLoginRepository.save(existingLogin).and(lockUserAccount(username))
						.then(generateNewLocationToken(username, loginLocation));
			} else {
				UserloginLocation newLocation = UserloginLocation.builder().username(username)
						.ipAddr(loginLocation.getIpAddr()).status(StatusType.VALID.getName())
						.loginLocHis(Collections.emptyList()).disabledLocations(Arrays.asList(loginLocation)).build();

				log.info("Generating Location: {}", newLocation);

				return successLoginRepository.save(newLocation).thenReturn(new NewLocationToken());
			}
		});
	}

	private Mono<Boolean> isExistingLocation(Mono<UserloginLocation> successlogin, LoginLocation loginLocation) {

		return successlogin.map(record -> {
			log.info("Print record found {}", record.getDisabledLocations());

			if (record.getId() == null) {
				log.info("No Record found {}", record);
				return false;
			} else {
				log.info("Check if it exist in Login History {}", record);
				return record.isLocationInRecord(loginLocation);
			}
		});
	}

	/**
	 * 
	 */
	@Override
	public Mono<String> isValidNewLocationToken(@NotBlank String token) {

		if (StringUtils.isBlank(token)) {
			return raiseBadRequestError(token, new Object[] {});
		}

		return newLocationTokenRepository.findByToken(token).defaultIfEmpty(new NewLocationToken())
				.flatMap(this::updateWithNewLocation)
				.doOnSuccess(profile -> this.eventPublisher
						.publishEvent(new GenericSpringEvent<>(ActivityEventTypes.USER_LOCATION_SAVED_EVENT,
								StringUtils.EMPTY, "Validating new location token was successfully",
								ObjectType.USER_AUTH, ContentType.USER_LOCATION)))
				.doOnError(e -> log.error("Error occured validating new Location token", e))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("newlocation.token.error")));
	}

	private Mono<String> updateWithNewLocation(NewLocationToken userLoc) {

		if (userLoc.getId() == null) {
			log.info("No token record found");
			return raiseNewLocationTokenInvalidError("invalid.newlocation.token", new Object[] {});
		}

		String username = userLoc.getUsername();
		String countryCode = userLoc.getCountryCode();
		String stateCode = userLoc.getStateCode();

		String location = getIpLocation(countryCode, stateCode);
		log.info("{}", location);

		return enableNewLocation(username, countryCode, stateCode).and(unlockUserAccount(username))
				.then(newLocationTokenRepository.delete(userLoc))
				.thenReturn(i8nMessageAccessor.getLocalizedMessage("newlocation.activated", new Object[] { location }));

	}

	private Mono<UserloginLocation> enableNewLocation(String username, String country, String state) {

		Mono<UserloginLocation> monoUserloginLocation = successLoginRepository.findOneByUsernameIgnoreCase(username);
		return monoUserloginLocation.flatMap(s -> {
			log.info("Before disabled login record {}", s.getDisabledLocations());
			log.info("Before enabled login record {}", s.getLoginLocHis());

			String countryIn = StringUtils.isBlank(country) ? UNKNOWN_COUNTRY : country;
			String stateCodeIn = StringUtils.isBlank(state) ? UNKNOWN_STATE : state;

			s.enableLocation(countryIn, stateCodeIn);

			log.info("After disabled login record {}", s.getDisabledLocations());
			log.info("After enabled login record {}", s.getLoginLocHis());
			return successLoginRepository.save(s);
		});
	}

	private boolean isGeoIpLibEnabled() {
		String goelocationEnabled = environment.getProperty("geo.ip.lib.enabled");
		log.info("Geolocation Enabled  {}", goelocationEnabled);

		return Boolean.parseBoolean(goelocationEnabled);
	}

	private Mono<String> lockUserAccount(final String username) {
		StatusUpdateRequest statusRequest = new StatusUpdateRequest(username, StatusType.LOCKED.toString());

		log.info("Locking user account temporarily {}", statusRequest);
		return keycloakClient.updateUserStatus(statusRequest);
	}

	private Mono<String> unlockUserAccount(final String username) {
		StatusUpdateRequest statusRequest = new StatusUpdateRequest(username, StatusType.VALID.toString());

		log.info("UnLocking user account {}", statusRequest);
		return keycloakClient.updateUserStatus(statusRequest);
	}
	
	/**
	 * 
	 * @param username
	 * @param emailAddr
	 * @param httpRequest
	 * @return
	 */
	@Override
	public UserDTO createDTOUser(String username, String emailAddr, ServerHttpRequest httpRequest) {
		final String ipAddress = getClientIP(httpRequest);
		final String userAgent = httpRequest.getHeaders().getFirst("User-Agent");
		String sessionId = RandomGenerator.generateSessionId();
		String deviceDetails = getDeviceDetails(userAgent, parser);

		LoginLocation requestLocation = GeolocationUtils.getUserRelativeLocation(ipAddress, deviceDetails,
				this.databaseReader);
		String location = GeolocationUtils.getIpLocation(requestLocation);

		return UserDTO.builder().ip(ipAddress).username(username).userAgent(userAgent).sessionId(sessionId)
				.deviceDetails(deviceDetails).location(location).email(emailAddr).build();
	}


}
