/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.keycloak.admin.client.handlers;

import java.net.URI;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;

import com.keycloak.admin.client.aop.qualifiers.Loggable;
import com.keycloak.admin.client.common.enums.Role;
import com.keycloak.admin.client.common.utils.ResponseCreator;
import com.keycloak.admin.client.models.UserRegistrationRequest;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.oauth.service.it.UserCredentialService;
import com.keycloak.admin.client.validators.GlobalProgrammaticValidator;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 *
 * @author Gbenga
 */
@Log4j2
@Component
public class RegistrationHandler {

	private final UserCredentialService userRegistrationService;
	private final ResponseCreator responseCreator;

	public RegistrationHandler(UserCredentialService userRegistrationService, ResponseCreator responseCreator) {
		this.userRegistrationService = userRegistrationService;
		this.responseCreator = responseCreator;
	}

	@Loggable
	public Mono<ServerResponse> hello(ServerRequest serverRequest) {

		return serverRequest.principal().map(Principal::getName)
				.flatMap(username -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
						.bodyValue(Collections.singletonMap("message", "Hello " + username + "!")));
	}

	/**
	 *
	 * @return
	 */
	@Loggable
	public Mono<ServerResponse> signupAdmin(ServerRequest r) {

		final Mono<UserRegistrationRequest> requestBody = r.bodyToMono(UserRegistrationRequest.class);

		return requestBody.flatMap(GlobalProgrammaticValidator::validate)

				.flatMap(userRegRequest -> {
					final Mono<UserVO> monoResult = userRegistrationService.signupUser(userRegRequest, Role.ROLE_ADMIN,
							r.exchange().getRequest());
					
					//URI uri = UriComponentsBuilder.fromPath(r.path()).path("{id}").build();
					URI uri = null;

					return this.responseCreator.defaultWriteResponse(monoResult, UserVO.class, null, uri, r);
				});
	}

}
