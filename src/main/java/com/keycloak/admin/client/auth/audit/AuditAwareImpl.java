/**
 * 
 */
package com.keycloak.admin.client.auth.audit;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.data.domain.ReactiveAuditorAware;

import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
public class AuditAwareImpl implements ReactiveAuditorAware<User> {

	@Override
	public Mono<User> getCurrentAuditor() {
		return getAuditor();
	}

	public Mono<User> getAuditor() {
		return ReactiveSecurityContextHolder.getContext().map(p -> p.getAuthentication())
				.filter(Authentication::isAuthenticated).map(Authentication::getPrincipal).map(User.class::cast);				
	}	
}
