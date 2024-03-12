/**
 * 
 */
package com.keycloak.admin.client.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keycloak.admin.client.common.enums.Role;
import com.keycloak.admin.client.common.utils.ResponseCreator;
import com.keycloak.admin.client.config.AuthProperties;
import com.keycloak.admin.client.config.SecurityConfig;
import com.keycloak.admin.client.config.keycloak.KeycloakConfiguration;
import com.keycloak.admin.client.controllers.GroupController;
import com.keycloak.admin.client.controllers.RoleController;
import com.keycloak.admin.client.dataacess.RoleBuilder;
import com.keycloak.admin.client.models.CreateRoleRequest;
import com.keycloak.admin.client.models.RoleVO;
import com.keycloak.admin.client.oauth.service.it.RoleService;

import lombok.extern.log4j.Log4j2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
//import static org.springframework.restdocs.request.RequestDocumentation.relaxedRequestParameters;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

/**
 * @author Gbenga
 *
 */
@Log4j2
@WebFluxTest(controllers = RoleController.class) // <1>
@DisplayName("User Role endpoints API")
@Import({ AuthProperties.class, ResponseCreator.class, RoleController.class })
@ExtendWith({ RestDocumentationExtension.class, SpringExtension.class })
@ContextConfiguration(classes = { SecurityConfig.class, KeycloakConfiguration.class })
class RoleApiAuthenticationTest {

	@Autowired 
	private ApplicationContext applicationContext;

	@Autowired
	private WebTestClient webTestClient;

    @MockBean 
    private RoleService roleService;
    
    protected String PATH = "/api/v1/roles";
    private String apiKey = "eyJhbGciOiJIUzI1NiJ9.eyJSb2xlIjoiQWRtaW4iLCJJc3N1ZXIiOiJJc3N1...";
	
	@BeforeEach
	void setUp(RestDocumentationContextProvider restDocumentation) {
		
		this.webTestClient = 
				WebTestClient.bindToApplicationContext(applicationContext)
				.apply(springSecurity())
				.configureClient()
				.baseUrl("http://localhost:8080")
				.filter(
					documentationConfiguration(restDocumentation)
						.operationPreprocessors()
						.withRequestDefaults(prettyPrint())
						.withResponseDefaults(prettyPrint()))
				.build();					
	}

	@DisplayName("as authenticated user is granted")
	@Nested
	class AuthenticatedRoleApi {

		@WithMockUser(roles = { "ADMIN" })
		@SuppressWarnings("unchecked")
		@DisplayName("to get all realm roles, authenticated")
		@Test
	    void verifyGetAllRolesAuthenticated() {

	    	log.info("running  " + this.getClass().getName() + ".getAllRealmRoles()");
			List<RoleVO> listRoles = RoleBuilder.roleList();
			
			given(roleService.findAllRealmRoles())
					.willReturn(Flux.fromIterable(listRoles));

			String endPointURI = "";	
			final Map<String, String> params = new HashMap<>();
			params.put("lang", "fr");
			
			Map<String, Object> templateVar = new HashMap<>();			
			/*
			String token = RandomGenerator.generateSecureRandomHexToken(16);
			Faker faker = new Faker();
			String username = faker.internet().safeEmailAddress();
			*/
			
			restCallRoleResponseMultiple(endPointURI,
				 templateVar, params, listRoles.toArray(RoleVO[]::new))			
				.consumeWith(
		            document(
		                    "get all realm roles", 			                    
		                    preprocessRequest(prettyPrint()), 
		                    preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName("Content-Type")
						  .description("The Content-Type of the request payload, e.g. `application/json`"),
						headerWithName("api-key")
						  .description("The API Key unique to each registered user for Rate-limiting")
						//headerWithName("Authorization")
						  //.description("The Oauth2 `Bearer` token issued at login for Request Authentication")
					),
					responseHeaders(
								headerWithName("Content-Type")
								  .description("The Content-Type of the response payload, e.g. `application/json`"))
					/*
					relaxedRequestParameters( 
							parameterWithName("lang")
							.description("To instruct the resource to use a language of choice in"
									+ " the response messages. en=English, fr=French etc"))
					*/
			));
	    }

