/**
 * 
 */
package com.keycloak.admin.client.controllers;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

import org.keycloak.representations.UserInfo;
import org.reactivestreams.Publisher;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerRequest;
import static com.keycloak.admin.client.error.handlers.ExceptionHandler.handleWebFluxError;

import java.security.Principal;
import java.time.Duration;
import java.util.Map;

import javax.validation.Valid;

import com.keycloak.admin.client.common.utils.ResponseCreator;
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;
import com.keycloak.admin.client.models.AuthenticationRequest;
import com.keycloak.admin.client.models.AuthenticationResponse;
import com.keycloak.admin.client.models.LogoutRequest;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.oauth.service.KeycloakRestService;
import com.keycloak.admin.client.oauth.service.it.UserAuthenticationService;
import com.keycloak.admin.client.response.model.AppResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Validated
@RestController
@RequestMapping(value = "/api/v1")
@Tag(name = "Authenticated User property APIs", description = "APIs that give details about authenticated user")
public class HomeController {

	private final KeycloakRestService restService;
	private final UserAuthenticationService userAuthService;
	private final CustomMessageSourceAccessor i8nMessageAccessor;
	private final ResponseCreator responseCreator;

	public HomeController(
			KeycloakRestService restService, CustomMessageSourceAccessor i8nMessageAccessor,
			ResponseCreator responseCreator, UserAuthenticationService userAuthService) {

		super();
		this.restService = restService;
		this.userAuthService = userAuthService;
		this.i8nMessageAccessor = i8nMessageAccessor;
		this.responseCreator = responseCreator;
	}

	/**
	 * 
	 */
	@GetMapping("/about-me")
	Mono<Map<String, Object>> claims(@AuthenticationPrincipal JwtAuthenticationToken auth) {
		return Mono.just(auth.getTokenAttributes());
	}

	/**
	 * 
	 * @param auth
	 * @return
	 */
	@GetMapping("/token")
	Mono<String> token(@AuthenticationPrincipal JwtAuthenticationToken auth) {
		return Mono.just(auth.getToken().getTokenValue());
	}

	/**
	 * 
	 * @return
	 */
	@GetMapping("/role_admin")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	Mono<String> role_admin() {
		return Mono.just("ROLE_ADMIN");
	}

	/**
	 * 
	 * @return
	 */
	@GetMapping("/scope_messages_read")
	@PreAuthorize("hasAuthority('SCOPE_MESSAGES:READ')")
	Mono<String> scope_api_me_read() {
		return Mono.just("You have 'MESSAGES:READ' scope");
	}

	/**
	 * 
	 * @param authHeader
	 * @return
	 */
	@GetMapping("/user-info")
	public Publisher<ResponseEntity<UserInfo>> userInfo(@AuthenticationPrincipal JwtAuthenticationToken auth) {

		String accessToken = auth.getToken().getTokenValue();

		return restService.checkValidity(accessToken).map(response -> ResponseEntity.ok(response));
	}

}
