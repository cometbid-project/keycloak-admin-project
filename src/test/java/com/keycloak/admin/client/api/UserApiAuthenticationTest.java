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
import com.keycloak.admin.client.controllers.UserController;
import com.keycloak.admin.client.dataacess.RoleBuilder;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.models.PagingModel;
import com.keycloak.admin.client.models.RoleVO;
import com.keycloak.admin.client.models.SearchUserRequest;
import com.keycloak.admin.client.models.UserRegistrationRequest;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.oauth.service.it.UserCredentialFinderService;
import com.keycloak.admin.client.oauth.service.it.UserCredentialService;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import lombok.extern.log4j.Log4j2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
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
import static org.mockito.BDDMockito.given;
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
@WebFluxTest(controllers = UserController.class) // <1>
@DisplayName("User Profile endpoints API")
@Import({ AuthProperties.class, ResponseCreator.class, UserController.class })
@ExtendWith({ RestDocumentationExtension.class, SpringExtension.class })
@ContextConfiguration(classes = { SecurityConfig.class, KeycloakConfiguration.class })
class UserApiAuthenticationTest {

	@Autowired 
	private ApplicationContext applicationContext;

	@Autowired
	private WebTestClient webTestClient;

    @MockBean 
    private UserCredentialService userService;
    @MockBean
    UserCredentialFinderService userFinderService;
    
    protected String PATH = "/api/v1/users";
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

	  /**
	   * 
	   * @author Gbenga
	   *
	   */
	  @DisplayName("as authenticated user is granted")
	  @Nested
	  class AuthenticatedUserApi {

