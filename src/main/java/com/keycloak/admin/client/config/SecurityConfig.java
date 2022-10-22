/**
 * 
 */
package com.keycloak.admin.client.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * @author Gbenga
 *
 */
@Configuration
@EnableWebSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig implements WebFluxConfigurer {
	
	/**
	 * 
	 * @return
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		// return new BCryptPasswordEncoder();
		return new Argon2PasswordEncoder(16, 32, 8, 1 << 16, 10);
	}


	/**
	 * 
	 * @param http
	 * @return
	 */
	 @Bean
	 public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		  
		 http
		 .cors().configurationSource(corsConfigurationSource())
		 .and()
		   .httpBasic().disable()
		     .formLogin().disable();
		 
	        http
	            .authorizeExchange(
		            exchanges ->
		                exchanges
		                    .anyExchange().authenticated()
	            )
	            //.httpBasic(withDefaults())
	            //.formLogin(withDefaults())
	            .redirectToHttps();
	        
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
		configuration.setAllowedMethods(List.of(
                HttpMethod.GET.name(),
                HttpMethod.PUT.name(),
                HttpMethod.POST.name(),
                HttpMethod.DELETE.name()
        ));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setMaxAge(8000L);
		configuration.setAllowCredentials(true);
				
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration.applyPermitDefaultValues());
        return source;
    }
}
