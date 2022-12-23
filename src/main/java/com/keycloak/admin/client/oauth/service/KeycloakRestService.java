/**
 * 
 */
package com.keycloak.admin.client.oauth.service;

import com.keycloak.admin.client.config.AuthProperties;
import com.keycloak.admin.client.models.AuthenticationResponse;
import com.keycloak.admin.client.oauth.service.it.ReactiveClientInterface;

import io.netty.handler.timeout.ReadTimeoutException;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.UserInfo;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.http.HttpHeaders;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotBlank;

/**
 * <pre>
 * com.edw.service.KeycloakRestService
 * </pre>
 *
 * @author Muhammad Edwin < edwin at redhat dot com > 18 Agt 2020 21:47
 */
@Log4j2
@Service
@Validated
public class KeycloakRestService {

	private final WebClient webClient;
	private final AuthProperties authProperties;
	private final ReactiveClientInterface reactiveClient;
	private final Keycloak keycloak;

	public KeycloakRestService(@Qualifier("keycloakClient") WebClient webClient, AuthProperties clientProperties,
			ReactiveClientInterface reactiveClient, Keycloak keycloak) {

		this.webClient = webClient;
		this.authProperties = clientProperties;
		this.reactiveClient = reactiveClient;
		this.keycloak = keycloak;
	}

	/**
	 * login by using username and password to keycloak, and capturing token on
	 * response body
	 *
	 * @param username
	 * @param password
	 * @return
	 */
	public Mono<String> login(@NotBlank final String username, @NotBlank final String password) {
		String keycloakTokenUri = authProperties.getTokenUrl();

		Map<String, List<String>> headers = new HashMap<>();
		headers.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("username", username);
		formData.add("password", password);
		formData.add("client_id", authProperties.getAdminClientId());
		formData.add("grant_type", OAuth2Constants.PASSWORD);
		formData.add("client_secret", authProperties.getAdminClientSecret());
		formData.add("scope", authProperties.getScope());

		return reactiveClient.performPostToMono(webClient, URI.create(keycloakTokenUri), formData, String.class,
				headers, null);
	}

	/**
	 * 
	 * @param username
	 * @param refreshToken
	 * @param request
	 * @return
	 */
	public Mono<AuthenticationResponse> refreshAccessToken(@NotBlank final String username,
			@NotBlank final String refreshToken) {

		String keycloakTokenUri = authProperties.getTokenUrl();

		Map<String, List<String>> headers = new HashMap<>();
		headers.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

		// get access token
		MultiValueMap<String, String> refreshTokenFormData = new LinkedMultiValueMap<>();
		refreshTokenFormData.add("grant_type", OAuth2Constants.REFRESH_TOKEN);
		refreshTokenFormData.add("client_id", authProperties.getAdminClientId());
		refreshTokenFormData.add("client_secret", authProperties.getAdminClientSecret());
		refreshTokenFormData.add("refresh_token", refreshToken);

		Mono<String> monoAccessCode = reactiveClient.performPostFormToMono(webClient, URI.create(keycloakTokenUri),
				refreshTokenFormData, String.class, headers, null);

		return monoAccessCode.map(accessCode -> KeycloakJwtTokenUtil.generateLoginResponse(accessCode,
				Collections.emptyList(), username));
	}

	/**
	 * logging out and disabling active token from keycloak
	 *
	 * @param refreshToken
	 * @return
	 */
	public Mono<String> logout(@NotBlank final String refreshToken) {
		String keycloakInvalidateTokenUri = authProperties.getLogoutUrl();

		Map<String, List<String>> headers = new HashMap<>();
		headers.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("client_id", authProperties.getAdminClientId());
		formData.add("client_secret", authProperties.getAdminClientSecret());
		formData.add("refresh_token", refreshToken);

		return reactiveClient.performPostFormToMono(webClient, URI.create(keycloakInvalidateTokenUri), formData,
				String.class, headers, null);
	}

	/**
	 * 
	 * @param token
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Flux<String> getUserRoles(@NotBlank final String token) {
		// Get realm role "tester" (requires view-realm role)
		String appRealm = authProperties.getAppRealm();

		Mono<UserInfo> userInfoResponse = getUserInfo(token);

		return userInfoResponse.flatMap(userInfo -> findUser(userInfo, appRealm)).map(p -> p.getRealmRoles())
				.flatMapMany(Flux::fromIterable);
	}

	private Mono<UserRepresentation> findUser(UserInfo userInfo, String appRealm) {
		String username = userInfo.getPreferredUsername();

		return Mono.fromCallable(() -> keycloak.realm(appRealm).users().search(username).get(0));
	}

	/**
	 * a successful user token will generate http code 200, other than that will
	 * create an exception
	 *
	 * @param token
	 * @return
	 * @throws Exception
	 */
	public Mono<UserInfo> checkValidity(@NotBlank final String token) {

		return getUserInfo(token).onErrorReturn(Exception.class, new UserInfo()).doOnError(ReadTimeoutException.class,
				ex -> log.error("Server timed out couldn't complete the process"));
	}

	/**
	 * 
	 * @param token
	 * @return
	 */
	public Mono<UserInfo> getUserInfo(@NotBlank final String accessToken) {
		String keycloakUserInfo = authProperties.getKeycloakUserInfo();

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", AuthProperties.BEARER + accessToken);
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);

		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

		return reactiveClient.performPostFormToMono(webClient, URI.create(keycloakUserInfo), formData, UserInfo.class,
				headers, null);
	}

	/**
	 * 
	 * @param token
	 * @param startingClientId
	 * @param startingClientSecret
	 * @param username
	 * @param targetClientId
	 * @return
	 */
	public Mono<String> doTokenExchange(@NotBlank final String token, @NotBlank final String startingClientId,
			@NotBlank final String startingClientSecret, @NotBlank final String username,
			@NotBlank final String targetClientId) {
		String keycloakTokenUri = authProperties.getTokenUrl();

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);

		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("client_id", startingClientId);
		formData.add("client_secret", startingClientSecret);
		formData.add("grant_type", OAuth2Constants.TOKEN_EXCHANGE_GRANT_TYPE);
		formData.add("subject_token", token);
		formData.add("requested_token_type", OAuth2Constants.REFRESH_TOKEN_TYPE);
		formData.add("audience", targetClientId);
		formData.add("requested_subject", username);

		return reactiveClient.performPostFormToMono(webClient, URI.create(keycloakTokenUri), formData, String.class,
				headers, null);
	}

	/**
	 * 
	 * @param token
	 * @param targetClientId
	 * @param targetClientSecret
	 * @return
	 */
	public Mono<AuthenticationResponse> doTokenExchangeToAuthenticationResponse(@NotBlank final String token,
			@NotBlank final String startingClientId, @NotBlank final String startingClientSecret,
			@NotBlank final String username, @NotBlank final String targetClientId) {

		Mono<String> monoAccessCode = doTokenExchange(token, targetClientId, startingClientSecret, username,
				targetClientId);

		return monoAccessCode.map(accessCode -> KeycloakJwtTokenUtil.generateLoginResponse(accessCode,
				Collections.emptyList(), username));
	}

}
