/**
 * 
 */
package com.keycloak.admin.client.api;

import static org.mockito.ArgumentMatchers.*;
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
import com.keycloak.admin.client.config.MessageConfig;
import com.keycloak.admin.client.config.SecurityConfig;
import com.keycloak.admin.client.config.keycloak.KeycloakConfiguration;
import com.keycloak.admin.client.dataacess.AuthBuilder;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.handlers.TokenManagementHandler;
import com.keycloak.admin.client.models.AuthenticationResponse;
import com.keycloak.admin.client.models.EnableMfaResponse;
import com.keycloak.admin.client.models.SendOtpRequest;
import com.keycloak.admin.client.models.TotpRequest;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.oauth.service.it.ActivationTokenService;
import com.keycloak.admin.client.oauth.service.it.UserAuthenticationService;
import com.keycloak.admin.client.oauth.service.it.UserCredentialFinderService;
import com.keycloak.admin.client.router.config.MfaAuthRouterConfig;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Log4j2
@WebFluxTest(controllers = TokenManagementHandler.class) // <1>
@DisplayName("Registration endpoints API")
@Import({ AuthProperties.class, ResponseCreator.class, 
	CustomMessageSourceAccessor.class, AuthenticatedUserMgr.class,
	TokenManagementHandler.class })
@ExtendWith({ RestDocumentationExtension.class, SpringExtension.class })
@ContextConfiguration(classes = { SecurityConfig.class, KeycloakConfiguration.class, 
				MfaAuthRouterConfig.class, MessageConfig.class })
class MfaApiHandlerTest {

	@Autowired 
	private ApplicationContext applicationContext;

	@Autowired
	private WebTestClient webTestClient;
	
	@MockBean
	private UserAuthenticationService userAuthenticationService;
	@MockBean
	private ActivationTokenService activationService;
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
		    @DisplayName("to validate totp request")
		    void verifyValidateTotpRequest() throws JsonProcessingException { 
				
				UUID userId = UUID.randomUUID();
				UserVO expectedUser = UserBuilder.user().userVo(userId);
				
				TotpRequest authRequest = AuthBuilder.auth(expectedUser).buildTotpRequest(false, false);			    	
				log.info("Totp Request {}", authRequest);
				
				AuthenticationResponse authResponse = AuthBuilder.auth(expectedUser).authResponse();
				
				given(userAuthenticationService.verifyTotpCode(any(TotpRequest.class), 
										any(ServerHttpRequest.class)))
						.willReturn(Mono.just(authResponse)); 
	
				String endPointURI = "/totp";	
				
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
		          .isOk()
		          .expectBody()
		          .consumeWith(
				       document(
				          "validate totp code", 			                    
				             preprocessRequest(prettyPrint()), 
				             preprocessResponse(prettyPrint())));
	    }
	