			 /**
			  * @throws JsonProcessingException 
			  * 
			  */
		    @WithMockUser(roles = { "ADMIN" })
			@SuppressWarnings("unchecked")
			@Test
		    @DisplayName("to search for list of users")
		    void verifyGetUsersAuthenticated() throws JsonProcessingException { 
	
	    		List<UserVO> listUsers = UserBuilder.userList();
	    	    			
				given(userFinderService.search(any(SearchUserRequest.class), any(PagingModel.class)))
						.willReturn(Flux.fromIterable(listUsers));
	
				String endPointURI = "/search";	
				
				SearchUserRequest searchRequest = SearchUserRequest.builder()
						.email(listUsers.get(0).getEmail()).firstName(listUsers.get(0).getFirstName())
						.lastName(listUsers.get(0).getLastName())
						.emailVerified(listUsers.get(0).isEmailVerified())
						.build();
				
				final Map<String, String> params = new HashMap<>();
				params.put("page", "1");
				params.put("size", "10");
				params.put("lang", "fr");
				
				Map<String, Object> templateVar = new HashMap<>();	
				
				UserVO[] userVO = listUsers.toArray(UserVO[]::new); 
				
				webTestClient
		          .mutateWith(csrf())
		          .post()
		          .uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
							.queryParams(buildParameters(params))
							.build(templateVar))
		          .header("api-key", apiKey)
		          .accept(MediaType.APPLICATION_JSON)
		          .contentType(MediaType.APPLICATION_JSON)		          
		          .body(BodyInserters.fromValue(new ObjectMapper()
		        		  .writeValueAsString(searchRequest)))
		          .exchange()
		          .expectStatus()
		          .isOk()
				  .expectBody()
				  .jsonPath("$.[0].id")
				  .isEqualTo(userVO[0].getId())
					.jsonPath("$.[0].first_name")
					.isEqualTo(userVO[0].getFirstName())
					.jsonPath("$.[0].last_name")
					.isEqualTo(userVO[0].getLastName())
					.jsonPath("$.[0].email")
					.isEqualTo(userVO[0].getEmail())
					.jsonPath("$.[0].roles")
					.isEqualTo(userVO[0].getRoles())					
					.jsonPath("$.[1].id")
					.isEqualTo(userVO[1].getId())
					.jsonPath("$.[1].first_name")
					.isEqualTo(userVO[1].getFirstName())
					.jsonPath("$.[1].last_name")
					.isEqualTo(userVO[1].getLastName())
					.jsonPath("$.[1].email")
					.isEqualTo(userVO[1].getEmail())
					.jsonPath("$.[1].roles")
					.isEqualTo(userVO[0].getRoles())	
					.consumeWith(
			            document(
			                    "headers-example", 			                    
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
	
	    /**
	     * 
	     */
	    @WithMockUser(roles = { "ADMIN" })
	    @Test
	    @DisplayName("to get a single user by id")
	    void verifyGetUserByIdAuthenticated() {
	
	      UUID userId = UUID.randomUUID();
	      UserVO expectedUser = UserBuilder.user().userVo(userId);
	      
	      String endPointURI = "/id/"+ userId;	
	      	
	      given(userFinderService.findUserById(userId.toString()))
	          .willReturn(Mono.just(expectedUser));
	      
	      Map<String, Object> templateVar = new HashMap<>();
	      Map<String, String> params = new HashMap<>();
	      params.put("lang", "fr");
	
	        /*
			String token = RandomGenerator.generateSecureRandomHexToken(16);
			Faker faker = new Faker();
			String username = faker.internet().safeEmailAddress();
			*/			
			restCallUserResponseOne(
					endPointURI, templateVar, 
					params, expectedUser)			
				.consumeWith(
		            document(
		                    "headers-example", 			                    
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
	    
	    /**
	     * 
	     */
	    @WithMockUser(roles = { "ADMIN" })
	    @Test
	    @DisplayName("to get a single user by email")
	    void verifyGetUserByEmailAuthenticated() {
	
	      List<UserVO> listUsers = UserBuilder.userList();
	      UserVO expectedUser = listUsers.get(0);
	      
	      String endPointURI = "/email/"+ expectedUser.getEmail();	
	      	
	      given(userFinderService.findUserByEmail(anyString(), any(PagingModel.class)))
	          .willReturn(Flux.fromIterable(listUsers));
	      
	      Map<String, Object> templateVar = new HashMap<>();
	      Map<String, String> params = new HashMap<>();
	      params.put("page", "1");
		  params.put("size", "10");
		  params.put("lang", "fr");
	
		  restCallUserResponseMultiple(endPointURI,
					 templateVar, params, listUsers.toArray(UserVO[]::new))					
				.consumeWith(
		            document(
		                    "headers-example", 			                    
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
	    
	    /**
	     * 
	     */
	    @WithMockUser(roles = { "ADMIN" })
	    @Test
	    @DisplayName("to get a single user by username")
	    void verifyGetUserByUsernameAuthenticated() {
	
	      UUID userId = UUID.randomUUID();
	      UserVO expectedUser = UserBuilder.user().userVo(userId);
	      
	      String endPointURI = "/username/"+ expectedUser.getUsername();
	      	
	      given(userFinderService.findByUsername(expectedUser.getUsername()))
	          .willReturn(Mono.just(expectedUser));
	      
	      Map<String, Object> templateVar = new HashMap<>();
	      Map<String, String> params = new HashMap<>();
	      params.put("lang", "fr");
		
		  restCallUserResponseOne(
					endPointURI, templateVar, 
					params, expectedUser)			
				.consumeWith(
		            document(
		                    "headers-example", 			                    
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
		@SuppressWarnings("unchecked")
		@DisplayName("to get all users, authenticated")
		@Test
		void verifyGetAllUsersAuthenticated() {
			
			List<UserVO> listUsers = UserBuilder.userList();
			
			given(userFinderService.findAll(any(PagingModel.class)))
					.willReturn(Flux.fromIterable(listUsers));
			
			String endPointURI = "";
			
			final Map<String, String> params = new HashMap<>();
			params.put("page", "1");
			params.put("size", "10");
			params.put("lang", "fr");
			
			Map<String, Object> templateVar = new HashMap<>();			
			//UserVO[] userVO = listUsers.toArray(UserVO[]::new); 
				
			restCallUserResponseMultiple(endPointURI,
					 templateVar, params, listUsers.toArray(UserVO[]::new))			
					.consumeWith(
			            document(
			                    "headers-example", 			                    
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
	
	    /**
	     * 
	     * @throws JsonProcessingException
	     */
	    @Test
	    @WithMockUser(roles = { "ADMIN" })
	    @DisplayName("to create a new user")
	    void verifyCreateUserAuthenticated() throws JsonProcessingException {
	
	    	UUID userId = UUID.randomUUID();
	    	String endPointURI = "";
	    	
		    UserVO expectedUser = UserBuilder.user().userVo(userId);
		
		    UserRegistrationRequest userResource = UserBuilder.user().build();
		
		    when(userService.signupUser(any(UserRegistrationRequest.class),	any(Role.class),
		    		any(ServerHttpRequest.class)))
		    		.thenReturn(Mono.just(expectedUser));
	
		     webTestClient
		          //.mutateWith(mockUser().roles("LIBRARY_ADMIN"))
		          .mutateWith(csrf())
		          .post()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .contentType(MediaType.APPLICATION_JSON)
		          .body(BodyInserters.fromValue(new ObjectMapper()
		        		  .writeValueAsString(userResource)))
		          .exchange()
		          .expectStatus()
		          .isCreated()
		          .expectBody()
		          .consumeWith(
				       document(
				          "create a new user", 			                    
				             preprocessRequest(prettyPrint()), 
				             preprocessResponse(prettyPrint())));
	    }
	    
	    /**
	     * 
	     * @throws JsonProcessingException
	     */
	    @Test
	    @WithMockUser(roles = { "ADMIN" })
	    @DisplayName("to assign user to client role, authenticated")
	    void verifyAssignUserToClientRoleAuthenticated() throws JsonProcessingException {
	
	    	UUID userId = UUID.randomUUID();
	    	UUID clientId = UUID.randomUUID();
	    	RoleVO expectedRole = RoleBuilder.role().roleVo();

	    	String endPointURI = "/" + userId + "/role/" + expectedRole.getName() + "/client/" + clientId ;
	    			
		   // UserRegistrationRequest userResource = UserBuilder.user().build();
		
		    when(userService.assignClientRoleToUser(anyString(), anyString(), anyString()))
		    		.thenReturn(Mono.just("SUCCESS"));
	
		    webTestClient
		          //.mutateWith(mockUser().roles("LIBRARY_ADMIN"))
		          .mutateWith(csrf())
		          .put()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .contentType(MediaType.APPLICATION_JSON)
		           // .body(BodyInserters.fromValue(new ObjectMapper()
		        		//  .writeValueAsString(userResource)))
		          .exchange()
		          .expectStatus()
		          .isOk()
		          .expectBody()
		          .consumeWith(
				       document(
				          "assign user to client role", 			                    
				             preprocessRequest(prettyPrint()), 
				             preprocessResponse(prettyPrint())));
	    }
	    
	    /**
	     * 
	     * @throws JsonProcessingException
	     */
	    @Test
	    @WithMockUser(roles = { "ADMIN" })
	    @DisplayName("to assign user to group, authenticated")
	    void verifyAssignUserToGroupAuthenticated() throws JsonProcessingException {
	
	    	UUID userId = UUID.randomUUID();
	    	UUID groupId = UUID.randomUUID();
	    	//RoleVO expectedRole = RoleBuilder.role().roleVo();

	    	String endPointURI = "/" + userId + "/group/" + groupId;
	    			
		   //UserRegistrationRequest userResource = UserBuilder.user().build();
		
		    when(userService.assignToGroup(anyString(), anyString()))
		    		.thenReturn(Mono.just("SUCCESS"));
	
		    webTestClient
		          //.mutateWith(mockUser().roles("LIBRARY_ADMIN"))
		          .mutateWith(csrf())
		          .put()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .contentType(MediaType.APPLICATION_JSON)
		          //.body(BodyInserters.fromValue(new ObjectMapper()
		        	//	  .writeValueAsString(userResource)))
		          .exchange()
		          .expectStatus()
		          .isOk()
		          .expectBody()
		          .consumeWith(
				       document(
				          "assign user to group", 			                    
				             preprocessRequest(prettyPrint()), 
				             preprocessResponse(prettyPrint())));
	    }
	    
	    /**
	     * 
	     * @throws JsonProcessingException
	     */
	    @Test
	    @WithMockUser(roles = { "ADMIN" })
	    @DisplayName("to assign user to realm role, authenticated")
	    void verifyAssignUserToRealmRoleAuthenticated() throws JsonProcessingException {
	
	    	UUID userId = UUID.randomUUID();
	    	RoleVO expectedRole = RoleBuilder.role().roleVo();
	    	String endPointURI = "/" + userId + "/role/" + expectedRole.getName();
	    			
		   // UserRegistrationRequest userResource = UserBuilder.user().build();
		
		    when(userService.assignRealmRole(anyString(), anyString()))
		    		.thenReturn(Mono.just("SUCCESS"));
	
		    webTestClient
		          //.mutateWith(mockUser().roles("LIBRARY_ADMIN"))
		          .mutateWith(csrf())
		          .put()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .contentType(MediaType.APPLICATION_JSON)
		          //.body(BodyInserters.fromValue(new ObjectMapper()
		        		//  .writeValueAsString(userResource)))
		          .exchange()
		          .expectStatus()
		          .isOk()
		          .expectBody()
		          .consumeWith(
				       document(
				          "assign user to realm role", 			                    
				             preprocessRequest(prettyPrint()), 
				             preprocessResponse(prettyPrint())));
	    }
	    
	    /**
	     * 
	     */
	    @Test
	    @WithMockUser(roles = { "ADMIN" })
	    @DisplayName("to delete a user")
	    void verifyDeleteUserAuthenticated() {
	
	      UUID userId = UUID.randomUUID();
	      
	      String endPointURI = "/"+ userId;
	      given(userService.deleteUser(userId.toString())).willReturn(Mono.just("SUCCESS"));
	
	      webTestClient
	          //.mutateWith(mockUser().roles("LIBRARY_ADMIN"))
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
			          "delete a user", 			                    
			             preprocessRequest(prettyPrint()), 
			             preprocessResponse(prettyPrint())));
	    }
	    
  	}

	  @DisplayName("as unauthenticated user is denied")
	  @Nested
	  class UnAuthenticatedUserApi {
	
			  /**
			   * 
			   */
		    @Test
		    @DisplayName("to search for a list of users")
		    void verifySearchUsersUnAuthenticated() {
		
		    	 String endPointURI = "/search";	
		    	
			     webTestClient
			          .get()
			          .uri(PATH + endPointURI)
			          .accept(MediaType.APPLICATION_JSON)
			          .exchange()
			          .expectStatus()
			          .isUnauthorized();
		    }
		    
		    /**
			 * 
			 */
		    @Test
		    @DisplayName("to get list of users")
		    void verifyGetUsersUnAuthenticated() {
		
		    	String endPointURI = "";
				
				final Map<String, String> params = new HashMap<>();
				params.put("page", "1");
				params.put("size", "10");
				params.put("lang", "fr");	
		    	
			     webTestClient
			          .get()
			          .uri(PATH + endPointURI)
			          .accept(MediaType.APPLICATION_JSON)
			          .exchange()
			          .expectStatus()
			          .isUnauthorized();
		    }
		
		    /**
		     * 
		     */
		    @Test
		    @DisplayName("to get single user by id")
		    void verifyGetUserUnAuthenticated() {
		
		      UUID userId = UUID.randomUUID();
		      String endPointURI = "/id/"+ userId;	
		
		      webTestClient
		          .get()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .exchange()
		          .expectStatus()
		          .isUnauthorized();
		    }
		    
		    /**
		     * 
		     */
		    @Test
		    @DisplayName("to get single user by username")
		    void verifyGetUserByUsernameUnAuthenticated() {
		
		    	 UUID userId = UUID.randomUUID();
			     UserVO expectedUser = UserBuilder.user().userVo(userId);
			      
			     String endPointURI = "/username/"+ expectedUser.getUsername();
		
			      webTestClient
			          .get()
			          .uri(PATH + endPointURI)
			          .accept(MediaType.APPLICATION_JSON)
			          .exchange()
			          .expectStatus()
			          .isUnauthorized();
		    }
		    
		    /**
		     * 
		     */
		    @Test
		    @DisplayName("to get single user by email")
		    void verifyGetUserByEmailUnAuthenticated() {
		
		    	UserVO expectedUser = UserBuilder.user().userVo();
			      
			    String endPointURI = "/email/"+ expectedUser.getEmail();	
		
		       webTestClient
		          .get()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .exchange()
		          .expectStatus()
		          .isUnauthorized();
		    }
		
		    /**
		     * 
		     */
		    @Test
		    @DisplayName("to delete a user")
		    void verifyDeleteUserUnAuthenticated() {
		
		    	  UUID userId = UUID.randomUUID();
			      
			      String endPointURI = "/"+ userId;
		
			      webTestClient
			          .mutateWith(csrf())
			          .delete()
			          .uri(PATH + endPointURI)
			          .accept(MediaType.APPLICATION_JSON)
			          .exchange()
			          .expectStatus()
			          .isUnauthorized();
		    }
		
		    /**
		     * 
		     * @throws JsonProcessingException
		     */
		    @Test
		    @DisplayName("to create a new user")
		    void verifyCreateUserUnAuthenticated() throws JsonProcessingException {
		
		    	UUID userId = UUID.randomUUID();
		    	String endPointURI = "";
		    	
			    UserVO expectedUser = UserBuilder.user().userVo(userId);
			
			    UserRegistrationRequest userResource = UserBuilder.user().build();
			
			    given(userService.signupUser(any(UserRegistrationRequest.class), 
			    		any(Role.class), any(ServerHttpRequest.class)))
			    		.willAnswer(i -> Mono.just(expectedUser));
		
		        webTestClient
		          .mutateWith(csrf())
		          .post()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .contentType(MediaType.APPLICATION_JSON)
		          .body(BodyInserters.fromValue(new ObjectMapper()
		        		  .writeValueAsString(userResource)))
		          .exchange()
		          .expectStatus()
		          .isCreated();
		    }
		    
		    /**
		     * 
		     * @throws JsonProcessingException
		     */
		    @Test
		    @DisplayName("to assign user to client role")
		    void verifyAssignUserToClientRole() throws JsonProcessingException {
		
		    	UUID userId = UUID.randomUUID();
		    	UUID clientId = UUID.randomUUID();
		    	RoleVO expectedRole = RoleBuilder.role().roleVo();

		    	String endPointURI = "/" + userId + "/role/" + expectedRole.getName() + "/client/" + clientId ;
		    						
			    when(userService.assignClientRoleToUser(anyString(), anyString(), anyString()))
			    		.thenReturn(Mono.just("SUCCESS"));
		
			    webTestClient
			          //.mutateWith(mockUser().roles("LIBRARY_ADMIN"))
			          .mutateWith(csrf())
			          .put()
			          .uri(PATH + endPointURI)
			          .accept(MediaType.APPLICATION_JSON)
			          .contentType(MediaType.APPLICATION_JSON)
			           // .body(BodyInserters.fromValue(new ObjectMapper()
			        		//  .writeValueAsString(userResource)))
			          .exchange()
			          .expectStatus()
			          .isUnauthorized();
		    }
		    
		    /**
		     * 
		     * @throws JsonProcessingException
		     */
		    @Test
		    @DisplayName("to assign user to group")
		    void verifyAssignUserToGroup() throws JsonProcessingException {
		
		    	UUID userId = UUID.randomUUID();
		    	UUID groupId = UUID.randomUUID();

		    	String endPointURI = "/" + userId + "/group/" + groupId;
		    						
			    when(userService.assignToGroup(anyString(), anyString()))
			    		.thenReturn(Mono.just("SUCCESS"));
		
			    webTestClient
			          //.mutateWith(mockUser().roles("LIBRARY_ADMIN"))
			          .mutateWith(csrf())
			          .put()
			          .uri(PATH + endPointURI)
			          .accept(MediaType.APPLICATION_JSON)
			          .contentType(MediaType.APPLICATION_JSON)
			          //.body(BodyInserters.fromValue(new ObjectMapper()
			        	//	  .writeValueAsString(userResource)))
			          .exchange()
			          .expectStatus()
			          .isUnauthorized();
		    }
		    
		    /**
		     * 
		     * @throws JsonProcessingException
		     */
		    @Test
		    @DisplayName("to assign user to realm role")
		    void verifyAssignUserToRealmRole() throws JsonProcessingException {
		
		    	UUID userId = UUID.randomUUID();
		    	RoleVO expectedRole = RoleBuilder.role().roleVo();
		    	String endPointURI = "/" + userId + "/role/" + expectedRole.getName();
		    						
			    when(userService.assignRealmRole(anyString(), anyString()))
			    		.thenReturn(Mono.just("SUCCESS"));
		
			    webTestClient
			          //.mutateWith(mockUser().roles("LIBRARY_ADMIN"))
			          .mutateWith(csrf())
			          .put()
			          .uri(PATH + endPointURI)
			          .accept(MediaType.APPLICATION_JSON)
			          .contentType(MediaType.APPLICATION_JSON)
			          //.body(BodyInserters.fromValue(new ObjectMapper()
			        		//  .writeValueAsString(userResource)))
			          .exchange()
			          .expectStatus()
			          .isUnauthorized();
		    }
	   }
	  
  		protected BodyContentSpec restCallUserResponseOne(
			 String endPointURI, Map<String, Object> templateVar,
			 Map<String, String> params, UserVO userVO) {
			  
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
						.isEqualTo(userVO.getId())
						.jsonPath("$.first_name")
						.isEqualTo(userVO.getFirstName())
						.jsonPath("$.last_name")
						.isEqualTo(userVO.getLastName())
						.jsonPath("$.email")
						.isEqualTo(userVO.getEmail())
						.jsonPath("$.roles")
						.isEqualTo(userVO.getRoles());
		}

		protected BodyContentSpec restCallUserResponseMultiple(
				String endPointURI,	Map<String, Object> templateVar, 
				Map<String, String> params, UserVO... userVO) {

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
						.isEqualTo(userVO[0].getId())
						.jsonPath("$.[0].first_name")
						.isEqualTo(userVO[0].getFirstName())
						.jsonPath("$.[0].last_name")
						.isEqualTo(userVO[0].getLastName())
						.jsonPath("$.[0].email")
						.isEqualTo(userVO[0].getEmail())
						.jsonPath("$.[0].roles")
						.isEqualTo(userVO[0].getRoles())					
						.jsonPath("$.[1].id")
						.isEqualTo(userVO[1].getId())
						.jsonPath("$.[1].first_name")
						.isEqualTo(userVO[1].getFirstName())
						.jsonPath("$.[1].last_name")
						.isEqualTo(userVO[1].getLastName())
						.jsonPath("$.[1].email")
						.isEqualTo(userVO[1].getEmail())
						.jsonPath("$.[1].roles")
						.isEqualTo(userVO[0].getRoles());
		}
		
		protected MultiValueMap<String, String> buildParameters(Map<String, String> params) {

			MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
			parameters.setAll(params);

			return parameters;
		}	
}
