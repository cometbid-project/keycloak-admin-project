/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.keycloak.admin.client.oauth.service.it;

import java.util.Map;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;

import com.keycloak.admin.client.common.enums.Role;
import com.keycloak.admin.client.models.LocalUser;
import com.keycloak.admin.client.models.UserRegistrationRequest;
import com.keycloak.admin.client.models.UserVO;

import lombok.NonNull;
import reactor.core.publisher.Mono;

/**
 *
 * @author Gbenga
 */
public interface UserCredentialService {
	
	/**
	 * 
	 * @param newRole
	 * @return
	 */
	Mono<String> assignToGroup(final String id, final String groupId);

	/**
	 * 
	 * @param newRole
	 * @return
	 */
	Mono<String> assignRealmRole(final String id, final String roleName);

	/**
	 * 
	 * @param newRole
	 * @return
	 */
	Mono<String> assignClientRoleToUser(final String id, final String roleName, final String actualClientId);


	/**
	 * 
	 * @param regRequest
	 * @param role
	 * @param r
	 * @return
	 */
	Mono<UserVO> signupUser(@NonNull UserRegistrationRequest regRequest, Role role, ServerHttpRequest r);

	/**
	 * 
	 * @param registrationId
	 * @param attributes
	 * @param idToken
	 * @param userInfo
	 * @return
	 */
	Mono<LocalUser> processUserRegistration(String registrationId, Map<String, Object> attributes, OidcIdToken idToken,
		OidcUserInfo userInfo);

}