		@WithMockUser(roles = { "ADMIN" })
		@Test
		@DisplayName("to get all client roles, authenticated")
	    void verifyGetClientRolesAuthenticated() {

			log.info("running  " + this.getClass().getName() + ".findRealmGroup()");
			List<RoleVO> listRoles = RoleBuilder.roleList();
			
			given(roleService.findAllClientRoles(anyString()))
					.willReturn(Flux.fromIterable(listRoles));

			UUID clientId = UUID.randomUUID();
			String endPointURI = "/client/" + clientId.toString();	
			
			final Map<String, String> params = new HashMap<>();
			params.put("lang", "fr");
			
			Map<String, Object> templateVar = new HashMap<>();			
			/*
			String token = RandomGenerator.generateSecureRandomHexToken(16);
			Faker faker = new Faker();
			String username = faker.internet().safeEmailAddress();
			*/			
			restCallRoleResponseMultiple(endPointURI,
					 templateVar, params, 
					 listRoles.toArray(RoleVO[]::new))			
				.consumeWith(
		            document(
		                    "get all client roles", 			                    
		                    preprocessRequest(prettyPrint()), 
		                    preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName("Content-Type")
						  .description("The Content-Type of the request payload, e.g. `application/json`"),
						headerWithName("api-key")
						  .description("The API Key unique to each registered user for Rate-limiting")
						//headerWithName("Authorization")
						  //.description("The Oauth2 `Bearer` token issued at login for Request Authentication")
					),
					responseHeaders(
								headerWithName("Content-Type")
								  .description("The Content-Type of the response payload, e.g. `application/json`"))
					/*
					relaxedRequestParameters( 
							parameterWithName("lang")
							.description("To instruct the resource to use a language of choice in"
									+ " the response messages. en=English, fr=French etc"))
									*/
			));
	    }

	    @WithMockUser(roles = { "ADMIN" })
		@Test
		@DisplayName("to c, authenticated")
	    void verifyGetRoleByNameAuthenticated() {

			log.info("running  " + this.getClass().getName() + ".findRealmRoleByName()");
			List<RoleVO> listRole = RoleBuilder.roleList();
			
			RoleVO roleVo = listRole.get(0);
			log.info("RoleVO {}", roleVo);
			
			given(roleService.findRealmRoleByName(anyString()))
					.willReturn(Mono.just(roleVo));
			
			String endPointURI = "/"+ roleVo.getName() +"/realm";	
			final Map<String, String> params = new HashMap<>();
			params.put("lang", "fr");
			
			Map<String, Object> templateVar = new HashMap<>();			
			/*
			String token = RandomGenerator.generateSecureRandomHexToken(16);
			Faker faker = new Faker();
			String username = faker.internet().safeEmailAddress();
			*/			
			restCallRoleResponseOne(
					endPointURI, templateVar, 
					params, roleVo)			
				.consumeWith(
		            document(
		                    "get a single realm role", 			                    
		                    preprocessRequest(prettyPrint()), 
		                    preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName("Content-Type")
						  .description("The Content-Type of the request payload, e.g. `application/json`"),
						headerWithName("api-key")
						  .description("The API Key unique to each registered user for Rate-limiting")
						//headerWithName("Authorization")
						  //.description("The Oauth2 `Bearer` token issued at login for Request Authentication")
					),
					responseHeaders(
								headerWithName("Content-Type")
								  .description("The Content-Type of the response payload, e.g. `application/json`"))
					/*
					relaxedRequestParameters( 
							parameterWithName("lang")
							.description("To instruct the resource to use a language of choice in"
									+ " the response messages. en=English, fr=French etc"))
									*/
		       ));
	    	}
			
			@WithMockUser(roles = { "ADMIN" })
			@Test
			@DisplayName("to get a single client role by name, authenticated")
		    void verifyGetClientRoleByNameAuthenticated() {

				log.info("running  " + this.getClass().getName() + ".findClientRoleByName()");
				List<RoleVO> listRole = RoleBuilder.roleList();
				
				RoleVO roleVo = listRole.get(0);
				log.info("RoleVO {}", roleVo);
				
				given(roleService.findClientRoleByName(anyString(), anyString()))
						.willReturn(Mono.just(roleVo));
				
				UUID clientId = UUID.randomUUID();				
				String endPointURI = "/"+ roleVo.getName() + "/client/" + clientId.toString();	
				
				final Map<String, String> params = new HashMap<>();
				params.put("lang", "fr");
				
				Map<String, Object> templateVar = new HashMap<>();			
				/*
				String token = RandomGenerator.generateSecureRandomHexToken(16);
				Faker faker = new Faker();
				String username = faker.internet().safeEmailAddress();
				*/			
				restCallRoleResponseOne(
						endPointURI, templateVar, 
						params, roleVo)			
					.consumeWith(
			            document(
			                    "get a single client role by name", 			                    
			                    preprocessRequest(prettyPrint()), 
			                    preprocessResponse(prettyPrint()),
						requestHeaders(
							headerWithName("Content-Type")
							  .description("The Content-Type of the request payload, e.g. `application/json`"),
							headerWithName("api-key")
							  .description("The API Key unique to each registered user for Rate-limiting")
							//headerWithName("Authorization")
							  //.description("The Oauth2 `Bearer` token issued at login for Request Authentication")
						),
						responseHeaders(
									headerWithName("Content-Type")
									  .description("The Content-Type of the response payload, e.g. `application/json`"))
						/*
						relaxedRequestParameters( 
								parameterWithName("lang")
								.description("To instruct the resource to use a language of choice in"
										+ " the response messages. en=English, fr=French etc"))
										*/
				));				
		}
		
		@WithMockUser(roles = { "ADMIN" })
		@Test
		@DisplayName("to delete a single realm role by name, authenticated")
	    void verifyDeleteRoleByNameAuthenticated() {

			when(roleService.deleteRealmRole(anyString()))
					.thenReturn(Mono.just("SUCCESS"));
			
			RoleVO roleVo = RoleBuilder.role().roleVo();
			log.info("RoleVO {}", roleVo);		      
			String endPointURI = "/"+ roleVo.getName() +"/realm";

		    webTestClient
		          .mutateWith(csrf())
		          .delete()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .exchange()
		          .expectStatus()
		          .isOk()
		          .expectBody()
		          .consumeWith(
					       document(
					          "delete a single realm role by name", 			                    
					             preprocessRequest(prettyPrint()), 
					             preprocessResponse(prettyPrint())));
		}	
		
		
		@WithMockUser(roles = { "ADMIN" })
		@Test
		@DisplayName("to delete a single client role by name, authenticated")
	    void verifyDeleteClientRoleByNameAuthenticated() {

			when(roleService.deleteClientRole(anyString(), anyString()))
					.thenReturn(Mono.just("SUCCESS"));
			
			RoleVO roleVo = RoleBuilder.role().roleVo();
			log.info("RoleVO {}", roleVo);
			
			UUID clientId = UUID.randomUUID();		      
			String endPointURI = "/"+ roleVo.getName() + "/client/" + clientId.toString();

		    webTestClient
		          .mutateWith(csrf())
		          .delete()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .exchange()
		          .expectStatus()
		          .isOk()
		          .expectBody()
		          .consumeWith(
					       document(
					          "delete a single client role by name", 			                    
					             preprocessRequest(prettyPrint()), 
					             preprocessResponse(prettyPrint())));
		}	
		
		@WithMockUser(roles = { "ADMIN" })
		@Test
	    @DisplayName("to create a new role, authenticated")
	    void verifyCreateRoleAuthenticated() throws JsonProcessingException {

	    	 UUID roleId = UUID.randomUUID();
	    	 String endPointURI = "";
	    	 
	    	 RoleVO expectedRole = RoleBuilder.role().roleVo();	    	 	
		     CreateRoleRequest roleResource =  RoleBuilder.role().withId(roleId).build();	          
	
		     log.info("Role Resource {}", roleResource); 
		     
		     when(roleService.createRealmRole(any())).thenReturn(Mono.just(expectedRole));
	
		     webTestClient
		          .mutateWith(csrf())
		          .post()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .contentType(MediaType.APPLICATION_JSON)
		          .body(Mono.just(roleResource), CreateRoleRequest.class)
		          .exchange()
		          .expectStatus()
		          .isCreated()
		          .expectBody()
		          .consumeWith(
					       document(
					          "create a new role", 			                    
					             preprocessRequest(prettyPrint()), 
					             preprocessResponse(prettyPrint())));
	    }
		
		@WithMockUser(roles = { "ADMIN" })
		@Test
	    @DisplayName("to create a new client role, authenticated")
	    void verifyCreateClientRoleAuthenticated() throws JsonProcessingException {			
	    	 
	    	 RoleVO expectedRole = RoleBuilder.role().roleVo();	  
	    	 
	    	 UUID roleId = UUID.randomUUID();
	    	 UUID clientId = UUID.randomUUID();	
	    	 
			 String endPointURI = "/client/" + clientId.toString();
				
		     CreateRoleRequest roleResource =  RoleBuilder.role().withId(roleId).build();	
		     log.info("Role Resource {}", roleResource); 
		     
		     when(roleService.createRealmRole(any())).thenReturn(Mono.just(expectedRole));
	
		     webTestClient
		          .mutateWith(csrf())
		          .post()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .contentType(MediaType.APPLICATION_JSON)
		          .body(Mono.just(roleResource), CreateRoleRequest.class)
		          .exchange()
		          .expectStatus()
		          .isCreated()
		          .expectBody()
		          .consumeWith(
					       document(
					          "create a new client role", 			                    
					             preprocessRequest(prettyPrint()), 
					             preprocessResponse(prettyPrint())));
	    }
		
		@WithMockUser(roles = { "ADMIN" })
		@Test
	    @DisplayName("to make realm role composite, authenticated")
	    void verifyRealmRoleCompositeAuthenticated() throws JsonProcessingException {	    	 
	    	 
			 UUID roleId = UUID.randomUUID();
	    	
	    	 RoleVO expectedRole = RoleBuilder.role().roleVo();	    	 	
		     CreateRoleRequest roleResource =  RoleBuilder.role().withId(roleId).build();	
		     
		     String endPointURI = "/" + expectedRole.getName() + "/realm";	
		     log.info("Role Resource {}", roleResource); 
		     
		     when(roleService.makeRealmRoleComposite(anyString(), any(CreateRoleRequest.class)))
		     			.thenReturn(Mono.just("SUCCESS"));
	
		     webTestClient
		          .mutateWith(csrf())
		          .post()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .contentType(MediaType.APPLICATION_JSON)
		          .body(Mono.just(roleResource), CreateRoleRequest.class)
		          .exchange()
		          .expectStatus()
		          .isOk()
		          .expectBody()
		          .consumeWith(
					       document(
					          "make realm role composite", 			                    
					             preprocessRequest(prettyPrint()), 
					             preprocessResponse(prettyPrint())));
	    }
		
		@WithMockUser(roles = { "ADMIN" })
		@Test
	    @DisplayName("to make client role composite, authenticated")
	    void verifyClientRoleCompositeAuthenticated() throws JsonProcessingException {	    	 
	    	 
			 UUID roleId = UUID.randomUUID();
			 UUID clientId = UUID.randomUUID();	
	    	
	    	 RoleVO expectedRole = RoleBuilder.role().roleVo();	    	 	
		     CreateRoleRequest roleResource =  RoleBuilder.role().withId(roleId).build();	
		     
		     String endPointURI = "/" + expectedRole.getName() + "/realm/client/"+ clientId.toString();;
		     log.info("Role Resource {}", roleResource); 
		     
		     when(roleService.makeRealmRoleCompositeWithClientRole(anyString(), any(CreateRoleRequest.class), anyString()))
		     			.thenReturn(Mono.just("SUCCESS"));
	
		     webTestClient
		          .mutateWith(csrf())
		          .post()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .contentType(MediaType.APPLICATION_JSON)
		          .body(Mono.just(roleResource), CreateRoleRequest.class)
		          .exchange()
		          .expectStatus()
		          .isOk()
		          .expectBody()
		          .consumeWith(
					       document(
					          "make client role composite", 			                    
					             preprocessRequest(prettyPrint()), 
					             preprocessResponse(prettyPrint())));
	    }
		
		@WithMockUser(roles = { "ADMIN" })
		@Test
	    @DisplayName("to make client role composite with client role, authenticated")
	    void verifyClientRoleCompositeWithClientRoleAuthenticated() throws JsonProcessingException {	    	 
	    	 
			 UUID roleId = UUID.randomUUID();
			 UUID clientId = UUID.randomUUID();	
	    	
	    	 RoleVO expectedRole = RoleBuilder.role().roleVo();	    	 	
		     CreateRoleRequest roleResource =  RoleBuilder.role().withId(roleId).build();	
		     
		     String endPointURI = "/client/"+ clientId.toString() + "/role_name/" + expectedRole.getName();
		     log.info("Role Resource {}", roleResource); 
		     
		     when(roleService.makeClientRoleComposite(any(CreateRoleRequest.class), anyString(), anyString()))
		     			.thenReturn(Mono.just("SUCCESS"));
	
		     webTestClient
		          .mutateWith(csrf())
		          .post()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .contentType(MediaType.APPLICATION_JSON)
		          .body(Mono.just(roleResource), CreateRoleRequest.class)
		          .exchange()
		          .expectStatus()
		          .isOk()
		          .expectBody()
		          .consumeWith(
					       document(
					          "make client role composite with client role", 			                    
					             preprocessRequest(prettyPrint()), 
					             preprocessResponse(prettyPrint())));
	    }
		
	 }

	  @DisplayName("as unauthenticated user is denied with 401")
	  @Nested
	  class UnAuthenticatedRoleApi {

		@Test
	    @DisplayName("to get all realm roles, unauthenticated")
	    void verifyGetRealmRolesUnAuthenticated() {

	    	  String endPointURI = "";	
		      webTestClient
		          .get()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .exchange()
		          .expectStatus()
		          .isUnauthorized();
	    }
	    
		@Test
	    @DisplayName("to get all client roles, unauthenticated")
	    void verifyGetClientRolesUnAuthenticated() {

			UUID clientId = UUID.randomUUID();
			String endPointURI = "/client/" + clientId.toString();
			
		    webTestClient
		          .get()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .exchange()
		          .expectStatus()
		          .isUnauthorized();
	    }

		@Test
	    @DisplayName("to get a single role by name, unauthenticated")
	    void verifyGetRealmRoleUnAuthenticated() {
			
			List<RoleVO> listRole = RoleBuilder.roleList();
			RoleVO roleVo = listRole.get(0);
			log.info("RoleVO {}", roleVo);
			
		     //UUID bookId = UUID.randomUUID();
		     String endPointURI = "/"+ roleVo.getName() +"/realm";
	
		      webTestClient
		          .get()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .exchange()
		          .expectStatus()
		          .isUnauthorized();
	    }
	    
	    @Test
	    @DisplayName("to get a single client role by name, unauthenticated")
	    void verifyGetClientRoleUnAuthenticated() {
	    	
	    	List<RoleVO> listRole = RoleBuilder.roleList();
	    	RoleVO roleVo = listRole.get(0);
			log.info("RoleVO {}", roleVo);
			
		     UUID clientId = UUID.randomUUID();				
			 String endPointURI = "/"+ roleVo.getName() + "/client/" + clientId.toString();
	
		      webTestClient
		          .get()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .exchange()
		          .expectStatus()
		          .isUnauthorized();
	    }

	    @Test
	    @DisplayName("to delete a realm role, unauthenticated")
	    void verifyDeleteRoleUnAuthenticated() {

	    	RoleVO roleVo = RoleBuilder.role().roleVo();
			log.info("RoleVO {}", roleVo);		      
			String endPointURI = "/"+ roleVo.getName() +"/realm";

		    webTestClient
		          .mutateWith(csrf())
		          .delete()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .exchange()
		          .expectStatus()
		          .isUnauthorized();
	    }

	    @Test
	    @DisplayName("to delete a client role, unauthenticated")
	    void verifyDeleteClientRoleUnAuthenticated() {

	    	RoleVO roleVo = RoleBuilder.role().roleVo();
			log.info("RoleVO {}", roleVo);
			
			UUID clientId = UUID.randomUUID();		      
			String endPointURI = "/"+ roleVo.getName() + "/client/" + clientId.toString();

		    webTestClient
		          .mutateWith(csrf())
		          .delete()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .exchange()
		          .expectStatus()
		          .isUnauthorized();
	    }

	    @Test
	    @DisplayName("to make client role composite with client role")
	    void verifyMakeClientRoleCompositeUnAuthenticated() {

	       UUID roleId = UUID.randomUUID();
		   UUID clientId = UUID.randomUUID();	
	    	
	       RoleVO expectedRole = RoleBuilder.role().roleVo();	    	 	
		   CreateRoleRequest roleResource =  RoleBuilder.role().withId(roleId).build();	
		     
		  String endPointURI = "/client/"+ clientId.toString() + "/role_name/" + expectedRole.getName();
		  log.info("Role Resource {}", roleResource); 

	      webTestClient
	          .mutateWith(csrf())
	          .post()
	          .uri(PATH + endPointURI)
	          .accept(MediaType.APPLICATION_JSON)
	          .exchange()
	          .expectStatus()
	          .isUnauthorized();
	    }
	    
	    @Test
	    @DisplayName("to create a new realm role")
	    void verifyCreateRealmRoleUnAuthenticated() throws JsonProcessingException {

	      //UUID bookId = UUID.randomUUID();
		  String endPointURI = "";
		      
	      RoleVO expectedRole = RoleBuilder.role().roleVo();

	      when(roleService.createRealmRole(any(CreateRoleRequest.class)))
	      			.thenReturn(Mono.just(expectedRole));

	      webTestClient
	          .mutateWith(csrf())
	          .post()
	          .uri(PATH + endPointURI)
	          .accept(MediaType.APPLICATION_JSON)
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(BodyInserters.fromValue(new ObjectMapper().writeValueAsString(expectedRole)))
	          .exchange()
	          .expectStatus()
	          .isUnauthorized();
	    }

	    @Test
	    @DisplayName("to create a new client role")
	    void verifyCreateClientRoleUnAuthenticated() throws JsonProcessingException {

	        UUID clientId = UUID.randomUUID();	
	    	String endPointURI = "/client/" + clientId.toString();
	      
	        RoleVO expectedRole = RoleBuilder.role().roleVo();

	        when(roleService.createClientRole(any(CreateRoleRequest.class), anyString()))
	      				.thenReturn(Mono.just(expectedRole));

		      webTestClient
		          .mutateWith(csrf())
		          .post()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .contentType(MediaType.APPLICATION_JSON)
		          .body(BodyInserters.fromValue(new ObjectMapper().writeValueAsString(expectedRole)))
		          .exchange()
		          .expectStatus()
		          .isUnauthorized();
	    }
	}
	  
	  protected BodyContentSpec restCallRoleResponseOne(
			 String endPointURI, Map<String, Object> templateVar,
			 Map<String, String> params, RoleVO roleVO) {
			  
		  return this.webTestClient
						.mutateWith(csrf())
						.get()
						.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
						.queryParams(buildParameters(params))
						.build(templateVar))
						.header("api-key", apiKey)
						.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
						.accept(MediaType.APPLICATION_JSON)
						.exchange()
						.expectStatus()
						.isOk()
						.expectBody()
						.jsonPath("$.id")
						.isEqualTo(roleVO.getId())
						.jsonPath("$.name")
						.isEqualTo(roleVO.getName())
						.jsonPath("$.description")
						.isEqualTo(roleVO.getDescription())
						.jsonPath("$.attributes")
						.isEmpty();
		}

		protected BodyContentSpec restCallRoleResponseMultiple(
				String endPointURI,	Map<String, Object> templateVar, 
				Map<String, String> params, RoleVO... roleVO) {

			return this.webTestClient
						.mutateWith(csrf())
						.get()
						.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
						.queryParams(buildParameters(params))
						.build(templateVar))
						.header("api-key", apiKey)
						.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
						.accept(MediaType.APPLICATION_JSON)
						.exchange()
						.expectStatus()
						.isOk()
						.expectBody()
						.jsonPath("$.[0].id")
						.isEqualTo(roleVO[0].getId())
						.jsonPath("$.[0].name")
						.isEqualTo(roleVO[0].getName())
						.jsonPath("$.[0].description")
						.isEqualTo(roleVO[0].getDescription())
						.jsonPath("$.[0].attributes")
						.isEmpty()					
						.jsonPath("$.[1].id")
						.isEqualTo(roleVO[1].getId())
						.jsonPath("$.[1].name")
						.isEqualTo(roleVO[1].getName())
						.jsonPath("$.[1].description")
						.isEqualTo(roleVO[1].getDescription())
						.jsonPath("$.[1].attributes")
						.isEmpty();
		}
		
		protected MultiValueMap<String, String> buildParameters(Map<String, String> params) {

			MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
			parameters.setAll(params);

			return parameters;
		}	
}
