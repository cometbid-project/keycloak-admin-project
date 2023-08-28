/**
 * 
 */
package com.keycloak.admin.client.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleByIdResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import com.keycloak.admin.client.common.utils.LocaleContextUtils;
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;
import com.keycloak.admin.client.config.AppConfiguration;
import com.keycloak.admin.client.config.AuthProfile;
import com.keycloak.admin.client.config.AuthProperties;
import com.keycloak.admin.client.config.MessageConfig;
import com.keycloak.admin.client.config.SecurityConfig;
import com.keycloak.admin.client.dataacess.RoleBuilder;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.exceptions.BadRequestException;
import com.keycloak.admin.client.exceptions.ResourceAlreadyExistException;
import com.keycloak.admin.client.exceptions.ResourceNotFoundException;
import com.keycloak.admin.client.models.CreateRoleRequest;
import com.keycloak.admin.client.models.mappers.RoleMapper;
import com.keycloak.admin.client.models.mappers.UserMapper;
import com.keycloak.admin.client.oauth.service.ActivationTokenServiceImpl;
import com.keycloak.admin.client.oauth.service.GatewayRedisCache;
import com.keycloak.admin.client.oauth.service.KeycloakOauthClient;
import com.keycloak.admin.client.oauth.service.RoleServiceImpl;
import com.keycloak.admin.client.oauth.service.it.RoleService;
import com.keycloak.admin.client.redis.service.ReactiveRedisComponent;
import com.keycloak.admin.client.token.utils.TotpManagerImpl;

import lombok.extern.log4j.Log4j2;
import reactor.test.StepVerifier;

/**
 * @author Gbenga
 *
 */
@Log4j2
@WebFluxTest
@DisplayName("Verify Role service")
@ContextConfiguration(classes = { AppConfiguration.class, SecurityConfig.class, 
				MessageConfig.class, AuthProperties.class })
@Import({ RoleServiceImpl.class, RoleMapper.class, 
		  LocaleContextUtils.class, CustomMessageSourceAccessor.class, 
		  ApplicationEventPublisher.class })
//@ExtendWith({ SpringExtension.class })
class RoleServiceAuthorizationTest {

	  @Autowired private RoleService roleService;
	  
	  @MockBean private ApplicationEventPublisher eventPublisher;
  	
	  @MockBean private Keycloak keycloak;
	  @MockBean private RealmResource realmResource;
	  @MockBean private RolesResource rolesResource;
	  @MockBean private ClientsResource clientsResource;
	  @MockBean private RoleResource roleResource;
	  @MockBean private ClientResource clientResource;
	  
	  @MockBean private RoleByIdResource roleByIdResource;
	  
	  @BeforeEach
	  void setUp() {
		  MockitoAnnotations.openMocks(this);
		  
		  when(keycloak.realm(anyString())).thenReturn(realmResource);
		     
		  when(realmResource.roles()).thenReturn(rolesResource);
		  
		  when(realmResource.clients()).thenReturn(clientsResource);	
		  
		  List<ClientRepresentation> clientRepList = RoleBuilder.clientRepresentationList();
		  when(clientsResource.findByClientId(anyString())).thenReturn(clientRepList);	
		  when(clientsResource.get(anyString())).thenReturn(clientResource);
		  when(clientResource.roles()).thenReturn(rolesResource); 
		  
		  when(rolesResource.get(anyString())).thenReturn(roleResource);
	  }

	  @BeforeAll 
	  static void initializeData() {
		  
	  }
	  
	  /**
	   * 
	   */
	  @DisplayName("grants access to find Realm role by name for ADMIN user")
	  @Test
	  @WithMockUser(roles = {"ADMIN"})
	  void verifyFindRoleByNameAccessIsGrantedForAdminOnly() {
		  		 
	     String roleName = RoleBuilder.getRandomRole().getName();
	    
	     RoleRepresentation roleRepresentation = RoleBuilder.role()
	    		.withName(roleName).roleRepresentation(UUID.randomUUID(), false);	     	     
	    
	     when(roleResource.toRepresentation())
          		.thenReturn(roleRepresentation);	    
	    	    
	     StepVerifier.create(roleService.findRealmRoleByName(roleName))
	        .expectNextCount(1)
	        .verifyComplete();
	  }
	  
	  /**
	   * 
	   */
	  @DisplayName("grants access to find Client role by name for ADMIN user")
	  @Test
	  @WithMockUser(roles = {"ADMIN"})
	  void verifyFindClientRoleByNameAccessIsGrantedForAdminOnly() {
		  		 
	     String roleName = RoleBuilder.getRandomRole().getName();
	     String clientId = UUID.randomUUID().toString();
	    
	     RoleRepresentation roleRepresentation = RoleBuilder.role()
	    		.withName(roleName).roleRepresentation(UUID.randomUUID(), false);
	    
	     when(roleResource.toRepresentation())
          		.thenReturn(roleRepresentation);	    
	    	    
	     StepVerifier.create(roleService.findClientRoleByName(roleName, clientId))
	        .expectNextCount(1).verifyComplete();
	  }
	  
	  /**
	   * 
	   */
	  @DisplayName("deny access to find Realm role by name for any other user role")
	  @Test
	  @WithMockUser(roles = {"USER"})
	  void verifyFindRoleByNameDenyAccessForAllUserExceptAdmin() {
		  		  
		  String roleName = RoleBuilder.getRandomRole().getName();
		  RoleRepresentation roleRepresentation = RoleBuilder.role()
		    		.withName(roleName).roleRepresentation(UUID.randomUUID(), false);
		    
		  when(roleResource.toRepresentation())
    				.thenReturn(roleRepresentation);
		    
		  StepVerifier.create(roleService.findRealmRoleByName(roleName))
		  		.verifyError(AccessDeniedException.class);
	  }
	  
