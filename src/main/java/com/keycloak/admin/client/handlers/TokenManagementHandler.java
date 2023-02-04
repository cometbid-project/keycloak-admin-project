/**
 * 
 */
package com.keycloak.admin.client.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import com.keycloak.admin.client.aop.qualifiers.Loggable;
import com.keycloak.admin.client.common.utils.ResponseCreator;
import com.keycloak.admin.client.components.AuthenticatedUserMgr;
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;
import com.keycloak.admin.client.models.AuthenticationResponse;
import com.keycloak.admin.client.models.EnableMfaResponse;
import com.keycloak.admin.client.models.SendOtpRequest;
import com.keycloak.admin.client.models.TotpRequest;
import com.keycloak.admin.client.oauth.service.it.ActivationTokenService;
import com.keycloak.admin.client.oauth.service.it.UserAuthenticationService;
import com.keycloak.admin.client.validators.GlobalProgrammaticValidator;

import java.time.Duration;
import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Component
public class TokenManagementHandler {

	private final UserAuthenticationService authService;
	private final ActivationTokenService activationService;
	private final AuthenticatedUserMgr authUserMgr;
	private final ResponseCreator responseCreator;
	private final CustomMessageSourceAccessor i8nMessageAccessor;

	public TokenManagementHandler(UserAuthenticationService authService, AuthenticatedUserMgr authUserMgr,
			ResponseCreator responseCreator, ActivationTokenService activationService,
			CustomMessageSourceAccessor i8nMessageAccessor) {
		this.authService = authService;
		this.activationService = activationService;
		this.authUserMgr = authUserMgr;
		this.responseCreator = responseCreator;
		this.i8nMessageAccessor = i8nMessageAccessor;
	}

	/**
	 *
	 * @return
	 */
	@Loggable
	public Mono<ServerResponse> validateTotp(ServerRequest r) {

		final Mono<TotpRequest> validateRequest = r.bodyToMono(TotpRequest.class);

		return validateRequest.flatMap(GlobalProgrammaticValidator::validate).flatMap(requestBody -> {

			final Mono<AuthenticationResponse> monoAuthResponse = authService.verifyTotpCode(requestBody,
					r.exchange().getRequest());

			return this.responseCreator.defaultReadResponse(monoAuthResponse, AuthenticationResponse.class, null, r);
		}).timeout(Duration.ofSeconds(3));
	}

	/**
	 * 
	 * @param username
	 * @param activate2FA
	 * @return
	 */
	@Loggable
	public Mono<ServerResponse> sendOtpCode(ServerRequest r) {

		final Mono<SendOtpRequest> validateRequest = r.bodyToMono(SendOtpRequest.class);

		return validateRequest.flatMap(GlobalProgrammaticValidator::validate).flatMap(requestBody -> {
			return authService.sendOtpCode(requestBody, r.exchange().getRequest())
					.flatMap(msg -> this.responseCreator.createSuccessMessageResponse(msg, null, null, r))
					.timeout(Duration.ofSeconds(3));
		});
	}

	/**
	 * 
	 * @param r
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	@Loggable
	public Mono<ServerResponse> activateMfa(ServerRequest r) {

		Mono<EnableMfaResponse> monoAuth = authUserMgr.getLoggedInUser(r.exchange())
				.flatMap(username -> authService.updateMFA(username, true)); 

		return this.responseCreator.defaultReadResponse(monoAuth, EnableMfaResponse.class, null, r)
				.switchIfEmpty(ServerResponse.noContent().build());
	}

	/**
	 * 
	 * @param r
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	@Loggable
	public Mono<ServerResponse> deactivateMfa(ServerRequest r) {

		Mono<EnableMfaResponse> monoAuth = authUserMgr.getLoggedInUser(r.exchange())
				.flatMap(username -> authService.updateMFA(username, false));

		return this.responseCreator.defaultReadResponse(monoAuth, EnableMfaResponse.class, null, r)
				.switchIfEmpty(ServerResponse.noContent().build());
	}

	/**
	 * 
	 * @param r
	 * @return
	 */
	@Loggable
	public Mono<ServerResponse> renewActivationToken(ServerRequest r) {

		final Optional<String> tokenParameter = r.queryParam("token");

		return tokenParameter.isEmpty()
				? this.responseCreator.createErrorMessageResponse(
						i8nMessageAccessor.getLocalizedMessage("email.activation.token.not.found"),
						HttpStatus.BAD_REQUEST, null, null, r)
				: this.activationService.renewActivationToken(tokenParameter.get(), r.exchange().getRequest())
						.flatMap(message -> this.responseCreator.createSuccessMessageResponse(message, null, null, r));
	}

	/**
	 * 
	 * @param r
	 * @return
	 */
	@Loggable
	public Mono<ServerResponse> validateActivationToken(ServerRequest r) {

		final Optional<String> tokenParameter = r.queryParam("token");

		return tokenParameter.isEmpty()
				? this.responseCreator.createErrorMessageResponse(
						i8nMessageAccessor.getLocalizedMessage("email.activation.token.not.found"),
						HttpStatus.BAD_REQUEST, null, null, r)
				: this.activationService.validateEmailActivationToken(tokenParameter.get())
						.flatMap(message -> this.responseCreator.createSuccessMessageResponse(message, null, null, r));
	}
}
