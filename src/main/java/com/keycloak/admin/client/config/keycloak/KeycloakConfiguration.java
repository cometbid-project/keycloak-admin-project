/**
 * 
 */
package com.keycloak.admin.client.config.keycloak;

import java.util.Collection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import com.keycloak.admin.client.config.AuthProperties;

import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */

@Configuration
public class KeycloakConfiguration {

	@Bean
	Converter<Jwt, Collection<GrantedAuthority>> keycloakGrantedAuthoritiesConverter(final AuthProperties keycloakProperties) {
		String clientId = keycloakProperties.getAdminClientId();
		return new KeycloakGrantedAuthoritiesConverter(clientId);
	}

	@Bean
	Converter<Jwt, Mono<AbstractAuthenticationToken>> keycloakJwtAuthenticationConverter(Converter<Jwt, Collection<GrantedAuthority>> converter) {
		return new ReactiveKeycloakJwtAuthenticationConverter(converter);
	}
}
