/**
 * 
 */
package com.keycloak.admin.client.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keycloak.admin.client.common.enums.StatusType;
import com.keycloak.admin.client.common.utils.ResponseCreator;
import com.keycloak.admin.client.components.AuthenticatedUserMgr;
import com.keycloak.admin.client.config.AuthProperties;
import com.keycloak.admin.client.config.SecurityConfig;
import com.keycloak.admin.client.router.config.UserAuthRouterConfig;
import com.keycloak.admin.client.config.keycloak.KeycloakConfiguration;
import com.keycloak.admin.client.dataacess.AuthBuilder;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.handlers.UserAuthenticationHandler;
import com.keycloak.admin.client.models.AuthenticationRequest;
import com.keycloak.admin.client.models.AuthenticationResponse;
import com.keycloak.admin.client.models.EmailStatusUpdateRequest;
import com.keycloak.admin.client.models.StatusUpdateRequest;
import com.keycloak.admin.client.models.UserDetailsUpdateRequest;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.oauth.service.it.UserAuthenticationService;
import com.keycloak.admin.client.oauth.service.it.UserCredentialFinderService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import lombok.extern.log4j.Log4j2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

/**
 * @author Gbenga
 *
 */
@Log4j2
@WebFluxTest(controllers = UserAuthenticationHandler.class) // <1>
@DisplayName("User Authentication endpoints API")
@Import({ AuthProperties.class, ResponseCreator.class, AuthenticatedUserMgr.class,
		  UserAuthenticationHandler.class })
@ExtendWith({ RestDocumentationExtension.class, SpringExtension.class })
@ContextConfiguration(classes = { SecurityConfig.class, KeycloakConfiguration.class, UserAuthRouterConfig.class })
class UserAuthHandlerTest {

	@Autowired 
	private ApplicationContext applicationContext;

	@Autowired
	private WebTestClient webTestClient;
	
	@MockBean
	private UserAuthenticationService userAuthenticationService;
	@MockBean
	private UserCredentialFinderService profileFinderService;
	//@MockBean
	//private AuthenticatedUserMgr userAuthMgr;
	    
