/**
 * 
 */
package com.keycloak.admin.client.oauth.service;

import java.util.Collections;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.jayway.jsonpath.JsonPath;
import com.keycloak.admin.client.config.AuthProperties;
import com.keycloak.admin.client.models.AuthTokenResponse;
import com.keycloak.admin.client.models.AuthenticationResponse;
import lombok.extern.log4j.Log4j2;
import static com.keycloak.admin.client.error.helpers.ErrorPublisher.*;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.OAuth2Constants;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Component
@Validated
public class KeycloakJwtTokenUtil {

	private final AuthProperties prop;
	private final AuthorizationServerRemoteClient remoteClient;
	private final Environment environment;

	public KeycloakJwtTokenUtil(AuthorizationServerRemoteClient remoteClient, AuthProperties prop,
			Environment environment) {
		this.prop = prop;
		this.remoteClient = remoteClient;
		this.environment = environment;
	}

	/**
	 * 
	 * @param username
	 * @param password
	 * @param request
	 * @return
	 */
	public Mono<AuthenticationResponse> obtainAccessToken(@NotBlank final String username,
			@NotBlank final String password, @NotNull final ServerRequest request) {
		// obtain authentication url with custom codes

		return doAuthorizationCodeRequest().flatMap(authCode -> doUserAuthentication(username, password,
				authCode.getSessionId(), authCode.getBody(), request));
	}

	private Mono<AuthTokenResponse> doAuthorizationCodeRequest() {
		// obtain authentication url with custom codes

		String authUrl = prop.getAuthorizeUrlPattern();
		String formattedUrl = String.format(authUrl, prop.getAdminClientId(), AuthProperties.SCOPES,
				prop.getRedirectUrl());
		log.info("Auth Url: {}", formattedUrl);

		return remoteClient.get(formattedUrl);
	}

	private Mono<AuthenticationResponse> doUserAuthentication(String username, String password, String authSessionId,
			String responseBody, ServerRequest request) {

		log.info("Session id: {}", authSessionId);
		if (StringUtils.isBlank(responseBody)) {
			raiseBadCredentials("token.authUrl.notfound", new Object[] {});
		}

		String kcPostAuthenticationUrl = responseBody.split("action=\"")[1].split("\"")[0].replace("&amp;", "&");
		log.info("KC POST Authentication Url: {}", kcPostAuthenticationUrl);

		// obtain authentication code and state
		MultiValueMap<String, String> cookiesMap = new LinkedMultiValueMap<>();
		cookiesMap.add("AUTH_SESSION_ID", authSessionId);

		MultiValueMap<String, String> authParams = new LinkedMultiValueMap<>();
		authParams.add("username", username);
		authParams.add("password", password);
		authParams.add("credentialId", "");

		return remoteClient.post(kcPostAuthenticationUrl, authParams, authSessionId)
				.flatMap(response -> extractAccessCode(username, extractAuthCode(response), request));

		// raiseBadCredentials("authentication.failed", new Object[] {});
	}

	private Mono<AuthenticationResponse> extractAccessCode(String username, String authCde, ServerRequest request) {
		// get access token
		MultiValueMap<String, String> accessCodeParams = new LinkedMultiValueMap<>();
		accessCodeParams.add("grant_type", OAuth2Constants.AUTHORIZATION_CODE);
		accessCodeParams.add("code", authCde);
		accessCodeParams.add("client_id", prop.getAdminClientId());
		accessCodeParams.add("redirect_uri", prop.getRedirectUrl());
		accessCodeParams.add("client_secret", prop.getAdminClientSecret());

		return processAccessToken(username, accessCodeParams);
	}

	private Mono<AuthenticationResponse> processAccessToken(String username, MultiValueMap<String, String> params) {

		return remoteClient.post(prop.getTokenUrl(), params, null).flatMap(response -> {
			if (!response.statusCode().is2xxSuccessful()) {
				raiseBadCredentials("token.generation.failed", new Object[] { response.rawStatusCode() });
			}
			return response.bodyToMono(String.class);
		}).map(data -> generateLoginResponse(data, Collections.emptyList(), username));
	}

	private String extractAuthCode(ClientResponse response) {
		List<String> locationHeaders = response.headers().header(HttpHeaders.LOCATION);
		String code = "";

		if (locationHeaders.isEmpty()) {
			raiseBadCredentials("authCode.not.found", new Object[] {});
		} else {
			String location = locationHeaders.get(0);
			code = location.split("code=")[1].split("&")[0];
		}

		return code;
	}

	/**
	 * 
	 * @param refreshToken
	 * @return
	 */
	public Mono<Void> revokeAccessToken(@NotBlank final String refreshToken) {
		// get access token
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("client_id", prop.getAdminClientId());
		params.add("client_secret", prop.getAdminClientSecret());
		params.add("refresh_token", refreshToken);

		remoteClient.post(prop.getRevokeTokenUrl(), params, null).flatMap(response -> {
			if (!response.statusCode().is2xxSuccessful()) {
				log.error("Cannot get token, expected 2xx HTTP Status code: {}", response.rawStatusCode());
			}
			return response.bodyToMono(String.class);
		});

		return Mono.empty();
	}

	/**
	 * 
	 * @param username
	 * @param refreshToken
	 * @return
	 */
	public Mono<AuthenticationResponse> refreshAccessToken(@NotBlank final String username,
			@NotBlank final String refreshToken) {
		// get access token
		MultiValueMap<String, String> refreshTokenParams = new LinkedMultiValueMap<>();
		refreshTokenParams.add("grant_type", OAuth2Constants.REFRESH_TOKEN);
		refreshTokenParams.add("client_id", prop.getAdminClientId());
		refreshTokenParams.add("client_secret", prop.getAdminClientSecret());
		refreshTokenParams.add("refresh_token", refreshToken);

		return processAccessToken(username, refreshTokenParams);
	}

	/**
	 * 
	 * @param data
	 * @param roles
	 * @param username
	 * @return
	 */
	public static AuthenticationResponse generateLoginResponse(@NotBlank final String data,
			@NotEmpty final List<String> roles, @NotBlank final String username) {
		log.info("Access Code response Data {}", data);

		String accessToken = JsonPath.read(data, "$.access_token");
		String refreshToken = JsonPath.read(data, "$.refresh_token");
		Long expiresIn = JsonPath.read(data, "$.expires_in");
		Long refreshExpiresIn = JsonPath.read(data, "$.refresh_expires_in");
		String secret = JsonPath.read(data, "$.secret");

		log.info("Access Token: " + accessToken);
		log.info("Refresh Token: " + refreshToken);
		log.info("Expires in: " + expiresIn);
		log.info("Refresh Expires in: " + refreshExpiresIn);

		String refreshTokenPath = UriComponentsBuilder.fromPath("/auth/refresh").build().toUriString();

		log.info("Refresh Token Path: {}", refreshTokenPath);

		AuthenticationResponse authenticationResponse = AuthenticationResponse.builder().username(username).roles(roles)
				.accessToken(accessToken).refreshToken(refreshToken).expiresIn(expiresIn)
				.refreshExpiresIn(refreshExpiresIn).secret(secret).build();

		return authenticationResponse;
	}

}
