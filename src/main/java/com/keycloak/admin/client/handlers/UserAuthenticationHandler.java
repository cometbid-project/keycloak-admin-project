/**
 *
 */
package com.keycloak.admin.client.handlers;

import java.time.Duration;
import com.keycloak.admin.client.aop.qualifiers.Loggable;
import com.keycloak.admin.client.common.utils.ResponseCreator;
import com.keycloak.admin.client.components.AuthenticatedUserMgr;
import com.keycloak.admin.client.models.AuthenticationRequest;
import com.keycloak.admin.client.models.AuthenticationResponse;
import com.keycloak.admin.client.models.EmailStatusUpdateRequest;
import com.keycloak.admin.client.models.StatusUpdateRequest;
import com.keycloak.admin.client.models.UserDetailsUpdateRequest;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.oauth.service.it.UserAuthenticationService;
import com.keycloak.admin.client.oauth.service.it.UserCredentialFinderService;
import com.keycloak.admin.client.validators.GlobalProgrammaticValidator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Component
public class UserAuthenticationHandler {

	private final UserAuthenticationService userAuthenticationService;
	private final UserCredentialFinderService profileFinderService;
	private final ResponseCreator responseCreator;
	private final AuthenticatedUserMgr authUserMgr;

	public UserAuthenticationHandler(UserCredentialFinderService profileFinderService, AuthenticatedUserMgr authUserMgr,
			ResponseCreator responseCreator, UserAuthenticationService userAuthenticationService) {

		this.userAuthenticationService = userAuthenticationService;
		this.profileFinderService = profileFinderService;
		this.authUserMgr = authUserMgr;
		this.responseCreator = responseCreator;
	}

	/**
	 *
	 * @return
	 */
	@Loggable
	public Mono<ServerResponse> signin(ServerRequest r) {
		log.info("auth login...");

		final Mono<AuthenticationRequest> monoSigninRequest = r.bodyToMono(AuthenticationRequest.class);

		return monoSigninRequest.flatMap(GlobalProgrammaticValidator::validate)
				.flatMap(request -> this.responseCreator.defaultReadResponse(
						userAuthenticationService.authenticate(request, r.exchange().getRequest()),
						AuthenticationResponse.class, null, r)).log()
				.timeout(Duration.ofSeconds(3));
	}