	  /**
	   * 
	   */
	  @DisplayName("deny access to find Client role by name for any other user role")
	  @Test
	  @WithMockUser(roles = {"USER"})
	  void verifyFindClientRoleByNameDenyAccessForAllUserExceptAdmin() {
		  			 
		  String roleName = RoleBuilder.getRandomRole().getName();
		  String clientId = UUID.randomUUID().toString();
		    
		  RoleRepresentation roleRepresentation = RoleBuilder.role()
		   		.withName(roleName).roleRepresentation(UUID.randomUUID(), false);
		    
		  when(roleResource.toRepresentation())
	       		.thenReturn(roleRepresentation);	    
		    	    
		  StepVerifier.create(roleService.findClientRoleByName(roleName, clientId))
		  		.verifyError(AccessDeniedException.class);
	  }
	  
	  /**
	   * 
	   */
	  @DisplayName("deny access to find Realm role by name for anonymous user")
	  @Test
	  void verifyFindRoleByNameDenyAccessForUnauthenticated() {
		  		  
		  String roleName = RoleBuilder.getRandomRole().getName();
		  RoleRepresentation roleRepresentation = RoleBuilder.role()
		    		.withName(roleName).roleRepresentation(UUID.randomUUID(), false);
		    
		  when(roleResource.toRepresentation())
    				.thenReturn(roleRepresentation);
		    
		  StepVerifier.create(roleService.findRealmRoleByName(roleName))
		  		.verifyError(AccessDeniedException.class);
	  }
	  
	  /**
	   * 
	   */
	  @DisplayName("deny access to find Client role by name for anonymous user")
	  @Test
	  void verifyFindClientRoleByNameDenyAccessForUnauthenticated() {
		  			 
		  String roleName = RoleBuilder.getRandomRole().getName();
		  String clientId = UUID.randomUUID().toString();
		    
		  RoleRepresentation roleRepresentation = RoleBuilder.role()
		   		.withName(roleName).roleRepresentation(UUID.randomUUID(), false);
		    
		  when(roleResource.toRepresentation())
	       		.thenReturn(roleRepresentation);	    
		    	    
		  StepVerifier.create(roleService.findClientRoleByName(roleName, clientId))
		  		.verifyError(AccessDeniedException.class);
	  }
	
	  /**
	   * 
	   */
	  @DisplayName("grants access to create realm role to 'ADMIN'")
	  @Test
	  @WithMockUser(roles = "ADMIN")
	  void verifyCreateRealmRoleAccessIsGrantedForAdmin() {
		  		  
		  String roleName = "ROLE_APP";
		  String roleDesc = "Role description";
		  RoleRepresentation newRoleRepresentation = RoleBuilder.role()
		    		.withName(roleName).roleRepresentation(UUID.randomUUID(), false);
		  
		  List<RoleRepresentation> rolesRepresentation = RoleBuilder.rolesRepresentationList();				    
		  
		  doNothing().when(rolesResource).create(any(RoleRepresentation.class));		  
		  when(rolesResource.list()).thenReturn(rolesRepresentation); 		  
		  when(roleResource.toRepresentation())
					.thenReturn(newRoleRepresentation);
	    
		  CreateRoleRequest realmRole = new CreateRoleRequest(roleName, roleDesc);
		    
		  StepVerifier.create(roleService.createRealmRole(realmRole))
					  .expectNextMatches(result -> result != null && 
						StringUtils.isNotBlank(result.getId()) && 
						StringUtils.isNotBlank(result.getName()) && 
						result.getName().equalsIgnoreCase(realmRole.getRoleName())
				  	)
		        .verifyComplete();
	  }
	  
	  /**
	   * 
	   */
	  @DisplayName("grants access to create client role to 'ADMIN'")
	  @Test
	  @WithMockUser(roles = "ADMIN")
	  void verifyCreateClientRoleAccessIsGrantedForAdmin() {
		  		  
		  String roleName = "CLIENT_ROLE_APP";
		  String roleDesc = "Client Role description";
		  
		  RoleRepresentation newRoleRepresentation = RoleBuilder.role()
		    		.withName(roleName).roleRepresentation(UUID.randomUUID(), false);
		  
		  String clientId = UUID.randomUUID().toString();
		  
		  List<RoleRepresentation> rolesRepresentation = RoleBuilder.rolesRepresentationList();				    
		  
		  doNothing().when(rolesResource).create(any(RoleRepresentation.class));
		  
		  when(rolesResource.list()).thenReturn(rolesRepresentation); 		  
		  when(roleResource.toRepresentation())
					.thenReturn(newRoleRepresentation);		  
		  
		  CreateRoleRequest clientRole = new CreateRoleRequest(roleName, roleDesc);		  
		    
		  StepVerifier.create(roleService.createClientRole(clientRole, clientId))
					  .expectNextMatches(result -> result != null && 
						StringUtils.isNotBlank(result.getId()) && 
						StringUtils.isNotBlank(result.getName()) && 
						result.getName().equalsIgnoreCase(clientRole.getRoleName())
				  	)
		        .verifyComplete();
	  }
	
	  /**
	   * 
	   */
	  @DisplayName("denies access to create realm role except ADMIN")
	  @Test
	  @WithMockUser(roles = {"USER", "APP_MANAGER"})
	  void verifyCreateRealmRoleAccessIsDeniedForUserAndCurator() {
		  		  
		  String roleName = "ROLE_APP";
		  String roleDesc = "Role description";
		  RoleRepresentation newRoleRepresentation = RoleBuilder.role()
		    		.withName(roleName).roleRepresentation(UUID.randomUUID(), false);
		  
		  List<RoleRepresentation> rolesRepresentation = RoleBuilder.rolesRepresentationList();	
		  
		  doNothing().when(rolesResource).create(any(RoleRepresentation.class));		  
		  when(rolesResource.list()).thenReturn(rolesRepresentation); 		  
		  when(roleResource.toRepresentation())
					.thenReturn(newRoleRepresentation);
	    
		  CreateRoleRequest realmRole = new CreateRoleRequest(roleName, roleDesc);
		  
		  StepVerifier.create(roleService.createRealmRole(realmRole))
	        		.verifyError(AccessDeniedException.class);
	  }
	  
