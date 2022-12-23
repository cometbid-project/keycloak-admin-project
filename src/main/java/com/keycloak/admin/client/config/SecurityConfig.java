/**
 * 
 */
package com.keycloak.admin.client.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig implements WebFluxConfigurer {
	
	/*
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new Argon2PasswordEncoder(16, 32, 8, 1 << 16, 10);
	}
	*/
	
	@Bean
    public PasswordEncoder passwordEncoder() {
		// with new spring security 5
        return PasswordEncoderFactories.createDelegatingPasswordEncoder(); 
    }

	/**
	 * 
	 * @param http
	 * @return
	 */
	 @Bean
	 public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, 
			 Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter) {
		 
		// @formatter:off 
		 http.csrf()
         .disable()
		 .cors().configurationSource(corsConfigurationSource())
		 .and()
		   .httpBasic().disable()
		     .formLogin().disable();
		 
	        http
	            .authorizeExchange(
		            exchanges ->
		                exchanges
		                    .pathMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()	
		                    .pathMatchers("/hello/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
		                    .anyExchange().authenticated()
		                    .and()
		    	            .oauth2ResourceServer()
		    					.jwt()
		    					.jwtAuthenticationConverter(jwtAuthenticationConverter) 
	            )//           
	            //.httpBasic(withDefaults())
	            //.formLogin(withDefaults())
	            .redirectToHttps();
	     // @formatter:on
	        
	     return http.build();
	 }
	 
	 /*
	 @Bean
	 SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) throws Exception {
			return http
				.authorizeExchange()
					.pathMatchers(HttpMethod.GET, "/posts/**").permitAll()
					.pathMatchers("/login", "/register", "/home").permitAll()
	                .pathMatchers(HttpMethod.DELETE, "/posts/**").hasRole("ADMIN")
					.anyExchange().authenticated()
					.and()
				.build();
	 }
	 */

	 /**
	  * 
	  * @return
	  */
	@Bean
	CorsWebFilter corsWebFilter() {
		
		return new CorsWebFilter(corsConfigurationSource());
	}
	
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("*"));
		// configuration.setAllowedOrigins(List.of("http://localhost:8080"));
		configuration.setAllowedMethods(List.of(
                HttpMethod.GET.name(),
                HttpMethod.PUT.name(),
                HttpMethod.POST.name(),
                HttpMethod.DELETE.name()
        ));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		//configuration.setAllowedHeaders(List.of("X-USER-ID"));
		configuration.setMaxAge(8000L);
		configuration.setAllowCredentials(true);
				
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration.applyPermitDefaultValues());
        return source;
    }
}