	/**
	 *
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	@Loggable
	public Mono<ServerResponse> findMyProfile(ServerRequest r) {

		return authUserMgr.getLoggedInUser(r.exchange()).flatMap(username -> this.responseCreator
				.defaultReadResponse(profileFinderService.findByUsername(username), UserVO.class, null, r));
	}

	/**
	 *
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	@Loggable
	public Mono<ServerResponse> updateMyProfile(ServerRequest r) {

		final Mono<UserDetailsUpdateRequest> monoUserDetailsRequest = r.bodyToMono(UserDetailsUpdateRequest.class);

		// log.info("Update User details...");
		return monoUserDetailsRequest.flatMap(GlobalProgrammaticValidator::validate).flatMap(updateRequest -> {
			return authUserMgr.getLoggedInUser(r.exchange())
					.flatMap(username -> this.responseCreator.defaultReadResponse(
							userAuthenticationService.updateUserDetails(username, updateRequest), UserVO.class, null,
							r));
		});
	}

	/**
	 *
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	@Loggable
	public Mono<ServerResponse> findUserProfile(ServerRequest r) {

		String userId = r.pathVariable("id");

		return this.responseCreator.defaultReadResponse(profileFinderService.findUserById(userId), UserVO.class, null,
				r);
	}

	/**
	 *
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	@Loggable
	public Mono<ServerResponse> updateUserProfile(ServerRequest r) {

		String userId = r.pathVariable("id");

		final Mono<UserDetailsUpdateRequest> monoUserDetailsRequest = r.bodyToMono(UserDetailsUpdateRequest.class);

		// log.info("Update User details...");
		return monoUserDetailsRequest.flatMap(GlobalProgrammaticValidator::validate)
				.flatMap(updateRequest -> this.responseCreator.defaultReadResponse(
						userAuthenticationService.updateUserById(userId, updateRequest), UserVO.class, null, r));
	}

	/**
	 * 
	 * @param r
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	@Loggable
	public Mono<ServerResponse> updateMyStatus(ServerRequest r) {

		final Mono<StatusUpdateRequest> monoStatusRequest = r.bodyToMono(StatusUpdateRequest.class);

		return monoStatusRequest.flatMap(GlobalProgrammaticValidator::validate).flatMap(updateRequest -> {
			return authUserMgr.getLoggedInUser(r.exchange())
					.flatMap(username -> userAuthenticationService.updateUserStatus(username, updateRequest).flatMap(
							message -> this.responseCreator.createSuccessMessageResponse(message, null, null, r)));
		});
	}

	/**
	 * This is a highly restricted API, requires special privilege..and cannot be
	 * done by the profile owner but by the Admin
	 * 
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	@Loggable
	public Mono<ServerResponse> updateUserStatus(ServerRequest r) {

		String userId = r.pathVariable("id");
		final Mono<StatusUpdateRequest> monoStatusRequest = r.bodyToMono(StatusUpdateRequest.class);

		return monoStatusRequest.flatMap(GlobalProgrammaticValidator::validate)
				.flatMap(updateRequest -> userAuthenticationService.updateUserByIdStatus(userId, updateRequest)
						.flatMap(message -> this.responseCreator.createSuccessMessageResponse(message, null, null, r)));
	}

	/**
	 * This is a highly restricted API, requires special privilege..and cannot be
	 * done by the profile owner but by the Admin
	 * 
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	@Loggable
	public Mono<ServerResponse> updateUserEmailStatus(ServerRequest r) {

		// String id = req.pathVariable("id");
		final Mono<EmailStatusUpdateRequest> monoEmailStatusUpdate = r.bodyToMono(EmailStatusUpdateRequest.class);

		return monoEmailStatusUpdate.flatMap(GlobalProgrammaticValidator::validate)
				.flatMap(statusUpdate -> userAuthenticationService.updateEmailStatus(statusUpdate)
						.flatMap(message -> this.responseCreator.createSuccessMessageResponse(message, null, null, r)));
	}

	/**
	 * This is a highly restricted API, requires special privilege..and cannot be
	 * done by the profile owner but by the Admin
	 * 
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	@Loggable
	public Mono<ServerResponse> enableUserProfile(ServerRequest r) {

		String userId = r.pathVariable("id");
		log.info("Enable User Profile...");

		return userAuthenticationService.enableUserProfile(userId, true)
				.flatMap(message -> this.responseCreator.createSuccessMessageResponse(message, null, null, r));
	}

	/**
	 * 
	 * @param r
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	@Loggable
	public Mono<ServerResponse> disableUserProfile(ServerRequest r) {

		String userId = r.pathVariable("id");
		log.info("Disable User Profile...");

		return userAuthenticationService.enableUserProfile(userId, false)
				.flatMap(message -> this.responseCreator.createSuccessMessageResponse(message, null, null, r));
	}

	/**
	 * 
	 * @param r
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	@Loggable
	public Mono<ServerResponse> logMeOut(ServerRequest r) {
		final String refreshToken = r.headers().firstHeader("refresh-token");
		if (StringUtils.isBlank(refreshToken)) {
			return this.responseCreator.createErrorMessageResponse("Missing refresh token in the header",
					HttpStatus.BAD_REQUEST, null, null, r);
		}

		return authUserMgr.getLoggedInUser(r.exchange())
				.flatMap(username -> this.userAuthenticationService.signout(username, refreshToken))
				.flatMap(msg -> this.responseCreator.createSuccessMessageResponse(msg, null, null, r))
				.timeout(Duration.ofSeconds(3));
	}

	/**
	 * Decided the refresh token be sent as Request Body in order to avoid expecting
	 * client to adhere to a particular way of securing the refresh token
	 * 
	 * @param request
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	@Loggable
	public Mono<ServerResponse> logout(ServerRequest r) {
		String userId = r.pathVariable("id");

		final String refreshToken = r.headers().firstHeader("refresh-token");
		if (StringUtils.isBlank(refreshToken)) {
			return this.responseCreator.createErrorMessageResponse("Missing refresh token in the header",
					HttpStatus.BAD_REQUEST, null, null, r);
		}

		return this.userAuthenticationService.logout(userId, refreshToken)
				.flatMap(msg -> this.responseCreator.createSuccessMessageResponse(msg, null, null, r))
				.timeout(Duration.ofSeconds(3));
	}

	/**
	 * 
	 * @param refreshToken
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	@Loggable
	public Mono<ServerResponse> revokeToken(ServerRequest r) {

		// Extract the Refresh token
		final String refreshToken = r.headers().firstHeader("refresh-token");
		if (StringUtils.isBlank(refreshToken)) {
			return this.responseCreator.createErrorMessageResponse("Missing refresh token in the header",
					HttpStatus.BAD_REQUEST, null, null, r);
		}

		return authUserMgr.getLoggedInUser(r.exchange())
				.flatMap(username -> this.userAuthenticationService.revokeToken(refreshToken))
				// .defaultReadResponse(this.userAuthenticationService.revokeToken(refreshToken),
				// String.class, null, r))
				.flatMap(msg -> this.responseCreator.createSuccessMessageResponse(msg, null, null, r));
	}

	/**
	 * Decided the refresh token be sent as Request Body in order to avoid expecting
	 * client to adhere to a particular way of securing the refresh token
	 * 
	 * @param request
	 * 
	 * @return
	 */
	@Loggable
	public Mono<ServerResponse> refreshToken(ServerRequest r) {
		// Extract the Refresh token
		final String refreshToken = r.headers().firstHeader("refresh-token");
		if (StringUtils.isBlank(refreshToken)) {
			return this.responseCreator.createErrorMessageResponse("Missing refresh token in the header",
					HttpStatus.BAD_REQUEST, null, null, r);
		}

		String username = r.pathVariable("username");

		return this.responseCreator.defaultReadResponse(userAuthenticationService.refreshToken(username, refreshToken),
				AuthenticationResponse.class, null, r);
	}

}