	  /**
	   * 
	   */
	  @DisplayName("denies access to create client role except ADMIN")
	  @Test
	  @WithMockUser(roles = {"USER", "APP_MANAGER"})
	  void verifyCreateClientRoleAccessIsDeniedForUserAndCurator() {
		  		  
		  String roleName = "CLIENT_ROLE_APP";
		  String roleDesc = "Client Role description";
		  
		  RoleRepresentation newRoleRepresentation = RoleBuilder.role()
		    		.withName(roleName).roleRepresentation(UUID.randomUUID(), false);
		  
		  String clientId = UUID.randomUUID().toString();
		  
		  List<RoleRepresentation> rolesRepresentation = RoleBuilder.rolesRepresentationList();				    
		  
		  doNothing().when(rolesResource).create(any(RoleRepresentation.class));
		  
		  when(rolesResource.list()).thenReturn(rolesRepresentation); 		  
		  when(roleResource.toRepresentation())
					.thenReturn(newRoleRepresentation);		  
		  
		  CreateRoleRequest clientRole = new CreateRoleRequest(roleName, roleDesc);		  
		    
		  StepVerifier.create(roleService.createClientRole(clientRole, clientId))
	        		.verifyError(AccessDeniedException.class);
	  }
	
	  /**
	   * 
	   */
	  @DisplayName("denies access to create a user for anonymous user")
	  @Test
	  void verifyCreateRoleAccessIsDeniedForUnauthenticated() {
		  		  
		  String roleName = "ROLE_APP";
		  String roleDesc = "Role description";
		  
		  RoleRepresentation newRoleRepresentation = RoleBuilder.role()
		    		.withName(roleName).roleRepresentation(UUID.randomUUID(), false);
		  
		  List<RoleRepresentation> rolesRepresentation = RoleBuilder.rolesRepresentationList();	
		  
		  doNothing().when(rolesResource).create(any(RoleRepresentation.class));		  
		  when(rolesResource.list()).thenReturn(rolesRepresentation); 		  
		  when(roleResource.toRepresentation())
					.thenReturn(newRoleRepresentation);
	    
		  CreateRoleRequest realmRole = new CreateRoleRequest(roleName, roleDesc);
		  
		  StepVerifier.create(roleService.createRealmRole(realmRole))
	        		.verifyError(AccessDeniedException.class);
	  }
	  
	  /**
	   * 
	   */
	  @DisplayName("denies access to create a user for anonymous user")
	  @Test
	  void verifyCreateClientRoleAccessIsDeniedForUnauthenticated() {
		  		  
		  String roleName = "CLIENT_ROLE_APP";
		  String roleDesc = "Client Role description";
		  
		  RoleRepresentation newRoleRepresentation = RoleBuilder.role()
		    		.withName(roleName).roleRepresentation(UUID.randomUUID(), false);
		  
		  String clientId = UUID.randomUUID().toString();
		  
		  List<RoleRepresentation> rolesRepresentation = RoleBuilder.rolesRepresentationList();				    
		  
		  doNothing().when(rolesResource).create(any(RoleRepresentation.class));
		  
		  when(rolesResource.list()).thenReturn(rolesRepresentation); 		  
		  when(roleResource.toRepresentation())
					.thenReturn(newRoleRepresentation);		  
		  
		  CreateRoleRequest clientRole = new CreateRoleRequest(roleName, roleDesc);
		  
		  StepVerifier.create(roleService.createClientRole(clientRole, clientId))
  					.verifyError(AccessDeniedException.class);
	  }
	  
	  
	  /**
	   * 
	   */
	  @DisplayName("grants access to create realm role to 'ADMIN' but role name exist")
	  @Test
	  @WithMockUser(roles = "ADMIN")
	  void verifyCreateRealmRoleWithExistingRolenameAccessIsGrantedForAdmin() {
		  		  
		  String roleName = RoleBuilder.getRandomRole().getName();
		  String roleDesc = "Role description";
		  
		  RoleRepresentation newRoleRepresentation = RoleBuilder.role()
		    		.withName(roleName).roleRepresentation(UUID.randomUUID(), false);
		  
		  List<RoleRepresentation> rolesRepresentation = RoleBuilder.rolesRepresentationList();	
		  
		  doNothing().when(rolesResource).create(any(RoleRepresentation.class));		  
		  when(rolesResource.list()).thenReturn(rolesRepresentation); 		  
		  when(roleResource.toRepresentation())
					.thenReturn(newRoleRepresentation);
	    
		  CreateRoleRequest realmRole = new CreateRoleRequest(roleName);
		    
		  StepVerifier.create(roleService.createRealmRole(realmRole))
		  		.verifyError(ResourceAlreadyExistException.class);
	  }
	  
	  /**
	   * 
	   */
	  @DisplayName("grants access to create client role to 'ADMIN' but role name exist")
	  @Test
	  @WithMockUser(roles = "ADMIN")
	  void verifyCreateClientRoleWithExistingRolenameAccessIsGrantedForAdmin() {
		  		  
		  String roleName = RoleBuilder.getRandomRole().getName();
		  RoleRepresentation newRoleRepresentation = RoleBuilder.role()
		    		.withName(roleName).roleRepresentation(UUID.randomUUID(), false);
		  
		  String clientId = UUID.randomUUID().toString();
		  
		  List<RoleRepresentation> rolesRepresentation = RoleBuilder.rolesRepresentationList();				    
		  
		  doNothing().when(rolesResource).create(any(RoleRepresentation.class));

		  when(rolesResource.list()).thenReturn(rolesRepresentation); 		  
		  when(roleResource.toRepresentation())
					.thenReturn(newRoleRepresentation);		  
		  
		  CreateRoleRequest clientRole = new CreateRoleRequest(roleName);
		    
		  StepVerifier.create(roleService.createClientRole(clientRole, clientId))
		  		.verifyError(ResourceAlreadyExistException.class);
	  }	  
	
