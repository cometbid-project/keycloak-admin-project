/**
 * 
 */
package com.keycloak.admin.client.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keycloak.admin.client.common.utils.RandomGenerator;
import com.keycloak.admin.client.common.utils.ResponseCreator;
import com.keycloak.admin.client.components.AuthenticatedUserMgr;
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;
import com.keycloak.admin.client.config.AuthProperties;
import com.keycloak.admin.client.config.SecurityConfig;
import com.keycloak.admin.client.config.keycloak.KeycloakConfiguration;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.handlers.PasswordManagementHandler;
import com.keycloak.admin.client.models.ForgotUsernameRequest;
import com.keycloak.admin.client.models.PasswordResetTokenResponse;
import com.keycloak.admin.client.models.PasswordUpdateRequest;
import com.keycloak.admin.client.models.ResetPasswordFinalRequest;
import com.keycloak.admin.client.models.ResetPasswordRequest;
import com.keycloak.admin.client.oauth.service.it.PasswordMgtService;
import com.keycloak.admin.client.oauth.service.it.UserCredentialFinderService;
import com.keycloak.admin.client.router.config.PasswordAuthRouterConfig;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Log4j2
@WebFluxTest(controllers = PasswordManagementHandler.class) // <1>
@DisplayName("Password Management endpoints API")
@Import({ AuthProperties.class, ResponseCreator.class, CustomMessageSourceAccessor.class, AuthenticatedUserMgr.class,
		PasswordManagementHandler.class })
@ExtendWith({ RestDocumentationExtension.class, SpringExtension.class })
@ContextConfiguration(classes = { SecurityConfig.class, KeycloakConfiguration.class, PasswordAuthRouterConfig.class })
class PasswordMgtHandlerTest {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private WebTestClient webTestClient;

	@MockBean
	private PasswordMgtService passwordService;
	@MockBean
	private UserCredentialFinderService profileFinderService;
	// @MockBean
	// private AuthenticatedUserMgr userAuthMgr;

	protected String PATH = "/api/v1";
	private String apiKey = "eyJhbGciOiJIUzI1NiJ9.eyJSb2xlIjoiQWRtaW4iLCJJc3N1ZXIiOiJJc3N1...";

