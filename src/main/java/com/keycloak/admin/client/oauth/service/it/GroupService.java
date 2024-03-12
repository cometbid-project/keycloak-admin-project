/**
 * 
 */
package com.keycloak.admin.client.oauth.service.it;

import jakarta.validation.constraints.NotBlank;

import com.keycloak.admin.client.models.CreateGroupRequest;
import com.keycloak.admin.client.models.GroupVO;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
public interface GroupService {
	
	Mono<GroupVO> createRealmGroup(final CreateGroupRequest newGroup);
	
	Flux<String> findAllRealmGroupNames();
	
	Flux<GroupVO> findAllRealmGroups();
	
	Mono<GroupVO> findRealmGroupById(final String id);

	Mono<String> deleteRealmGroup(String groupId); 

}