	  /**
	   * 
	   */
	  @DisplayName("grants access to find all Realm roles for role 'ADMIN'")
	  @Test
	  @WithMockUser(roles = "ADMIN")
	  void verifyFindAllRealmRoleAccessIsGrantedForAdmin() {
		  		  
		  List<RoleRepresentation> rolesRepresentation = RoleBuilder.rolesRepresentationList();	
		  when(rolesResource.list()).thenReturn(rolesRepresentation); 		  
	    
		  StepVerifier.create(roleService.findAllRealmRoles())
		  			.expectNextCount(rolesRepresentation.size())
		  			.verifyComplete();
	  }
	
	  /**
	   * 
	   */
	  @DisplayName("denies access to find all Realm roles except 'ADMIN'")
	  @Test
	  @WithMockUser(roles = {"USER", "APP_MANAGER"})
	  void verifyFindAllRealmRoleAccessIsDeniedForUserAndCurator() {
		  		  
		  List<RoleRepresentation> rolesRepresentation = RoleBuilder.rolesRepresentationList();	
		  when(rolesResource.list()).thenReturn(rolesRepresentation); 		
		  
		  StepVerifier.create(roleService.findAllRealmRoles())
		  			.verifyError(AccessDeniedException.class);
	  }
	
	  /**
	   * 
	   */
	  @DisplayName("denies access to find all Realm roles for anonymous user")
	  @Test
	  void verifyFindAllRealmRoleAccessIsDeniedForUnauthenticated() {
		  		  
		  List<RoleRepresentation> rolesRepresentation = RoleBuilder.rolesRepresentationList();	
		  when(rolesResource.list()).thenReturn(rolesRepresentation);
		  
		  StepVerifier.create(roleService.findAllRealmRoles())
		  			.verifyError(AccessDeniedException.class);
	  }
	  
	  
	  /**
	   * 
	   */
	  @DisplayName("grants access to find all Client roles for role 'ADMIN'")
	  @Test
	  @WithMockUser(roles = "ADMIN")
	  void verifyFindAllClientRoleAccessIsGrantedForAdmin() {
		  		  
		  List<RoleRepresentation> rolesRepresentation = RoleBuilder.rolesRepresentationList();	
		  when(rolesResource.list()).thenReturn(rolesRepresentation); 	
		  
		  String clientId = UUID.randomUUID().toString();
	    
		  StepVerifier.create(roleService.findAllClientRoles(clientId))
		  			.expectNextCount(rolesRepresentation.size())
		  			.verifyComplete();
	  }
	
	  /**
	   * 
	   */
	  @DisplayName("denies access to find all Client roles except 'ADMIN'")
	  @Test
	  @WithMockUser(roles = {"USER", "APP_MANAGER"})
	  void verifyFindAllClientRoleAccessIsDeniedForUserAndCurator() {
		  		  
		  List<RoleRepresentation> rolesRepresentation = RoleBuilder.rolesRepresentationList();	
		  when(rolesResource.list()).thenReturn(rolesRepresentation); 	
		  
		  String clientId = UUID.randomUUID().toString();
		  
		  StepVerifier.create(roleService.findAllClientRoles(clientId))
		  			.verifyError(AccessDeniedException.class);
	  }
	
	  /**
	   * 
	   */
	  @DisplayName("denies access to find all Client roles for anonymous user")
	  @Test
	  void verifyFindAllClientRoleAccessIsDeniedForUnauthenticated() {
		  		  
		  List<RoleRepresentation> rolesRepresentation = RoleBuilder.rolesRepresentationList();	
		  when(rolesResource.list()).thenReturn(rolesRepresentation);
		  
		  String clientId = UUID.randomUUID().toString();
		  
		  StepVerifier.create(roleService.findAllClientRoles(clientId))
		  			.verifyError(AccessDeniedException.class);
	  }	
	 
	  /**
	   * 
	   */
	  @DisplayName("grants access to make realm role Composite for 'ADMIN'")
	  @Test
	  @WithMockUser(roles = {"ADMIN"})
	  void verifyMakeRealmRoleCompositeAccessIsGrantedForAdmin() {
		  
		  String roleToComposite = RoleBuilder.getRandomRole().getName();		  
		  log.info("Role to composite {}", roleToComposite); 
		  
		  RoleRepresentation oldRoleRepresentation = RoleBuilder.role()
		    		.withName(roleToComposite).roleRepresentation(UUID.randomUUID(), false);
		  
		  RoleResource firstRoleResource = Mockito.mock(RoleResource.class);
		  
		  when(rolesResource.get(roleToComposite)).thenReturn(firstRoleResource);		  
		  when(firstRoleResource.toRepresentation()).thenReturn(oldRoleRepresentation);	
		  
		  // -----------------------------------------------------------------------------------------
		  
		  String roleToAdd = RoleBuilder.getRandomRoleExclude(roleToComposite).getName();	
		  log.info("Role to Add {}", roleToAdd); 
		  
		  RoleRepresentation newRoleRepresentation = RoleBuilder.role()
		    		.withName(roleToAdd).roleRepresentation(UUID.randomUUID(), false);
		  
		  RoleResource secondRoleResource = Mockito.mock(RoleResource.class);
		  
		  when(rolesResource.get(roleToAdd)).thenReturn(secondRoleResource);		  
		  when(secondRoleResource.toRepresentation()).thenReturn(newRoleRepresentation);
		  
		  CreateRoleRequest realmRoleToAdd = new CreateRoleRequest(roleToAdd);
		  
		  // -----------------------------------------------------------------------------------------
		  
		  when(realmResource.rolesById()).thenReturn(roleByIdResource); 	
		  doNothing().when(roleByIdResource).addComposites(anyString(), any(List.class)); 
		  
		  String expectedResponse = String.format("Role %s made a composite role of '%s'", 
				  			roleToComposite, roleToAdd);
		  
		  StepVerifier.create(roleService.makeRealmRoleComposite(roleToComposite, realmRoleToAdd))
					  .expectNextMatches(result -> 
						StringUtils.isNotBlank(result) && 
						result.equalsIgnoreCase(expectedResponse)
				  	)
					.verifyComplete();
	  }
	  
	  
	  /**
	   * 
	   */
	  @DisplayName("grants access to make realm role Composite for 'ADMIN' while role to Composite not found")
	  @Test
	  @WithMockUser(roles = {"ADMIN"})
	  void verifyMakeRealmRoleCompositeAccessIsGrantedForAdminRoleNotFound() {
		  
		  String roleToComposite = RoleBuilder.getRandomRole().getName();		  
		  log.info("Role to composite {}", roleToComposite); 
		  
		  RoleResource firstRoleResource = Mockito.mock(RoleResource.class);
		  
		  when(rolesResource.get(roleToComposite)).thenReturn(firstRoleResource);		  
		  when(firstRoleResource.toRepresentation()).thenReturn(null);	
		  
		  // -----------------------------------------------------------------------------------------
		  
		  String roleToAdd = RoleBuilder.getRandomRoleExclude(roleToComposite).getName();	
		  log.info("Role to Add {}", roleToAdd); 
		  
		  RoleRepresentation newRoleRepresentation = RoleBuilder.role()
		    		.withName(roleToAdd).roleRepresentation(UUID.randomUUID(), false);
		  
		  RoleResource secondRoleResource = Mockito.mock(RoleResource.class);
		  
		  when(rolesResource.get(roleToAdd)).thenReturn(secondRoleResource);		  
		  when(secondRoleResource.toRepresentation()).thenReturn(newRoleRepresentation);
		  
		  CreateRoleRequest realmRoleToAdd = new CreateRoleRequest(roleToAdd);
		  
		  // -----------------------------------------------------------------------------------------
		  
		  when(realmResource.rolesById()).thenReturn(roleByIdResource); 	
		  doNothing().when(roleByIdResource).addComposites(anyString(), any(List.class)); 
		  
		  String expectedResponse = String.format("Role %s made a composite role of '%s'", 
				  			roleToComposite, roleToAdd);
		  
		  StepVerifier.create(roleService.makeRealmRoleComposite(roleToComposite, realmRoleToAdd))
		  		.verifyError(ResourceNotFoundException.class);
	  }
	  