	@BeforeEach
	void setUp(RestDocumentationContextProvider restDocumentation) {

		this.webTestClient = WebTestClient.bindToApplicationContext(applicationContext)
				.apply(springSecurity())
				.configureClient().baseUrl("http://localhost:8080")
				.filter(documentationConfiguration(restDocumentation)
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
		    @DisplayName("to initiate reset password")
		    void verifyInitialResetPasswordRequest() throws JsonProcessingException { 
				
				//UUID userId = UUID.randomUUID();			    
			    ResetPasswordRequest authRequest = UserBuilder.user().buildPasswordResetRequest();			    	
				log.info("Password Request {}", authRequest);
				
				given(passwordService.initiateResetPasswd(any(ResetPasswordRequest.class), 
										any(ServerHttpRequest.class)))
						.willReturn(Mono.just("SUCCESS")); 
	
				String endPointURI = "/password";	
				
				final Map<String, String> params = new HashMap<>();				
				final Map<String, Object> templateVar = new HashMap<>();	
								
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
		          .isAccepted()
		          .expectBody()
		          .consumeWith(
				       document(
				          "initiate reset password", 			                    
				             preprocessRequest(prettyPrint()), 
				             preprocessResponse(prettyPrint())));
	    }
	
	    /**
	     * 
	     */
	    @Test
	    @DisplayName("to validate reset password")
	    void verifyValidateResetPassword() {
	
	      UUID sessionId = UUID.randomUUID();
	      PasswordResetTokenResponse authResponse = UserBuilder.user().buildPasswordResetToken(sessionId.toString());	 
	      
	      given(passwordService.validatePasswordResetToken(anyString()))
	      		.willReturn(Mono.just(authResponse)); 
	      
	      String endPointURI = "/password/token-validation";		
	      
	      Map<String, Object> templateVar = new HashMap<>();
	      Map<String, String> params = new HashMap<>();
	      params.put("lang", "fr");
	      params.put("token", RandomGenerator.generateNewToken());
	
	      webTestClient
	          .mutateWith(csrf())
	          .get()
	          .uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
						.queryParams(buildParameters(params))
						.build(templateVar))
	          .header("api-key", apiKey)
	          .accept(MediaType.APPLICATION_JSON)
	          .exchange()
	          .expectStatus()
	          .isOk()
	          .expectBody()
	          .consumeWith(
			       document(
			          "validate reset password token", 			                    
			             preprocessRequest(prettyPrint()), 
			             preprocessResponse(prettyPrint())));
	   }
	    
	    /**
	     * @throws JsonProcessingException 
	     * 
	     */
	    @Test
	    @DisplayName("to reset password")
	    void verifyResetPassword() throws JsonProcessingException {
	
	      UUID sessionId = UUID.randomUUID();
	      ResetPasswordFinalRequest authRequest = UserBuilder.user()
	    		  		.buildFinalPasswordResetRequest(sessionId.toString());			    	
		  log.info("Password Reset Request {}", authRequest);
	       
	      given(passwordService.resetUserPassword(any(ResetPasswordFinalRequest.class), 
					any(ServerHttpRequest.class)))
	      		.willReturn(Mono.just("SUCCESS")); 
	      
	      String endPointURI = "/password";		
	      
	      Map<String, Object> templateVar = new HashMap<>();
	      Map<String, String> params = new HashMap<>();
	      params.put("lang", "fr");
	
	      webTestClient
	          .mutateWith(csrf())
	          .put()
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
	          .consumeWith(
			       document(
			          "reset password", 			                    
			             preprocessRequest(prettyPrint()), 
			             preprocessResponse(prettyPrint())));
	   }	    
	    
	    
	    /**
	     * 
	     * @throws JsonProcessingException
	     */
	    @Test
	    @WithMockUser(roles = { "ADMIN" })
	    @DisplayName("to change password")
	    void verifyChangePasswordAuthenticated() throws JsonProcessingException {
	
	    	//UUID userId = UUID.randomUUID();
	    	String endPointURI = "/password";	
	    	
	    	String oldPassword = RandomGenerator.generateRandomPassword();
	    	String newPassword = RandomGenerator.generateRandomPassword();
	    	PasswordUpdateRequest userResource = PasswordUpdateRequest.builder()
		    								.oldPassword(oldPassword).newPassword(newPassword)
		    								.build();
		
		    when(passwordService.changePassword(
		    		any(PasswordUpdateRequest.class), anyString(), 
		    		any(ServerHttpRequest.class)))
		    		.thenReturn(Mono.just("SUCCESS"));
	
		     webTestClient
		          //.mutateWith(mockUser().roles("LIBRARY_ADMIN"))
		          .mutateWith(csrf())
		          .patch()
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
				          "change password", 			                    
				             preprocessRequest(prettyPrint()), 
				             preprocessResponse(prettyPrint())));
	    }
	    
	 
	    /**
	     * 
	     * @throws JsonProcessingException
	     */
	    @Test
	    @DisplayName("to recover username")
	    void verifyRecoverUsernameAuthenticated() throws JsonProcessingException {
	
	    	String endPointURI = "/username";	
	    			
	    	UUID userId = UUID.randomUUID();
			//UserVO expectedUser = UserBuilder.user().userVo(userId);
	    	
			ForgotUsernameRequest authRequest = UserBuilder.user().buildUsernameRequest();
	    			
		    Map<String, Object> templateVar = new HashMap<>();
			Map<String, String> params = new HashMap<>();
			params.put("lang", "fr");
			
			when(passwordService.recoverUsername(any(ForgotUsernameRequest.class),
					any(ServerHttpRequest.class)))
  			.thenReturn(Mono.just("SUCCESS"));
	
		    webTestClient
		          //.mutateWith(mockUser().roles("LIBRARY_ADMIN"))
		          .mutateWith(csrf())
		          .post()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .contentType(MediaType.APPLICATION_JSON)		          
		          .body(BodyInserters.fromValue(new ObjectMapper()
		        		  .writeValueAsString(authRequest)))
		          .exchange()
		          .expectStatus()
		          .isAccepted()
		          .expectBody()
		          .consumeWith(
				       document(
				          "recover username", 			                    
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
		    @DisplayName("to change password")
		    void verifyChangePasswordUnAuthenticated() {
		
		    	String endPointURI = "/password";	
				
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
		
	   }
	  
		
		protected MultiValueMap<String, String> buildParameters(Map<String, String> params) {

			MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
			parameters.setAll(params);

			return parameters;
		}	
}