	    /**
	     * @throws JsonProcessingException 
	     * 
	     */
	    @Test
	    @DisplayName("to send Otp code")
	    void verifyValidateSendOtpCode() throws JsonProcessingException {
	
	      UUID userId = UUID.randomUUID();
		  UserVO expectedUser = UserBuilder.user().userVo(userId);
		  
	      SendOtpRequest authRequest = AuthBuilder.auth(expectedUser).buildOtpRequest(false, false);	 
	      
	      given(userAuthenticationService.sendOtpCode(any(SendOtpRequest.class), 
					any(ServerHttpRequest.class)))
	      		.willReturn(Mono.just("SUCCESS")); 
	      
	      String endPointURI = "/totp";		
	      
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
			          "validate send Otp code", 			                    
			             preprocessRequest(prettyPrint()), 
			             preprocessResponse(prettyPrint())));
	   }
	    
	    /**
	     * @throws JsonProcessingException 
	     * 
	     */
	    @Test
	    @WithMockUser(roles = { "ADMIN" })
	    @DisplayName("to activate Mfa")
	    void verifyActivateMfaAuthenticated() throws JsonProcessingException {
	
	      UUID userId = UUID.randomUUID();
	      UserVO expectedUser = UserBuilder.user().userVo(userId);
	      
	      EnableMfaResponse mfaResponse = EnableMfaResponse.builder()
	    		  		.message("A message").qrCodeImage("A qrImage").build();
	       
	      given(userAuthenticationService.updateMFA(any(String.class), anyBoolean()))
	      		.willReturn(Mono.just(mfaResponse)); 
	      
	      String endPointURI = "/mfa";		
	      
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
	          .exchange()
	          .expectStatus()
	          .isOk()
	          .expectBody()
	          .consumeWith(
			       document(
			          "activate mfa", 			                    
			             preprocessRequest(prettyPrint()), 
			             preprocessResponse(prettyPrint())));
	   }	    
	    
	    
	    /**
	     * 
	     * @throws JsonProcessingException
	     */
	    @Test
	    @WithMockUser(roles = { "ADMIN" })
	    @DisplayName("to deactivate Mfa")
	    void verifyDeactivateMfaAuthenticated() throws JsonProcessingException {
		
		    String endPointURI = "/mfa";	
		    
		    EnableMfaResponse mfaResponse = EnableMfaResponse.builder()
    		  		.message("A message").qrCodeImage("").build();
       
		    given(userAuthenticationService.updateMFA(any(String.class), anyBoolean()))
      				.willReturn(Mono.just(mfaResponse)); 
		      
		    Map<String, Object> templateVar = new HashMap<>();
		    Map<String, String> params = new HashMap<>();
		    params.put("lang", "fr");
	
		    webTestClient
	          .mutateWith(csrf())
	          .delete()
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
			          "deactivate mfa", 			                    
			             preprocessRequest(prettyPrint()), 
			             preprocessResponse(prettyPrint())));
	    }
	    
	 
	    /**
	     * 
	     * @throws JsonProcessingException
	     */
	    @Test
	    @DisplayName("to renew Activation token")
	    void verifyRenewActivationTokenAuthenticated() throws JsonProcessingException {
	
	    	String endPointURI = "/email/activation";	
	    			
		    Map<String, Object> templateVar = new HashMap<>();
			Map<String, String> params = new HashMap<>();
			params.put("lang", "fr");
			params.put("token", RandomGenerator.generateNewToken());
			
			when(activationService.renewActivationToken(anyString(),
					any(ServerHttpRequest.class)))
				.thenReturn(Mono.just("SUCCESS"));
	
		    webTestClient
		          //.mutateWith(mockUser().roles("LIBRARY_ADMIN"))
		          .mutateWith(csrf())
		          .put()
		          .uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI) 
							.queryParams(buildParameters(params))
							.build(templateVar))
		          .accept(MediaType.APPLICATION_JSON)
		          .exchange()
		          .expectStatus()
		          .isOk()
		          .expectBody()
		          .consumeWith(
				       document(
				          "renew Activation token", 			                    
				             preprocessRequest(prettyPrint()), 
				             preprocessResponse(prettyPrint())));	
		    
		    // =======================================================================
		    // no token, expect Bad request Status code
		    
		    webTestClient
	          //.mutateWith(mockUser().roles("LIBRARY_ADMIN"))
	          .mutateWith(csrf())
	          .put()
	          .uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI) 
						.build(templateVar))
	          .accept(MediaType.APPLICATION_JSON)
	          .exchange()
	          .expectStatus()
	          .isBadRequest()
	          .expectBody()
	          .consumeWith(
			       document(
			          "renew Activation with missing token", 			                    
			             preprocessRequest(prettyPrint()), 
			             preprocessResponse(prettyPrint())));
	    }
	    
	    /**
	     * 
	     * @throws JsonProcessingException
	     */
	    @Test
	    @DisplayName("to validate Activation token")
	    void verifyValidateActivationTokenAuthenticated() throws JsonProcessingException {
	
	    	String endPointURI = "/email/activation";	
	    			
		    Map<String, Object> templateVar = new HashMap<>();
			Map<String, String> params = new HashMap<>();
			params.put("lang", "fr");
			params.put("token", RandomGenerator.generateNewToken());
			
			when(activationService.validateEmailActivationToken(anyString()))
				.thenReturn(Mono.just("SUCCESS"));
	
		    webTestClient
		          //.mutateWith(mockUser().roles("LIBRARY_ADMIN"))
		          .mutateWith(csrf())
		          .get()
		          .uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI) 
							.queryParams(buildParameters(params))
							.build(templateVar))
		          .accept(MediaType.APPLICATION_JSON)
		          .exchange()
		          .expectStatus()
		          .isOk()
		          .expectBody()
		          .consumeWith(
				       document(
				          "validate Activation token", 			                    
				             preprocessRequest(prettyPrint()), 
				             preprocessResponse(prettyPrint())));	
		    
		    // =======================================================================
		    // No token, expect Bad request Status code
		    
		     webTestClient
		          //.mutateWith(mockUser().roles("LIBRARY_ADMIN"))
		          .mutateWith(csrf())
		          .get()
		          .uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI) 
							.build(templateVar))
		          .accept(MediaType.APPLICATION_JSON)
		          .exchange()
		          .expectStatus()
		          .isBadRequest()
		          .expectBody()
		          .consumeWith(
				       document(
				          "validate Activation with missing token", 			                    
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
		    @DisplayName("to activate Mfa")
		    void verifyActivateMfaUnAuthenticated() {
		
		    	String endPointURI = "/mfa";		
			      
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
			       .exchange()
			       .expectStatus()
			       .isUnauthorized();
		    }
		    
		    /**
			 * 
			 */
		    @Test
		    @DisplayName("to deactivate Mfa")
		    void verifyChangePasswordUnAuthenticated() {
		
		    	String endPointURI = "/mfa";		
			      
			    Map<String, Object> templateVar = new HashMap<>();
			    Map<String, String> params = new HashMap<>();
			    params.put("lang", "fr");
		
			    webTestClient
		          .mutateWith(csrf())
		          .delete()
		          .uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
							.queryParams(buildParameters(params))
							.build(templateVar))
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
