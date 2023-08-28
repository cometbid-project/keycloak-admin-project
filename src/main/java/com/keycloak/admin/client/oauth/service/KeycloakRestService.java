/**
 * 
 */
package com.keycloak.admin.client.oauth.service;

import com.keycloak.admin.client.config.AuthProperties;
import com.keycloak.admin.client.models.AuthenticationResponse;
import com.keycloak.admin.client.oauth.service.it.ReactiveClientInterface;
import com.keycloak.admin.client.token.utils.KeycloakJwtTokenUtil;

import io.netty.handler.timeout.ReadTimeoutException;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.keycloak.OAuth2Constants;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.UserInfo;
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
 * com.keycloak.admin.client.oauth.service.KeycloakRestService
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

	public KeycloakRestService(@Qualifier("keycloak-webClient") WebClient webClient, AuthProperties clientProperties,
			ReactiveClientInterface reactiveClient) {

		this.webClient = webClient;
		this.authProperties = clientProperties;
		this.reactiveClient = reactiveClient;
	}

	/**
	 * login by using username and password to keycloak, and capturing token on
	 * response body
	 *
	 * @param username
	 * @param password
	 * @return
	 */
	public Mono<AccessTokenResponse> login(@NotBlank final String username, @NotBlank final String password) {
		String keycloakTokenUri = authProperties.getTokenUrl();

		Map<String, List<String>> headers = new HashMap<>();
		headers.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("username", username);
		formData.add("password", password);
		formData.add("client_id", authProperties.getAdminClientId());
		formData.add("client_secret", authProperties.getAdminClientSecret());
		formData.add("grant_type", OAuth2Constants.PASSWORD);
		formData.add("scope", authProperties.getScope());

		return reactiveClient.performPostFormToMono(webClient, URI.create(keycloakTokenUri), formData,
				AccessTokenResponse.class, headers, null);
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

		Mono<AccessTokenResponse> monoAccessCode = reactiveClient.performPostFormToMono(webClient,
				URI.create(keycloakTokenUri), refreshTokenFormData, AccessTokenResponse.class, headers, null);

		return monoAccessCode.map(
				tokenGen -> KeycloakJwtTokenUtil.generateLoginResponse(tokenGen, Collections.emptyList(), username));
	}

	/**
	 * logging out and disabling active token from keycloak
	 *
	 * @param refreshToken
	 * @return
	 */
	public Mono<AccessTokenResponse> logout(@NotBlank final String refreshToken) {
		String keycloakInvalidateTokenUri = authProperties.getKeycloakLogout();

		Map<String, List<String>> headers = new HashMap<>();
		headers.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("client_id", authProperties.getAdminClientId());
		formData.add("client_secret", authProperties.getAdminClientSecret());
		formData.add("refresh_token", refreshToken);

		return reactiveClient.performPostFormToMono(webClient, URI.create(keycloakInvalidateTokenUri), formData,
				AccessTokenResponse.class, headers, null);
	}

	/**
	 * 
	 * @param refreshToken
	 * @return
	 */
	public Mono<String> revokeAccessToken(@NotBlank final String accessToken) {

		String revokeTokenUrl = authProperties.getRevokeTokenUrl();

		Map<String, List<String>> headers = new HashMap<>();
		headers.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

		// get access token
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("client_id", authProperties.getAdminClientId());
		formData.add("client_secret", authProperties.getAdminClientSecret());
		formData.add("token", accessToken);
		formData.add("token_type_hint", "access_token");

		return reactiveClient.performPostFormToMono(webClient, URI.create(revokeTokenUrl), formData, String.class,
				headers, null);
	}

	/**
	 * a successful user token will generate http code 200, other than that will
	 * create an exception
	 *
	 * @param token
	 * @return
	 * @throws Exception
	 */
	public Mono<Boolean> checkValidity(@NotBlank final String token) {

		return getUserInfo(token).thenReturn(true)
				.doOnError(ReadTimeoutException.class,
						ex -> log.error("Server timed out couldn't complete the process"))
				.onErrorReturn(Exception.class, false);
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

		return reactiveClient.performGetToMono(webClient, URI.create(keycloakUserInfo), UserInfo.class, headers, null);
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
	public Mono<AccessTokenResponse> doTokenExchange(@NotBlank final String token,
			@NotBlank final String startingClientId, @NotBlank final String startingClientSecret,
			@NotBlank final String username, @NotBlank final String targetClientId) {
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

		return reactiveClient.performPostFormToMono(webClient, URI.create(keycloakTokenUri), formData,
				AccessTokenResponse.class, headers, null);
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

		Mono<AccessTokenResponse> monoAccessCode = doTokenExchange(token, targetClientId, startingClientSecret,
				username, targetClientId);

		return monoAccessCode.map(
				tokenGen -> KeycloakJwtTokenUtil.generateLoginResponse(tokenGen, Collections.emptyList(), username));
	}

}