	  /**
	   * 
	   */
	  @DisplayName("grants access to make realm role Composite for 'ADMIN' while role to Add not found")
	  @Test
	  @WithMockUser(roles = {"ADMIN"})
	  void verifyMakeRealmRoleCompositeAccessIsGrantedForAdmin2ndRoleNotFound() {
		  
		  String roleToComposite = RoleBuilder.getRandomRole().getName();		  
		  log.info("Role to composite {}", roleToComposite); 
		  
		  RoleRepresentation oldRoleRepresentation = RoleBuilder.role()
		    		.withName(roleToComposite).roleRepresentation(UUID.randomUUID(), false);
		  
		  RoleResource firstRoleResource = Mockito.mock(RoleResource.class);
		  
		  when(rolesResource.get(roleToComposite)).thenReturn(firstRoleResource);		  
		  when(firstRoleResource.toRepresentation()).thenReturn(oldRoleRepresentation);	
		  
		  // -----------------------------------------------------------------------------------------
		  
		  String roleToAdd = RoleBuilder.getRandomRoleExclude(roleToComposite).getName();	
		  log.info("Role to Add {}", roleToAdd); 
		  
		  RoleRepresentation newRoleRepresentation = RoleBuilder.role()
		    		.withName(roleToAdd).roleRepresentation(UUID.randomUUID(), false);
		  
		  RoleResource secondRoleResource = Mockito.mock(RoleResource.class);
		  
		  when(rolesResource.get(roleToAdd)).thenReturn(secondRoleResource);		  
		  when(secondRoleResource.toRepresentation()).thenReturn(null);
		  
		  CreateRoleRequest realmRoleToAdd = new CreateRoleRequest(roleToAdd);
		  
		  // -----------------------------------------------------------------------------------------
		  
		  when(realmResource.rolesById()).thenReturn(roleByIdResource); 	
		  doNothing().when(roleByIdResource).addComposites(anyString(), any(List.class)); 
		  
		  String expectedResponse = String.format("Role %s made a composite role of '%s'", 
				  			roleToComposite, roleToAdd);
		  
		  StepVerifier.create(roleService.makeRealmRoleComposite(roleToComposite, realmRoleToAdd))
	  				.verifyError(ResourceNotFoundException.class);
	  }
	  
	  /**
	   * 
	   */
	  @DisplayName("grant access to make realm role Composite for 'ADMIN' with role name conflict")
	  @Test
	  @WithMockUser(roles = {"ADMIN"})
	  void verifyMakeRealmRoleCompositeAccessIsGrantedForAdminRoleNameConflict() {
		  
		  String roleToComposite = RoleBuilder.getRandomRole().getName();
		  
		  CreateRoleRequest realmRoleToAdd = new CreateRoleRequest(roleToComposite);
		  
		  StepVerifier.create(roleService.makeRealmRoleComposite(roleToComposite, realmRoleToAdd))
	        	.verifyError(BadRequestException.class);
	  }
	  
	  
	  /**
	   * 
	   */
	  @DisplayName("denies access to make realm role Composite for users except 'ADMIN'")
	  @Test
	  @WithMockUser(roles = {"USER", "APP_MANAGER"})
	  void verifyMakeRealmRoleCompositeAccessIsDeniedForUsers() {
		  
		  String roleToComposite = RoleBuilder.getRandomRole().getName();
		  
		  CreateRoleRequest realmRoleToAdd = new CreateRoleRequest(RoleBuilder.getRandomRole().getName());
		  
		  StepVerifier.create(roleService.makeRealmRoleComposite(roleToComposite, realmRoleToAdd))
	        	.verifyError(AccessDeniedException.class);
	  }
	
	
	  /**
	   * 
	   */
	  @DisplayName("denies access to make Realm role Composite for anonymous user")
	  @Test
	  void verifyMakeRealmRoleCompositeAccessIsDeniedForUnauthenticated() {
		  
		  String roleToComposite = RoleBuilder.getRandomRole().getName();
		  
		  CreateRoleRequest realmRoleToAdd = new CreateRoleRequest(RoleBuilder.getRandomRole().getName());
		  
		  StepVerifier.create(roleService.makeRealmRoleComposite(roleToComposite, realmRoleToAdd))
	        	.verifyError(AccessDeniedException.class);
	  }
	