    protected String PATH = "/api/v1";
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
			@SuppressWarnings("unchecked")
			@Test
		    @DisplayName("to signin user")
		    void verifySigninAuthenticated() throws JsonProcessingException { 
				
				UUID userId = UUID.randomUUID();		    	
			    UserVO expectedUser = UserBuilder.user().userVo(userId);
			    
				AuthenticationResponse authResponse = AuthBuilder.auth(expectedUser).authResponse();				
				AuthenticationRequest authRequest = AuthBuilder.auth(expectedUser).build();
		    	
				log.info("Authentication Response {}", authResponse);
				
				given(userAuthenticationService.authenticate(any(AuthenticationRequest.class), 
										any(ServerHttpRequest.class)))
						.willReturn(Mono.just(authResponse)); 
	
				String endPointURI = "/signin";	
				
				final Map<String, String> params = new HashMap<>();
				params.put("page", "1");
				params.put("size", "10");
				params.put("lang", "fr");
				
				Map<String, Object> templateVar = new HashMap<>();	
								
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
		        		  .writeValueAsString(authRequest)))
		          .exchange()
		          .expectStatus()
		          .isOk()
		          .expectBody()
				  //.expectBody(AuthenticationResponse.class);				
					.jsonPath("$.username")
					.isEqualTo(authResponse.getUsername())					
					.jsonPath("$.access_token")
					.isEqualTo(authResponse.getAccessToken())
					.jsonPath("$.refresh_token")
					.isEqualTo(authResponse.getRefreshToken())					
					.jsonPath("$.expires_in")
					.isEqualTo(authResponse.getExpiresIn())
					.jsonPath("$.refresh_expires_in")
					.isEqualTo(authResponse.getRefreshExpiresIn())
					.jsonPath("$.secret")
					.isEqualTo(authResponse.getSecret())	
					.jsonPath("$.roles")
					.isEqualTo(authResponse.getRoles())
					.consumeWith(
			            document(
			                    "signin", 			                    
			                    preprocessRequest(prettyPrint()), 
			                    preprocessResponse(prettyPrint()),
						requestHeaders(
							headerWithName("Content-Type")
							  .description("The Content-Type of the request payload, e.g. `application/json`")
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
	    @DisplayName("to get my info")
	    void verifyGetMyInfoAuthenticated() {
	
	      UUID userId = UUID.randomUUID();
	      UserVO expectedUser = UserBuilder.user().userVo(userId);
	      
	      given(profileFinderService.findByUsername(anyString()))
	      		.willReturn(Mono.just(expectedUser)); 
	      
	      String endPointURI = "/my-info";		
	      
	      Map<String, Object> templateVar = new HashMap<>();
	      Map<String, String> params = new HashMap<>();
	      params.put("lang", "fr");
	
		  restCallUserResponseOne(
					endPointURI, templateVar, 
					params, expectedUser)			
				.consumeWith(
		            document(
		                    "my-info", 			                    
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
	    @DisplayName("to get user info")
	    void verifyGetUserInfoAuthenticated() {
	
	      UUID userId = UUID.randomUUID();
	      UserVO expectedUser = UserBuilder.user().userVo(userId);
	      
	      given(profileFinderService.findUserById(anyString()))
	      		.willReturn(Mono.just(expectedUser)); 
	      
	      String endPointURI = "/user-info/"+userId;		
	      
	      Map<String, Object> templateVar = new HashMap<>();
	      Map<String, String> params = new HashMap<>();
	      params.put("lang", "fr");
	
		  restCallUserResponseOne(
					endPointURI, templateVar, 
					params, expectedUser)			
				.consumeWith(
		            document(
		                    "user-info", 			                    
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
	    @Disabled
	    @DisplayName("to get a single user by username")
	    void verifyGetUserByUsernameAuthenticated() {
	
	      UUID userId = UUID.randomUUID();
	      UserVO expectedUser = UserBuilder.user().userVo(userId);
	      
	      String endPointURI = "/username/" + expectedUser.getUsername();
	      	
	      given(userAuthenticationService.enableUserProfile(anyString(), anyBoolean()))
					.willReturn(Mono.just("SUCCESS"));
	      
	      Map<String, Object> templateVar = new HashMap<>();
	      Map<String, String> params = new HashMap<>();
	      params.put("lang", "fr");
		
		  restCallUserResponseOne(
					endPointURI, templateVar, 
					params, expectedUser)			
				.consumeWith(
		            document(
		                    "user-info by username", 			                    
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
	    @DisplayName("to update my info")
	    void verifyUpdateMyInfoAuthenticated() throws JsonProcessingException {
	
	    	UUID userId = UUID.randomUUID();
	    	String endPointURI = "/my-info";	
	    	
		    UserVO expectedUser = UserBuilder.user().userVo(userId);
		
		    UserDetailsUpdateRequest userResource = UserDetailsUpdateRequest.builder()
		    								.firstName(expectedUser.getFirstName())
		    								.lastName(expectedUser.getLastName())
		    								.build();
		
		    when(userAuthenticationService.updateUserDetails(anyString(), 
		    		any(UserDetailsUpdateRequest.class)))
		    		.thenReturn(Mono.just(expectedUser));
	
		     webTestClient
		          //.mutateWith(mockUser().roles("LIBRARY_ADMIN"))
		          .mutateWith(csrf())
		          .put()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .contentType(MediaType.APPLICATION_JSON)
		          .body(BodyInserters.fromValue(new ObjectMapper()
		        		  .writeValueAsString(userResource)))
		          .exchange()
		          .expectStatus()
		          .isOk()
		          .expectBody()
		          .consumeWith(
				       document(
				          "update my info", 			                    
				             preprocessRequest(prettyPrint()), 
				             preprocessResponse(prettyPrint())));
	    }
	    
	    /**
	     * 
	     * @throws JsonProcessingException
	     */
	    @Test
	    @WithMockUser(roles = { "ADMIN" })
	    @DisplayName("to update user info")
	    void verifyUpdateUserInfoAuthenticated() throws JsonProcessingException {
	
	    	UUID userId = UUID.randomUUID();
	    	String endPointURI = "/user-info/"+userId;	
	    	
		    UserVO expectedUser = UserBuilder.user().userVo(userId);
		
		    UserDetailsUpdateRequest userResource = UserDetailsUpdateRequest.builder()
		    								.firstName(expectedUser.getFirstName())
		    								.lastName(expectedUser.getLastName())
		    								.build();
		
		    when(userAuthenticationService.updateUserById(anyString(), 
		    		any(UserDetailsUpdateRequest.class)))
		    		.thenReturn(Mono.just(expectedUser));
	
		     webTestClient
		          //.mutateWith(mockUser().roles("LIBRARY_ADMIN"))
		          .mutateWith(csrf())
		          .put()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .contentType(MediaType.APPLICATION_JSON)
		          .body(BodyInserters.fromValue(new ObjectMapper()
		        		  .writeValueAsString(userResource)))
		          .exchange()
		          .expectStatus()
		          .isOk()
		          .expectBody()
		          .consumeWith(
				       document(
				          "update user info", 			                    
				             preprocessRequest(prettyPrint()), 
				             preprocessResponse(prettyPrint())));
	    }
	    
	    /**
	     * 
	     * @throws JsonProcessingException
	     */
	    @Test
	    @WithMockUser(roles = { "ADMIN" })
	    @DisplayName("to update my status")
	    void verifyUpdateMyStatusAuthenticated() throws JsonProcessingException {
	
	    	UUID userId = UUID.randomUUID();
	    	String endPointURI = "/my/status";	
	    			
		    StatusUpdateRequest userResource = StatusUpdateRequest.builder()
		    								.status(StatusType.EXPIRED.toString())
		    								.build();
		
		    when(userAuthenticationService.updateUserStatus(anyString(), 
		    		any(StatusUpdateRequest.class)))
		    		.thenReturn(Mono.just("SUCCESS"));
	
		     webTestClient
		          //.mutateWith(mockUser().roles("LIBRARY_ADMIN"))
		          .mutateWith(csrf())
		          .put()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .contentType(MediaType.APPLICATION_JSON)
		          .body(BodyInserters.fromValue(new ObjectMapper()
		        		  .writeValueAsString(userResource)))
		          .exchange()
		          .expectStatus()
		          .isOk()
		          .expectBody()
		          .consumeWith(
				       document(
				          "update my status", 			                    
				             preprocessRequest(prettyPrint()), 
				             preprocessResponse(prettyPrint())));
	    }
	    
	    
	    /**
	     * 
	     * @throws JsonProcessingException
	     */
	    @Test
	    @WithMockUser(roles = { "ADMIN" })
	    @DisplayName("to update user status")
	    void verifyUpdateUserStatusAuthenticated() throws JsonProcessingException {
	
	    	UUID userId = UUID.randomUUID();
	    	String endPointURI = "/user/" + userId + "/status";	
	    			
		    StatusUpdateRequest userResource = StatusUpdateRequest.builder()
		    								.status(StatusType.EXPIRED.toString())
		    								.build();
		
		    when(userAuthenticationService.updateUserByIdStatus(anyString(), 
		    		any(StatusUpdateRequest.class)))
		    		.thenReturn(Mono.just("SUCCESS"));
	
		     webTestClient
		          //.mutateWith(mockUser().roles("LIBRARY_ADMIN"))
		          .mutateWith(csrf())
		          .put()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .contentType(MediaType.APPLICATION_JSON)
		          .body(BodyInserters.fromValue(new ObjectMapper()
		        		  .writeValueAsString(userResource)))
		          .exchange()
		          .expectStatus()
		          .isOk()
		          .expectBody()
		          .consumeWith(
				       document(
				          "update user status", 			                    
				             preprocessRequest(prettyPrint()), 
				             preprocessResponse(prettyPrint())));
	    }
	    
	    /**
	     * 
	     * @throws JsonProcessingException
	     */
	    @Test
	    @WithMockUser(roles = { "ADMIN" })
	    @DisplayName("to update user email status")
	    void verifyUpdateUserEmailStatusAuthenticated() throws JsonProcessingException {
	
	    	UUID userId = UUID.randomUUID();
	    	UserVO expectedUser = UserBuilder.user().userVo(userId);
	    	String endPointURI = "/email/status";	
	    			
	    	EmailStatusUpdateRequest userResource = EmailStatusUpdateRequest.builder()
		    								.email(expectedUser.getEmail())
		    								.verified(false)
		    								.build();
		
		    when(userAuthenticationService.updateEmailStatus( 
		    		any(EmailStatusUpdateRequest.class)))
		    		.thenReturn(Mono.just("SUCCESS"));
	
		    webTestClient
		          //.mutateWith(mockUser().roles("LIBRARY_ADMIN"))
		          .mutateWith(csrf())
		          .put()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .contentType(MediaType.APPLICATION_JSON)
		          .body(BodyInserters.fromValue(new ObjectMapper()
		        		  .writeValueAsString(userResource)))
		          .exchange()
		          .expectStatus()
		          .isOk()
		          .expectBody()
		          .consumeWith(
				       document(
				          "update email status", 			                    
				             preprocessRequest(prettyPrint()), 
				             preprocessResponse(prettyPrint())));
	    }
	    
	    /**
	     * 
	     */
	    @Test
	    @WithMockUser(roles = { "ADMIN" })
	    @DisplayName("to enable user profile")
	    void verifyEnableUserProfileAuthenticated() {
	
	    	UUID userId = UUID.randomUUID();
	    	 
			when(userAuthenticationService.enableUserProfile(anyString(), anyBoolean()))
					.thenReturn(Mono.just("SUCCESS"));
		      
		     String endPointURI = "/user/"+ userId;
	
		     webTestClient
		          .put()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .exchange()
		          .expectStatus()
		          .isOk()
		          .expectBody()
		          .consumeWith(
				       document(
				          "enable profile", 			                    
				             preprocessRequest(prettyPrint()), 
				             preprocessResponse(prettyPrint())));
	    }
	    
	    /**
	     * 
	     */
	    @Test
	    @WithMockUser(roles = { "ADMIN" })
	    @DisplayName("to disable user profile")
	    void verifyDisableUserProfileAuthenticated() {
	
	    	UUID userId = UUID.randomUUID();
	    	 
			when(userAuthenticationService.enableUserProfile(anyString(), anyBoolean()))
					.thenReturn(Mono.just("SUCCESS"));
		      
		     String endPointURI = "/user/"+ userId;
	
		     webTestClient
		          .delete()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .exchange()
		          .expectStatus()
		          .isOk()
		          .expectBody()
		          .consumeWith(
				       document(
				          "disable profile", 			                    
				             preprocessRequest(prettyPrint()), 
				             preprocessResponse(prettyPrint())));
	    }
	
	    
	    /**
	     * @throws JsonProcessingException 
	     * 
	     */
	    @Test
	    //@WithMockUser(roles = { "ADMIN" })
	    @DisplayName("to refresh token")
	    void verifyRefreshTokenAuthenticated() throws JsonProcessingException {
	
	      UUID userId = UUID.randomUUID();
	      UserVO expectedUser = UserBuilder.user().userVo(userId);
	      
	      AuthenticationResponse authResponse = AuthBuilder.auth(expectedUser).authResponse();
		  
		  Map<String, Object> templateVar = new HashMap<>();
	      Map<String, String> params = new HashMap<>();
	      params.put("lang", "fr");
		    	      
	      String endPointURI = "/access-token/"+expectedUser.getUsername();
	      given(userAuthenticationService.refreshToken(anyString(), anyString()))
	      				.willReturn(Mono.just(authResponse));
	
	      webTestClient
	          //.mutateWith(mockUser().roles("LIBRARY_ADMIN"))
	          .mutateWith(csrf())
	          .patch()
	          .uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
						.queryParams(buildParameters(params))
						.build(templateVar))
	          .header("refresh-token", authResponse.getRefreshToken())
	          .accept(MediaType.APPLICATION_JSON)
	          .exchange()
	          .expectStatus()
	          .isOk()
	          .expectBody(AuthenticationResponse.class)
	          .consumeWith(
			       document(
			          "refresh access token", 			                    
			             preprocessRequest(prettyPrint()), 
			             preprocessResponse(prettyPrint())));
	       //=============================================================
		    // Missing token header
	      webTestClient
	          //.mutateWith(mockUser().roles("LIBRARY_ADMIN"))
		      .mutateWith(csrf())
	          .patch()
	          .uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
						.queryParams(buildParameters(params))
						.build(templateVar))
	          //.header("refresh-token", authResponse.getRefreshToken())
	          .accept(MediaType.APPLICATION_JSON)
	          .exchange()
	          .expectStatus()
	          .isBadRequest()
	          //.expectBody(AppResponse.class);
	          .expectBody()
	          .consumeWith(
			       document(
			          "refresh access token - refresh token not found", 			                    
			             preprocessRequest(prettyPrint()), 
			             preprocessResponse(prettyPrint())));
	    }
	    
	    /**
	     * 
	     */
	    @Test
	    @WithMockUser(roles = { "ADMIN" })
	    @DisplayName("to revoke token")
	    void verifyRevokeTokenAuthenticated() {
	
	      UUID userId = UUID.randomUUID();
		  UserVO expectedUser = UserBuilder.user().userVo(userId);
		      
		  AuthenticationResponse authResponse = AuthBuilder.auth(expectedUser).authResponse();
			  
		  Map<String, Object> templateVar = new HashMap<>();
		  Map<String, String> params = new HashMap<>();
		  params.put("lang", "fr");
	      
	      String endPointURI = "/access-token";
	      given(userAuthenticationService.revokeToken(anyString()))
	      				.willReturn(Mono.just("SUCCESS"));
	
	      webTestClient
	          //.mutateWith(mockUser().roles("LIBRARY_ADMIN"))
	          .mutateWith(csrf())
	          .delete()
	          .uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
						.queryParams(buildParameters(params))
						.build(templateVar))
	          .header("refresh-token", authResponse.getRefreshToken())
	          .accept(MediaType.APPLICATION_JSON)
	          .exchange()
	          .expectStatus()
	          .isOk()
	          .expectBody()
	          .consumeWith(
			       document(
			          "revoke access token", 			                    
			             preprocessRequest(prettyPrint()), 
			             preprocessResponse(prettyPrint())));
	      
	      //=============================================================
		    // Missing token header
	      webTestClient
		          //.mutateWith(mockUser().roles("LIBRARY_ADMIN"))
		          .mutateWith(csrf())
		          .delete()
		          .uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
							.queryParams(buildParameters(params))
							.build(templateVar))
		          //.header("refresh-token", authResponse.getRefreshToken())
		          .accept(MediaType.APPLICATION_JSON)
		          .exchange()
		          .expectStatus()
		          .isBadRequest()
		          //.expectBody(AppResponse.class);
		          .expectBody()
		          .consumeWith(
				       document(
				          "revoke access token- refresh token not found", 			                    
				             preprocessRequest(prettyPrint()), 
				             preprocessResponse(prettyPrint())));
	    }
	    
	    /**
	     * 
	     * @throws JsonProcessingException
	     */
	    @Test
	    @WithMockUser(roles = { "ADMIN" })
	    @DisplayName("to logout user")
	    void verifyLogoutAuthenticated() throws JsonProcessingException {
	
	    	UUID userId = UUID.randomUUID();
	    	String endPointURI = "/signout/"+ userId;
	    	
			UserVO expectedUser = UserBuilder.user().userVo(userId);	    	
	    	AuthenticationResponse authResponse = AuthBuilder.auth(expectedUser).authResponse();
	    			
		    Map<String, Object> templateVar = new HashMap<>();
			Map<String, String> params = new HashMap<>();
			params.put("lang", "fr");
			  
		    when(userAuthenticationService.logout(anyString(), anyString()))
		    		.thenReturn(Mono.just("SUCCESS"));
		    	
		    webTestClient
		          //.mutateWith(mockUser().roles("LIBRARY_ADMIN"))
		          .mutateWith(csrf())
		          .post()
		          .uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
							.queryParams(buildParameters(params))
							.build(templateVar))
		          .header("refresh-token", authResponse.getRefreshToken())
		          .accept(MediaType.APPLICATION_JSON)
		          .exchange()
		          .expectStatus()
		          .isOk()
				  .expectBody()
		          .consumeWith(
				       document(
				          "logout user", 			                    
				             preprocessRequest(prettyPrint()), 
				             preprocessResponse(prettyPrint())));
						
		    //=======================================================
		    // Missing token header
		    webTestClient
		          //.mutateWith(mockUser().roles("LIBRARY_ADMIN"))
		          .mutateWith(csrf())
		          .post()
		          .uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
							.queryParams(buildParameters(params))
							.build(templateVar))
		          //.header("refresh-token", authResponse.getRefreshToken())
		          .accept(MediaType.APPLICATION_JSON)
		          .exchange()
		          .expectStatus()
		          .isBadRequest()
				  .expectBody()
			          .consumeWith(
					       document(
					          "logout user- refresh token not found", 			                    
					             preprocessRequest(prettyPrint()), 
					             preprocessResponse(prettyPrint())));
	    }
	    
	    /**
	     * 
	     * @throws JsonProcessingException
	     */
	    @Test
	    @WithMockUser(roles = { "ADMIN" })
	    @DisplayName("to log me out")
	    void verifyLogmeoutAuthenticated() throws JsonProcessingException {
	
	    	String endPointURI = "/signmeout";	
	    			
	    	UUID userId = UUID.randomUUID();
			UserVO expectedUser = UserBuilder.user().userVo(userId);
	    	
	    	AuthenticationResponse authResponse = AuthBuilder.auth(expectedUser).authResponse();
	    			
		    Map<String, Object> templateVar = new HashMap<>();
			Map<String, String> params = new HashMap<>();
			params.put("lang", "fr");
			
			when(userAuthenticationService.signout(anyString(), anyString()))
    			.thenReturn(Mono.just("SUCCESS"));
	
		    webTestClient
		          //.mutateWith(mockUser().roles("LIBRARY_ADMIN"))
		          .mutateWith(csrf())
		          .post()
		          .uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
							.queryParams(buildParameters(params))
							.build(templateVar))
		          .header("refresh-token", authResponse.getRefreshToken())
		          .accept(MediaType.APPLICATION_JSON)
		          .exchange()
		          .expectStatus()
		          .isOk()
		          .expectBody()
		          .consumeWith(
				       document(
				          "log me out", 			                    
				             preprocessRequest(prettyPrint()), 
				             preprocessResponse(prettyPrint())));
		    
		    //=======================================================
		    // Missing token header
		    webTestClient
		          //.mutateWith(mockUser().roles("LIBRARY_ADMIN"))
		          .mutateWith(csrf())
		          .post()
		          .uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
							.queryParams(buildParameters(params))
							.build(templateVar))
		          //.header("refresh-token", authResponse.getRefreshToken())
		          .accept(MediaType.APPLICATION_JSON)
		          .exchange()
		          .expectStatus()
		          .isBadRequest()
		          .expectBody()
		          .consumeWith(
				       document(
				          "log me out - refresh token not found", 			                    
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
		    @DisplayName("to get my profile")
		    void verifyGetMyProfileUnAuthenticated() {
		
		    	String endPointURI = "/my-info";
				
				final Map<String, String> params = new HashMap<>();
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
		    @DisplayName("to update my profile")
		    void verifyUpdateMyProfileUnAuthenticated() {
		
		      String endPointURI = "/my-info";	
		
		      webTestClient
		          .put()
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
		    @DisplayName("to get user profile")
		    void verifyGetUserProfileUnAuthenticated() {
		
		    	UUID userId = UUID.randomUUID();
		    	String endPointURI = "/user-info/" + userId;
				
				final Map<String, String> params = new HashMap<>();
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
		    @DisplayName("to update my profile status")
		    void verifyUpdateMyStatusProfileUnAuthenticated() {
		      
		    	UUID userId = UUID.randomUUID();
			    String endPointURI = "/my/status";
			
			     webTestClient
			          .put()
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
		    @DisplayName("to update user profile status")
		    void verifyUpdateUserStatusUnAuthenticated() {
		    	
		    	UUID userId = UUID.randomUUID();
		    	String endPointURI = "/user/"+ userId + "/status";
				
				final Map<String, String> params = new HashMap<>();
				params.put("lang", "fr");	
		    	
			     webTestClient
			          .put()
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
		    @DisplayName("to update user profile")
		    void verifyUpdateUserEmailStatusUnAuthenticated() {
		
		      String endPointURI = "/email/status";	
		
		      webTestClient
		          .put()
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
		    @DisplayName("to enable user profile")
		    void verifyEnableUserProfileUnAuthenticated() {
		
		    	 UUID userId = UUID.randomUUID();
			      
			     String endPointURI = "/user/"+ userId;
		
			     webTestClient
			          .put()
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
		    @DisplayName("to disable user profile")
		    void verifyDisableUserProfileUnAuthenticated() {
		
		    	 UUID userId = UUID.randomUUID();
			      
			     String endPointURI = "/user/"+ userId;
		
			     webTestClient
			          .put()
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
		    @DisplayName("to sign me out")
		    void verifySignmeOutUnAuthenticated() {
					      
			      String endPointURI = "/signmeout";
		
			      webTestClient
			          .mutateWith(csrf())
			          .post()
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
		    @DisplayName("to sign a user out")
		    void verifySignUserOutUnAuthenticated() {
				
		    	 UUID userId = UUID.randomUUID();
			     String endPointURI = "/signout/" + userId;
		
			      webTestClient
			          .mutateWith(csrf())
			          .post()
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
		    @DisplayName("to revoke token")
		    void verifyRevokeTokenUnAuthenticated() {
					      
			      String endPointURI = "/access-token";
		
			      webTestClient
			          .mutateWith(csrf())
			          .delete()
			          .uri(PATH + endPointURI)
			          .accept(MediaType.APPLICATION_JSON)
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
