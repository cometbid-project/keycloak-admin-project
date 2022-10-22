/**
 * 
 */
package com.keycloak.admin.client.service;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.keycloak.admin.client.common.activity.enums.ContentType;
import com.keycloak.admin.client.common.events.GenericSpringEvent;
import com.keycloak.admin.client.common.utils.LocaleContextUtils;
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;
import com.keycloak.admin.client.config.AppConfiguration;
import com.keycloak.admin.client.config.AuthProperties;
import com.keycloak.admin.client.config.MessageConfig;
import com.keycloak.admin.client.config.SecurityConfig;
import com.keycloak.admin.client.dataacess.GroupBuilder;
import com.keycloak.admin.client.exceptions.ResourceAlreadyExistException;
import com.keycloak.admin.client.exceptions.ResourceNotFoundException;
import com.keycloak.admin.client.models.CreateGroupRequest;
import com.keycloak.admin.client.models.CreateRoleRequest;
import com.keycloak.admin.client.models.mappers.GroupMapper;
import com.keycloak.admin.client.oauth.service.GroupServiceImpl;
import com.keycloak.admin.client.oauth.service.it.GroupService;
import lombok.extern.log4j.Log4j2;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * @author Gbenga
 *
 */
@Log4j2
@WebFluxTest
@RecordApplicationEvents
@DisplayName("Verify Group service")
@ContextConfiguration(classes = { AppConfiguration.class, SecurityConfig.class, MessageConfig.class,
		AuthProperties.class })
@Import({ GroupServiceImpl.class, GroupMapper.class, LocaleContextUtils.class, CustomMessageSourceAccessor.class })
//@ExtendWith({ SpringExtension.class })
class GroupServiceAuthorizationTest {

	@Autowired
	private GroupService groupService;

	@Autowired
	private ApplicationEvents applicationEvents;

	@MockBean
	private Keycloak keycloak;
	@MockBean
	private RealmResource realmResource;
	@MockBean
	private GroupsResource groupsResource;
	@MockBean
	private GroupResource groupResource;

	@MockBean
	private Response createGroupResponse;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		when(keycloak.realm(anyString())).thenReturn(realmResource);

		when(realmResource.groups()).thenReturn(groupsResource);