	  //==============================================================================================================
	 
	  /**
	   * 
	   */
	  @DisplayName("grants access to make client role Composite for 'ADMIN'")
	  @Test
	  @WithMockUser(roles = {"ADMIN"})
	  void verifyMakeClientRoleCompositeAccessIsGrantedForAdmin() {
		  
		  String roleToComposite = RoleBuilder.getRandomRole().getName();		  
		  log.info("Role to composite {}", roleToComposite); 
		  
		  RoleRepresentation oldRoleRepresentation = RoleBuilder.role()
		    		.withName(roleToComposite).roleRepresentation(UUID.randomUUID(), false);
		  
		  RoleResource firstRoleResource = Mockito.mock(RoleResource.class);
		  
		  when(rolesResource.get(roleToComposite)).thenReturn(firstRoleResource);		  
		  when(firstRoleResource.toRepresentation()).thenReturn(oldRoleRepresentation);	

		  // ----------------------------------------------------------------------------------------- 	
		  
		  String roleToAdd = RoleBuilder.getRandomRoleExclude(roleToComposite).getName();	
		  log.info("Role to Add {}", roleToAdd); 
		  
		  RoleRepresentation newRoleRepresentation = RoleBuilder.role()
		    		.withName(roleToAdd).roleRepresentation(UUID.randomUUID(), false);
		  
		  RoleResource secondRoleResource = Mockito.mock(RoleResource.class);
		  
		  when(rolesResource.get(roleToAdd)).thenReturn(secondRoleResource);		  
		  when(secondRoleResource.toRepresentation()).thenReturn(newRoleRepresentation);
		  
		  CreateRoleRequest realmRoleToAdd = new CreateRoleRequest(roleToAdd);
		  
		  // -----------------------------------------------------------------------------------------
		  
		  when(realmResource.rolesById()).thenReturn(roleByIdResource); 	
		  doNothing().when(roleByIdResource).addComposites(anyString(), any(List.class)); 
		  
		  String expectedResponse = String.format("Role %s made a composite role of '%s'", 
				  			roleToComposite, roleToAdd);
		  
		  String clientId = UUID.randomUUID().toString();
		  
		  StepVerifier.create(roleService.makeClientRoleComposite(realmRoleToAdd, roleToComposite, clientId))
					  .expectNextMatches(result -> 
						StringUtils.isNotBlank(result) && 
						result.equalsIgnoreCase(expectedResponse)
				  	)
					.verifyComplete();
	  }
	  
	  
	  /**
	   * 
	   */
	  @DisplayName("grants access to make Client role Composite for 'ADMIN' while role to Composite not found")
	  @Test
	  @WithMockUser(roles = {"ADMIN"})
	  void verifyMakeClientRoleCompositeAccessIsGrantedForAdminRoleNotFound() {
		  
		  String roleToComposite = RoleBuilder.getRandomRole().getName();		  
		  log.info("Role to composite {}", roleToComposite); 
		  
		  RoleResource firstRoleResource = Mockito.mock(RoleResource.class);
		  
		  when(rolesResource.get(roleToComposite)).thenReturn(firstRoleResource);		  
		  when(firstRoleResource.toRepresentation()).thenReturn(null);	
		  
		  // -----------------------------------------------------------------------------------------
		  
		  String roleToAdd = RoleBuilder.getRandomRoleExclude(roleToComposite).getName();	
		  log.info("Role to Add {}", roleToAdd); 
		  
		  RoleRepresentation newRoleRepresentation = RoleBuilder.role()
		    		.withName(roleToAdd).roleRepresentation(UUID.randomUUID(), false);
		  
		  RoleResource secondRoleResource = Mockito.mock(RoleResource.class);
		  
		  when(rolesResource.get(roleToAdd)).thenReturn(secondRoleResource);		  
		  when(secondRoleResource.toRepresentation()).thenReturn(newRoleRepresentation);
		  
		  CreateRoleRequest realmRoleToAdd = new CreateRoleRequest(roleToAdd);
		  
		  // -----------------------------------------------------------------------------------------
		  
		  when(realmResource.rolesById()).thenReturn(roleByIdResource); 	
		  doNothing().when(roleByIdResource).addComposites(anyString(), any(List.class)); 
		  
		  String expectedResponse = String.format("Role %s made a composite role of '%s'", 
				  			roleToComposite, roleToAdd);
		  
		  String clientId = UUID.randomUUID().toString();
		  
		  StepVerifier.create(roleService.makeClientRoleComposite(realmRoleToAdd, roleToComposite, clientId))
		  		.verifyError(ResourceNotFoundException.class);
	  }
	  
	  /**
	   * 
	   */
	  @DisplayName("grants access to make Client role Composite for 'ADMIN' while role to Add not found")
	  @Test
	  @WithMockUser(roles = {"ADMIN"})
	  void verifyMakeClientRoleCompositeAccessIsGrantedForAdmin2ndRoleNotFound() {
		  
		  String roleToComposite = RoleBuilder.getRandomRole().getName();		  
		  log.info("Role to composite {}", roleToComposite); 
		  
		  RoleRepresentation oldRoleRepresentation = RoleBuilder.role()
		    		.withName(roleToComposite).roleRepresentation(UUID.randomUUID(), false);
		  
		  RoleResource firstRoleResource = Mockito.mock(RoleResource.class);
		  
		  when(rolesResource.get(roleToComposite)).thenReturn(firstRoleResource);		  
		  when(firstRoleResource.toRepresentation()).thenReturn(oldRoleRepresentation);	
		  
		  // -----------------------------------------------------------------------------------------
		  
		  String roleToAdd = RoleBuilder.getRandomRoleExclude(roleToComposite).getName();	
		  log.info("Role to Add {}", roleToAdd); 
		  
		  RoleResource secondRoleResource = Mockito.mock(RoleResource.class);
		  
		  when(rolesResource.get(roleToAdd)).thenReturn(secondRoleResource);		  
		  when(secondRoleResource.toRepresentation()).thenReturn(null);
		  
		  CreateRoleRequest realmRoleToAdd = new CreateRoleRequest(roleToAdd);
		  
		  // -----------------------------------------------------------------------------------------
		  
		  when(realmResource.rolesById()).thenReturn(roleByIdResource); 	
		  doNothing().when(roleByIdResource).addComposites(anyString(), any(List.class)); 
		  
		  String expectedResponse = String.format("Role %s made a composite role of '%s'", 
				  			roleToComposite, roleToAdd);
		  
		  String clientId = UUID.randomUUID().toString();
		  
		  StepVerifier.create(roleService.makeClientRoleComposite(realmRoleToAdd, roleToComposite, clientId))
	  				.verifyError(ResourceNotFoundException.class);
	  }
	  
