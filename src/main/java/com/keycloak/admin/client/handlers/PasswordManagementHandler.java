/**
 * 
 */
package com.keycloak.admin.client.handlers;

import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import com.keycloak.admin.client.aop.qualifiers.Loggable;
import com.keycloak.admin.client.common.utils.ResponseCreator;
import com.keycloak.admin.client.components.AuthenticatedUserMgr;
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;
import com.keycloak.admin.client.models.ForgotUsernameRequest;
import com.keycloak.admin.client.models.PasswordResetTokenResponse;
import com.keycloak.admin.client.models.PasswordUpdateRequest;
import com.keycloak.admin.client.models.ResetPasswordFinalRequest;
import com.keycloak.admin.client.models.ResetPasswordRequest;
import com.keycloak.admin.client.oauth.service.it.PasswordMgtService;
import com.keycloak.admin.client.validators.GlobalProgrammaticValidator;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Component
public class PasswordManagementHandler {

	private final PasswordMgtService passwdService;
	private final ResponseCreator responseCreator;
	private final AuthenticatedUserMgr authUserMgr;
	private final CustomMessageSourceAccessor i8nMessageAccessor;

	public PasswordManagementHandler(PasswordMgtService passwdService, AuthenticatedUserMgr authUserMgr,
			ResponseCreator responseCreator, CustomMessageSourceAccessor i8nMessageAccessor) {
		this.passwdService = passwdService;
		this.responseCreator = responseCreator;
		this.authUserMgr = authUserMgr;
		this.i8nMessageAccessor = i8nMessageAccessor;
	}

	/**
	 *
	 * @return
	 */
	@Loggable
	public Mono<ServerResponse> initResetPassword(ServerRequest r) {
		log.info("Initiate Password reset process...");
		final Mono<ResetPasswordRequest> monoPasswdRequest = r.bodyToMono(ResetPasswordRequest.class);

		return monoPasswdRequest.flatMap(GlobalProgrammaticValidator::validate).flatMap(passwdRequest -> {
			return passwdService.initiateResetPasswd(passwdRequest, r.exchange().getRequest())
					.flatMap(message -> this.responseCreator.createAcceptedMessageResponse(message, null, r));
		});
	}

	/**
	 *
	 * @return
	 */
	@Loggable
	public Mono<ServerResponse> validateResetToken(ServerRequest r) {
		log.info("Validate reset password token process...");
		final Optional<String> tokenParameter = r.queryParam("token");

		return tokenParameter.isEmpty()
				? this.responseCreator.createErrorMessageResponse(
						i8nMessageAccessor.getLocalizedMessage("resetToken.not.found", null), HttpStatus.BAD_REQUEST, null, null, r)
				: this.responseCreator.defaultReadResponse(
						this.passwdService.validatePasswordResetToken(tokenParameter.get()),
						PasswordResetTokenResponse.class, null, r);
	}

	/**
	 *
	 * @return
	 */
	@Loggable
	public Mono<ServerResponse> resetPassword(ServerRequest r) {

		final Mono<ResetPasswordFinalRequest> monoResetPasswdRequest = r.bodyToMono(ResetPasswordFinalRequest.class);

		return monoResetPasswdRequest.flatMap(
				resetPasswdRequest -> passwdService.resetUserPassword(resetPasswdRequest, r.exchange().getRequest())
						.flatMap(message -> this.responseCreator.createSuccessMessageResponse(message, null, null, r)));
	}

	/**
	 *
	 * @return
	 */
	@Loggable
	public Mono<ServerResponse> changePassword(ServerRequest r) {
		log.info("Change Password process...");
		final Mono<PasswordUpdateRequest> monoPasswdRequest = r.bodyToMono(PasswordUpdateRequest.class);

		return monoPasswdRequest.flatMap(passwdRequest -> {
			return authUserMgr.getLoggedInUser(r.exchange()).flatMap(username -> passwdService
					.changePassword(passwdRequest, username, r.exchange().getRequest())
					.flatMap(message -> this.responseCreator.createSuccessMessageResponse(message, null, null, r)));
		});
	}

	/**
	 * 
	 * @param r
	 * @return
	 */
	@Loggable
	public Mono<ServerResponse> recoverUsername(ServerRequest r) {
		log.info("Username recovery process...");
		final Mono<ForgotUsernameRequest> monoUsernameRequest = r.bodyToMono(ForgotUsernameRequest.class);

		return monoUsernameRequest.flatMap(GlobalProgrammaticValidator::validate).flatMap(usernameRequest -> {
			return passwdService.recoverUsername(usernameRequest, r.exchange().getRequest())
					.flatMap(message -> this.responseCreator.createAcceptedMessageResponse(message, null, r));
		});
	}

}
