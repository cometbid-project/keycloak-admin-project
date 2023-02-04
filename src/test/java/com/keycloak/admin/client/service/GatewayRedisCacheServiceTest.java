/**
 * 
 */
package com.keycloak.admin.client.service;


import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.event.RecordApplicationEvents;

import com.keycloak.admin.client.common.utils.LocaleContextUtils;
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;
import com.keycloak.admin.client.config.AppConfiguration;
import com.keycloak.admin.client.config.AuthProperties;
import com.keycloak.admin.client.config.MessageConfig;
import com.keycloak.admin.client.dataacess.GroupBuilder;
import com.keycloak.admin.client.exceptions.ResourceAlreadyExistException;
import com.keycloak.admin.client.models.CreateGroupRequest;
import com.keycloak.admin.client.oauth.service.GatewayRedisCache;
import com.keycloak.admin.client.redis.service.ReactiveRedisComponent;

import lombok.extern.log4j.Log4j2;
import reactor.test.StepVerifier;

/**
 * @author Gbenga
 *
 */
@Log4j2
@WebFluxTest
@DisplayName("Gateway Cache Service")
@ContextConfiguration(classes = { AppConfiguration.class, MessageConfig.class,
		AuthProperties.class })
@Import({ ReactiveRedisComponent.class, LocaleContextUtils.class, CustomMessageSourceAccessor.class })
class GatewayRedisCacheServiceTest {

	@MockBean
	private ReactiveRedisComponent redisComponent;

	@Autowired
	private GatewayRedisCache redisCache;
	
	/**
	 * 
	 */
	@DisplayName("grants access to create realm group to 'ADMIN' but group name exist")
	@Test
	void verifyCreateRealmRoleWithExistingRolenameAccessIsGrantedForAdmin() {
		List<GroupRepresentation> groupList = GroupBuilder.groupRepresentationList();
		when(groupsResource.groups()).thenReturn(groupList);

		String groupName = groupList.get(0).getName();

		GroupRepresentation newGroupRepresentation = GroupBuilder.groupRepresentation(UUID.randomUUID());
		when(groupsResource.add(newGroupRepresentation)).thenReturn(createGroupResponse);

		CreateGroupRequest realmGroup = new CreateGroupRequest(groupName);

		StepVerifier.create(groupService.createRealmGroup(realmGroup)).verifyError(ResourceAlreadyExistException.class);
	}

}