	  /**
	   * 
	   */
	  @DisplayName("grant access to make realm role Composite for 'ADMIN' with role name conflict")
	  @Test
	  @WithMockUser(roles = {"ADMIN"})
	  void verifyMakeClientRoleCompositeAccessIsGrantedForAdminRoleNameConflict() {
		  
		  String roleToComposite = RoleBuilder.getRandomRole().getName();
		  
		  CreateRoleRequest realmRoleToAdd = new CreateRoleRequest(roleToComposite);
		  
		  String clientId = UUID.randomUUID().toString();
		  
		  StepVerifier.create(roleService.makeClientRoleComposite(realmRoleToAdd, roleToComposite, clientId))
		  				.verifyError(BadRequestException.class);
	  }
	  
	  
	  /**
	   * 
	   */
	  @DisplayName("denies access to make Client role Composite for users except 'ADMIN'")
	  @Test
	  @WithMockUser(roles = {"USER", "APP_MANAGER"})
	  void verifyMakeClientRoleCompositeAccessIsDeniedForUsers() {
		  
		  String roleToComposite = RoleBuilder.getRandomRole().getName();
		  
		  CreateRoleRequest realmRoleToAdd = new CreateRoleRequest(RoleBuilder.getRandomRole().getName());
		  
		  String clientId = UUID.randomUUID().toString();
		  
		  StepVerifier.create(roleService.makeClientRoleComposite(realmRoleToAdd, roleToComposite, clientId))
	        	.verifyError(AccessDeniedException.class);
	  }
	
	
	  /**
	   * 
	   */
	  @DisplayName("denies access to make Client role Composite for anonymous user")
	  @Test
	  void verifyMakeClientRoleCompositeAccessIsDeniedForUnauthenticated() {
		  
		  String roleToComposite = RoleBuilder.getRandomRole().getName();
		  
		  CreateRoleRequest realmRoleToAdd = new CreateRoleRequest(RoleBuilder.getRandomRole().getName());
		  
		  String clientId = UUID.randomUUID().toString();
		  
		  StepVerifier.create(roleService.makeClientRoleComposite(realmRoleToAdd, roleToComposite, clientId))
	        	.verifyError(AccessDeniedException.class);
	  }
	  
	//==============================================================================================================
		 
	  /**
	   * 
	   */
	  @DisplayName("grants access to make realm role Composite with client role for 'ADMIN'")
	  @Test
	  @WithMockUser(roles = {"ADMIN"})
	  void verifyMakeRealmRoleCompositeWithClientRoleAccessIsGrantedForAdmin() {
		  
		  String roleToComposite = RoleBuilder.getRandomRole().getName();		  
		  log.info("Role to composite {}", roleToComposite); 
		  
		  RoleRepresentation oldRoleRepresentation = RoleBuilder.role()
		    		.withName(roleToComposite).roleRepresentation(UUID.randomUUID(), false);
		  
		  RoleResource firstRoleResource = Mockito.mock(RoleResource.class);
		  
		  when(rolesResource.get(roleToComposite)).thenReturn(firstRoleResource);		  
		  when(firstRoleResource.toRepresentation()).thenReturn(oldRoleRepresentation);	

		  // ----------------------------------------------------------------------------------------- 	
		  
		  String roleToAdd = RoleBuilder.getRandomRoleExclude(roleToComposite).getName();	
		  log.info("Role to Add {}", roleToAdd); 
		  
		  RoleRepresentation newRoleRepresentation = RoleBuilder.role()
		    		.withName(roleToAdd).roleRepresentation(UUID.randomUUID(), false);
		  
		  RoleResource secondRoleResource = Mockito.mock(RoleResource.class);
		  
		  when(rolesResource.get(roleToAdd)).thenReturn(secondRoleResource);		  
		  when(secondRoleResource.toRepresentation()).thenReturn(newRoleRepresentation);
		  
		  CreateRoleRequest clientRoleToAdd = new CreateRoleRequest(roleToAdd);
		  
		  // -----------------------------------------------------------------------------------------
		  
		  when(realmResource.rolesById()).thenReturn(roleByIdResource); 	
		  doNothing().when(roleByIdResource).addComposites(anyString(), any(List.class)); 
		  
		  String expectedResponse = String.format("Role %s made a composite role of '%s'", 
				  			roleToComposite, roleToAdd);
		  
		  String clientId = UUID.randomUUID().toString();
		  
		  StepVerifier.create(roleService.makeRealmRoleCompositeWithClientRole(roleToComposite, clientRoleToAdd, clientId))
					  .expectNextMatches(result -> 
						StringUtils.isNotBlank(result) && 
						result.equalsIgnoreCase(expectedResponse)
				  	)
					.verifyComplete();
	  }
	  
	  
	  /**
	   * 
	   */
	  @DisplayName("grants access to make Realm role Composite with client role for 'ADMIN' while Realm role not found")
	  @Test
	  @WithMockUser(roles = {"ADMIN"})
	  void verifyMakeRealmRoleCompositeWithClientRoleAccessIsGrantedForAdminRoleNotFound() {
		  
		  String roleToComposite = RoleBuilder.getRandomRole().getName();		  
		  log.info("Role to composite {}", roleToComposite); 
		  
		  RoleResource firstRoleResource = Mockito.mock(RoleResource.class);
		  
		  when(rolesResource.get(roleToComposite)).thenReturn(firstRoleResource);		  
		  when(firstRoleResource.toRepresentation()).thenReturn(null);	
		  
		  // -----------------------------------------------------------------------------------------
		  
		  String roleToAdd = RoleBuilder.getRandomRoleExclude(roleToComposite).getName();	
		  log.info("Role to Add {}", roleToAdd); 
		  
		  RoleRepresentation newRoleRepresentation = RoleBuilder.role()
		    		.withName(roleToAdd).roleRepresentation(UUID.randomUUID(), false);
		  
		  RoleResource secondRoleResource = Mockito.mock(RoleResource.class);
		  
		  when(rolesResource.get(roleToAdd)).thenReturn(secondRoleResource);		  
		  when(secondRoleResource.toRepresentation()).thenReturn(newRoleRepresentation);
		  
		  CreateRoleRequest realmRoleToAdd = new CreateRoleRequest(roleToAdd);
		  
		  // -----------------------------------------------------------------------------------------
		  
		  when(realmResource.rolesById()).thenReturn(roleByIdResource); 	
		  doNothing().when(roleByIdResource).addComposites(anyString(), any(List.class)); 
		  
		  String expectedResponse = String.format("Role %s made a composite role of '%s'", 
				  			roleToComposite, roleToAdd);
		  
		  String clientId = UUID.randomUUID().toString();
		  
		  StepVerifier.create(roleService.makeRealmRoleCompositeWithClientRole(roleToComposite, realmRoleToAdd, clientId))
		  		.verifyError(ResourceNotFoundException.class);
	  }
	  
