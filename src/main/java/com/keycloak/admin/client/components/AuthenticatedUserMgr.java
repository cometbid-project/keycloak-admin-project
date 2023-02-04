/**
 * 
 */
package com.keycloak.admin.client.components;

import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.keycloak.admin.client.error.helpers.ErrorPublisher;
import com.keycloak.admin.client.models.Username;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Component
public class AuthenticatedUserMgr {

	/**
	 * MVC Pattern/Approach
	 * 
	 * @return
	 */
	public Mono<Username> getCurrentUser(String mvc) {
		Optional<User> user = Optional.ofNullable(SecurityContextHolder.getContext())
				.map(SecurityContext::getAuthentication).filter(Authentication::isAuthenticated)
				.map(Authentication::getPrincipal).map(User.class::cast);

		String roles = null;
		String username = "Guest, you are not logged in";

		Username authUser = null;
		if (user.isPresent()) {
			username = user.get().getUsername();
			roles = user.get().getAuthorities().parallelStream().map(auth -> (GrantedAuthority) auth)
					.map(a -> a.getAuthority()).collect(Collectors.joining(","));
			authUser = new Username(username, roles);
		}
		return Mono.justOrEmpty(authUser);
	}

	/**
	 * Reactive Pattern/Approach
	 * 
	 * @return
	 */
	public Mono<Username> getCurrentUser() {
		return ReactiveSecurityContextHolder.getContext().map(SecurityContext::getAuthentication)
				.filter(Authentication::isAuthenticated).defaultIfEmpty(new UsernamePasswordAuthenticationToken("", ""))
				.map(auth -> {
					String username = "";
					String roles = "";
					Optional<String> optUser = Optional.ofNullable(auth.getName());
					if (optUser.isPresent()) {
						username = optUser.get();
						roles = auth.getAuthorities().parallelStream().map(role -> (GrantedAuthority) role)
								.map(a -> a.getAuthority()).collect(Collectors.joining(","));
					}
					return new Username(username, roles);
				});
	}

	/**
	 * 
	 * @return
	 */
	public Mono<String> getLoggedInUser(ServerWebExchange webExchange) {
		log.info("Server WebExchange {}", webExchange);

		return webExchange.getPrincipal().map(p -> p.getName()).log()
				.switchIfEmpty(ErrorPublisher.raiseUnauthenticatedUserError("unauthenticated.user", new Object[] {}));
	}

	/**
	 * Reactive Pattern/Approach
	 * 
	 * @return
	 */
	public Mono<User> getCurrentUserWithException() {

		return ReactiveSecurityContextHolder.getContext().map(SecurityContext::getAuthentication)
				.switchIfEmpty(ErrorPublisher.raiseUnauthenticatedUserError("unauthenticated.user", new Object[] {}))
				.map(Authentication::getPrincipal).cast(User.class);
	}

}
