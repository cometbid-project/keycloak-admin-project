/**
 * 
 */
package com.keycloak.admin.client.controllers;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

import org.keycloak.representations.UserInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerRequest;

import static com.keycloak.admin.client.error.handlers.ExceptionHandler.handleWebFluxError;

import javax.validation.Valid;

import com.keycloak.admin.client.common.utils.ResponseCreator;
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;
import com.keycloak.admin.client.models.AuthenticationRequest;
import com.keycloak.admin.client.models.AuthenticationResponse;
import com.keycloak.admin.client.models.LogoutRequest;
import com.keycloak.admin.client.oauth.service.KeycloakRestService;
import com.keycloak.admin.client.oauth.service.it.KeycloakOauthClientService;
import com.keycloak.admin.client.response.model.AppResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Validated
@RestController
@Tag(name = "Authentication APIs", description = "APIs for authentication services e.g login, logout")
public class AuthController {

	private final KeycloakRestService restService;

	private final KeycloakOauthClientService keycloakClient;

	private final CustomMessageSourceAccessor i8nMessageAccessor;

	private final ResponseCreator responseCreator;

	public AuthController(@Qualifier("keycloak-client") KeycloakOauthClientService keycloakClient,
			KeycloakRestService restService, CustomMessageSourceAccessor i8nMessageAccessor,
			ResponseCreator responseCreator) {

		super();
		this.restService = restService;
		this.keycloakClient = keycloakClient;
		this.i8nMessageAccessor = i8nMessageAccessor;
		this.responseCreator = responseCreator;
	}

	/**
	 * 
	 * @param refreshToken
	 * @return
	 */
	@PostMapping(value = "/v1/login")
	@Operation(summary = "Login a User", description = "API endpoint for login", tags = { "login", "token" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}.", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponse.class, description = "Token details")) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) })

	public Mono<AuthenticationResponse> login(@RequestBody @Valid final AuthenticationRequest authRequest) {

		log.debug("Executing login endpoint...{}", authRequest);

		return this.keycloakClient.passwordGrantLogin(authRequest)
				.doOnError(e -> log.error("Unexpected error occured while starting user session(login)", e))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("login.user.error")));
	}

	/**
	 * 
	 * @param refreshToken
	 * @return
	 */
	@PostMapping(value = "/v1/logout")
	@Operation(summary = "Logout a User(version 1)", description = "API endpoint for logout", tags = { "logout",
			"refresh token" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description} Ends User session by invalidating the refresh token", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class, description = "Success message")) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) })

	public Mono<AppResponse> logout(@RequestBody @Valid final LogoutRequest refreshToken, ServerRequest r) {

		log.debug("Executing logout endpoint...{}", refreshToken);

		return restService.logout(refreshToken.getRefreshToken())
				.map(msg -> this.responseCreator.createAppResponse(msg, r))
				.doOnError(e -> log.error("Unexpected error occured while ending user session(logout)", e))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("logout.user.error")));
	}
	
	/**
	 * 
	 * @param authHeader
	 * @return
	 */
	@GetMapping("/user")
	public Mono<UserInfo> userInfo(@RequestHeader("Authorization") String authHeader) {

		String accessToken = extractToken(authHeader);

		return restService.checkValidity(accessToken);
	}

	private String extractToken(String authHeader) {
		return authHeader.replace("Bearer", "").trim();
	}

}
