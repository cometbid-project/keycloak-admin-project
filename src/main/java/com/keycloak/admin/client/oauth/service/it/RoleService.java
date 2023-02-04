/**
 * 
 */
package com.keycloak.admin.client.oauth.service.it;

import javax.validation.constraints.NotBlank;

import com.keycloak.admin.client.models.CreateRoleRequest;
import com.keycloak.admin.client.models.RoleVO;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
public interface RoleService {
	
	Mono<RoleVO> createRealmRole(final CreateRoleRequest roleRequest);
	
	Mono<RoleVO> createClientRole(final CreateRoleRequest roleRequest, final String clientId);
	
	Flux<RoleVO> findAllRealmRoles();
	
	Flux<RoleVO> findAllClientRoles(final String clientId);
	
	Mono<RoleVO> findRealmRoleByName(final String roleName);
	
	Mono<RoleVO> findClientRoleByName(final String roleName, final String clientId);

	Mono<String> makeRealmRoleComposite(final String roleToMakeComposite, CreateRoleRequest roleRequest);
	
	Mono<String> makeClientRoleComposite(CreateRoleRequest roleRequest, final String roleToMakeComposite,
			final String clientId);

	Mono<String> makeRealmRoleCompositeWithClientRole(final String realmRoleName, final CreateRoleRequest clientRole,
			final String clientId);

	Mono<String> deleteRealmRole(final String realmRoleName); 
	
	Mono<String> deleteClientRole(final String clientId, final String clientRoleName); 

}