	  /**
	   * 
	   */
	  @DisplayName("grants access to Realm role Composite with client role for 'ADMIN' while role to Client role not found")
	  @Test
	  @WithMockUser(roles = {"ADMIN"})
	  void verifyMakeRealmRoleCompositeWithClientRoleIsGrantedForAdmin2ndRoleNotFound() {
		  
		  String roleToComposite = RoleBuilder.getRandomRole().getName();		  
		  log.info("Role to composite {}", roleToComposite); 
		  
		  RoleRepresentation oldRoleRepresentation = RoleBuilder.role()
		    		.withName(roleToComposite).roleRepresentation(UUID.randomUUID(), false);
		  
		  RoleResource firstRoleResource = Mockito.mock(RoleResource.class);
		  
		  when(rolesResource.get(roleToComposite)).thenReturn(firstRoleResource);		  
		  when(firstRoleResource.toRepresentation()).thenReturn(oldRoleRepresentation);	
		  
		  // -----------------------------------------------------------------------------------------
		  
		  String roleToAdd = RoleBuilder.getRandomRoleExclude(roleToComposite).getName();	
		  log.info("Role to Add {}", roleToAdd); 
		  
		  RoleResource secondRoleResource = Mockito.mock(RoleResource.class);
		  
		  when(rolesResource.get(roleToAdd)).thenReturn(secondRoleResource);		  
		  when(secondRoleResource.toRepresentation()).thenReturn(null);
		  
		  CreateRoleRequest realmRoleToAdd = new CreateRoleRequest(roleToAdd);
		  
		  // -----------------------------------------------------------------------------------------
		  
		  when(realmResource.rolesById()).thenReturn(roleByIdResource); 	
		  doNothing().when(roleByIdResource).addComposites(anyString(), any(List.class)); 
		  
		  String expectedResponse = String.format("Role %s made a composite role of '%s'", 
				  			roleToComposite, roleToAdd);
		  
		  String clientId = UUID.randomUUID().toString();
		  
		  StepVerifier.create(roleService.makeRealmRoleCompositeWithClientRole(roleToComposite, realmRoleToAdd, clientId))
	  		.verifyError(ResourceNotFoundException.class);
	  }
	  
	  /**
	   * 
	   */
	  @DisplayName("grant access to make realm role Composite for 'ADMIN' with role name conflict")
	  @Test
	  @WithMockUser(roles = {"ADMIN"})
	  void verifyMakeRealmRoleCompositeWithClientRoleAccessIsGrantedForAdminRoleNameConflict() {
		  
		  String roleToComposite = RoleBuilder.getRandomRole().getName();
		  
		  CreateRoleRequest realmRoleToAdd = new CreateRoleRequest(roleToComposite);
		  
		  String clientId = UUID.randomUUID().toString();
		  
		  StepVerifier.create(roleService.makeRealmRoleCompositeWithClientRole(roleToComposite, realmRoleToAdd, clientId))
		  				.verifyError(BadRequestException.class);
	  }
	  
	  
	  /**
	   * 
	   */
	  @DisplayName("denies access to make Client role Composite for users except 'ADMIN'")
	  @Test
	  @WithMockUser(roles = {"USER", "APP_MANAGER"})
	  void verifyMakeRealmRoleCompositeWithClientRoleAccessIsDeniedForUsers() {
		  
		  String roleToComposite = RoleBuilder.getRandomRole().getName();
		  
		  CreateRoleRequest realmRoleToAdd = new CreateRoleRequest(RoleBuilder.getRandomRole().getName());
		  
		  String clientId = UUID.randomUUID().toString();
		  
		  StepVerifier.create(roleService.makeRealmRoleCompositeWithClientRole(roleToComposite, realmRoleToAdd, clientId))
	        	.verifyError(AccessDeniedException.class);
	  }
	
	
	  /**
	   * 
	   */
	  @DisplayName("denies access to make Client role Composite for anonymous user")
	  @Test
	  void verifyMakeRealmRoleCompositeWithClientRoleAccessIsDeniedForUnauthenticated() {
		  
		  String roleToComposite = RoleBuilder.getRandomRole().getName();
		  
		  CreateRoleRequest realmRoleToAdd = new CreateRoleRequest(RoleBuilder.getRandomRole().getName());
		  
		  String clientId = UUID.randomUUID().toString();
		  
		  StepVerifier.create(roleService.makeRealmRoleCompositeWithClientRole(roleToComposite, realmRoleToAdd, clientId))
	        	.verifyError(AccessDeniedException.class);
	  }
}
