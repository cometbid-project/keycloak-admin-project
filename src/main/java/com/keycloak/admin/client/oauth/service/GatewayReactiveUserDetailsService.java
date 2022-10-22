package com.keycloak.admin.client.oauth.service;

import javax.validation.constraints.NotBlank;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.keycloak.admin.client.oauth.service.it.UserCredentialFinderService;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * 
 * @author Gbenga
 *
 */
@Log4j2
@Service
@Validated
public class GatewayReactiveUserDetailsService implements ReactiveUserDetailsService {

	private final UserCredentialFinderService userService;

	public GatewayReactiveUserDetailsService(UserCredentialFinderService userService) {
		this.userService = userService;
	}

	@Override
	public Mono<UserDetails> findByUsername(@NotBlank final String username) {
		log.info("Finding user with username: {}", username);

		return userService.findByUsername(username).cast(UserDetails.class);
	}
}
