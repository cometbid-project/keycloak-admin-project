/**
 * 
 */
package com.keycloak.admin.client.oauth.service.it;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.server.ServerRequest;

import com.keycloak.admin.client.entities.NewLocationToken;
import com.keycloak.admin.client.entities.UserloginLocation;
import com.keycloak.admin.client.events.dto.UserDTO;
import com.keycloak.admin.client.models.LoginLocation;

import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
public interface UserLocationService {

	/**
	 * 
	 */
	Mono<String> isValidNewLocationToken(final String token);

	/**
	 * 
	 */
	Mono<NewLocationToken> isNewLoginLocation(final String username, final ServerHttpRequest r);

	/**
	 * 
	 */
	Mono<UserloginLocation> recordNewUserLocation(final String username, final ServerHttpRequest r);

	/**
	 * 
	 * @param username
	 * @param r
	 * @return
	 */
	Mono<String> processNewLocationCheck(final String username, final ServerHttpRequest r);

	/**
	 * 
	 * @param r
	 * @return
	 */
	Mono<? extends LoginLocation> decodeUserLocation(final ServerHttpRequest r);

	/**
	 * 
	 * @param username
	 * @param emailAddr
	 * @param httpRequest
	 * @return
	 */
	UserDTO createDTOUser(final String username, final String emailAddr, final ServerHttpRequest r);      

}