		when(groupsResource.group(anyString())).thenReturn(groupResource);
	}

	@BeforeAll
	static void initializeData() {

	}
	
	private void checkApplicationEventPublished(int expectedEventCount) {
		
		Assertions.assertEquals(expectedEventCount, applicationEvents.stream(GenericSpringEvent.class)
				.filter(event -> event.getContentActedOn() == ContentType.USER_GROUP).count());

		applicationEvents.stream().forEach(System.out::println);

		Predicate<? super ApplicationEvent> predicate = p -> p instanceof GenericSpringEvent;

		Assertions.assertTrue(applicationEvents.stream().anyMatch(predicate));
	}

	/**
	 * 
	 */
	@DisplayName("grants access to find Realm group by id for ADMIN user")
	@Test
	@WithMockUser(roles = { "ADMIN" })
	void verifyFindGroupByIdAccessIsGrantedForAdminOnly() {

		UUID groupId = UUID.randomUUID();

		GroupRepresentation newGroupRepresentation = GroupBuilder.groupRepresentation(groupId);

		when(groupResource.toRepresentation()).thenReturn(newGroupRepresentation);

		StepVerifier.create(groupService.findRealmGroupById(groupId.toString())).expectNextCount(1).verifyComplete();

		checkApplicationEventPublished(1);
	}

	/**
	 * 
	 */
	@DisplayName("grants access to find Realm group by id for ADMIN user by group not found")
	@Test
	@WithMockUser(roles = { "ADMIN" })
	void verifyFindGroupByIdDenyAccessForAdminGroupNotFound() {

		UUID groupId = UUID.randomUUID();
		GroupRepresentation newGroupRepresentation = GroupBuilder.groupRepresentation(groupId);

		when(groupResource.toRepresentation()).thenReturn(null);

		StepVerifier.create(groupService.findRealmGroupById(groupId.toString()))
				.verifyError(ResourceNotFoundException.class);
		
	}

	/**
	 * 
	 */
	@DisplayName("deny access to find Realm group by id for any other user role")
	@Test
	@WithMockUser(roles = { "USER" })
	void verifyFindGroupByIdDenyAccessForAllUserExceptAdmin() {

		UUID groupId = UUID.randomUUID();

		GroupRepresentation newGroupRepresentation = GroupBuilder.groupRepresentation(groupId);

		when(groupResource.toRepresentation()).thenReturn(newGroupRepresentation);

		StepVerifier.create(groupService.findRealmGroupById(groupId.toString()))
				.verifyError(AccessDeniedException.class);
		
	}

	/**
	 * 
	 */
	@DisplayName("deny access to find Realm group by id for anonymous user")
	@Test
	void verifyFindRoleByNameDenyAccessForUnauthenticated() {

		UUID groupId = UUID.randomUUID();

		GroupRepresentation newGroupRepresentation = GroupBuilder.groupRepresentation(groupId);

		when(groupResource.toRepresentation()).thenReturn(newGroupRepresentation);

		StepVerifier.create(groupService.findRealmGroupById(groupId.toString()))
				.verifyError(AccessDeniedException.class);
		
	}

	/**
	 * 
	 */
	@DisplayName("grants access to find all Realm groups for 'ADMIN'")
	@Test
	@WithMockUser(roles = "ADMIN")
	void verifyFindAllRealmRoleAccessIsGrantedForAdmin() {

		List<GroupRepresentation> groupList = GroupBuilder.groupRepresentationList();
		when(groupsResource.groups()).thenReturn(groupList);

		StepVerifier.create(groupService.findAllRealmGroups()).expectNextCount(groupList.size()).verifyComplete();
		
		checkApplicationEventPublished(1);
	}

	/**
	 * 
	 */
	@DisplayName("denies access to find all Realm groups for all roles except 'ADMIN'")
	@Test
	@WithMockUser(roles = { "USER", "APP_MANAGER" })
	void verifyFindAllRealmRoleAccessIsDeniedForUserAndCurator() {

		List<GroupRepresentation> groupList = GroupBuilder.groupRepresentationList();
		when(groupsResource.groups()).thenReturn(groupList);

		StepVerifier.create(groupService.findAllRealmGroups()).verifyError(AccessDeniedException.class);
	}

	/**
	 * 
	 */
	@DisplayName("denies access to find all Realm groups for anonymous user")
	@Test
	void verifyFindAllRealmRoleAccessIsDeniedForUnauthenticated() {

		List<GroupRepresentation> groupList = GroupBuilder.groupRepresentationList();
		when(groupsResource.groups()).thenReturn(groupList);

		StepVerifier.create(groupService.findAllRealmGroups()).verifyError(AccessDeniedException.class);
	}

	/**
	 * 
	 */
	@DisplayName("grants access to find all Realm group names for 'ADMIN'")
	@Test
	@WithMockUser(roles = "ADMIN")
	void verifyFindAllRealmGroupNamesAccessIsGrantedForAdmin() {

		List<GroupRepresentation> groupList = GroupBuilder.groupRepresentationList();
		when(groupsResource.groups()).thenReturn(groupList);

		StepVerifier.create(groupService.findAllRealmGroupNames()).expectNextCount(groupList.size()).verifyComplete();
		
		checkApplicationEventPublished(1);
	}

	/**
	 * 
	 */
	@DisplayName("denies access to find all Realm group names for all roles except 'ADMIN'")
	@Test
	@WithMockUser(roles = { "USER", "APP_MANAGER" })
	void verifyFindAllRealmGroupNamesAccessIsDeniedForUserAndCurator() {

		List<GroupRepresentation> groupList = GroupBuilder.groupRepresentationList();
		when(groupsResource.groups()).thenReturn(groupList);

		StepVerifier.create(groupService.findAllRealmGroupNames()).verifyError(AccessDeniedException.class);
	}

	/**
	 * 
	 */
	@DisplayName("denies access to find all Realm group names for anonymous user")
	@Test
	void verifyFindAllRealmGroupNamesAccessIsDeniedForUnauthenticated() {

		List<GroupRepresentation> groupList = GroupBuilder.groupRepresentationList();
		when(groupsResource.groups()).thenReturn(groupList);

		StepVerifier.create(groupService.findAllRealmGroupNames()).verifyError(AccessDeniedException.class);
	}

	/**
	 * 
	 */
	@DisplayName("grants access to create realm group to 'ADMIN'")
	@Test
	@WithMockUser(roles = "ADMIN")
	void verifyCreateRealmGroupAccessIsGrantedForAdmin() {

		Integer CREATED_CODE = 201;

		GroupRepresentation newGroupRepresentation = GroupBuilder.groupRepresentation(UUID.randomUUID());

		when(groupResource.toRepresentation()).thenReturn(newGroupRepresentation);

		List<GroupRepresentation> groupList = GroupBuilder.groupRepresentationList();
		when(groupsResource.groups()).thenReturn(groupList);
		when(groupsResource.add(any(GroupRepresentation.class))).thenReturn(createGroupResponse);

		when(createGroupResponse.getStatus()).thenReturn(CREATED_CODE);
		when(createGroupResponse.getStatusInfo()).thenReturn(Status.CREATED);
		when(createGroupResponse.getLocation()).thenReturn(URI.create("/group/123"));

		CreateGroupRequest realmGroup = new CreateGroupRequest(newGroupRepresentation.getName());

		StepVerifier.create(groupService.createRealmGroup(realmGroup))
				.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result.getId())
						&& StringUtils.isNotBlank(result.getName())
						&& result.getName().equalsIgnoreCase(realmGroup.getGroupName()))
				.verifyComplete();
		
		checkApplicationEventPublished(3);
	}

	/**
	 * 
	 */
	@DisplayName("denies access to create realm role except ADMIN")
	@Test
	@WithMockUser(roles = { "USER", "APP_MANAGER" })
	void verifyCreateRealmRoleAccessIsDeniedForUserAndCurator() {

		GroupRepresentation newGroupRepresentation = GroupBuilder.groupRepresentation(UUID.randomUUID());

		List<GroupRepresentation> groupList = GroupBuilder.groupRepresentationList();
		when(groupsResource.groups()).thenReturn(groupList);
		when(groupsResource.add(newGroupRepresentation)).thenReturn(createGroupResponse);

		CreateGroupRequest realmGroup = new CreateGroupRequest(newGroupRepresentation.getName());

		StepVerifier.create(groupService.createRealmGroup(realmGroup)).verifyError(AccessDeniedException.class);
	}

	/**
	 * 
	 */
	@DisplayName("denies access to create a user for anonymous user")
	@Test
	void verifyCreateRoleAccessIsDeniedForUnauthenticated() {

		GroupRepresentation newGroupRepresentation = GroupBuilder.groupRepresentation(UUID.randomUUID());

		List<GroupRepresentation> groupList = GroupBuilder.groupRepresentationList();
		when(groupsResource.groups()).thenReturn(groupList);
		when(groupsResource.add(newGroupRepresentation)).thenReturn(createGroupResponse);

		CreateGroupRequest realmGroup = new CreateGroupRequest(newGroupRepresentation.getName());

		StepVerifier.create(groupService.createRealmGroup(realmGroup)).verifyError(AccessDeniedException.class);
	}

	/**
	 * 
	 */
	@DisplayName("grants access to create realm group to 'ADMIN' but group name exist")
	@Test
	@WithMockUser(roles = "ADMIN")
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
