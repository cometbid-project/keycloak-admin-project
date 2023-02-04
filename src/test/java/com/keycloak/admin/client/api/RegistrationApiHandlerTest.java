/**
 * 
 */
package com.keycloak.admin.client.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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
import com.keycloak.admin.client.common.enums.Role;
import com.keycloak.admin.client.common.utils.RandomGenerator;
import com.keycloak.admin.client.common.utils.ResponseCreator;
import com.keycloak.admin.client.components.AuthenticatedUserMgr;
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;
import com.keycloak.admin.client.config.AuthProperties;
import com.keycloak.admin.client.config.SecurityConfig;
import com.keycloak.admin.client.config.keycloak.KeycloakConfiguration;
import com.keycloak.admin.client.dataacess.AuthBuilder;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.handlers.PasswordManagementHandler;
import com.keycloak.admin.client.handlers.RegistrationHandler;
import com.keycloak.admin.client.models.AuthenticationResponse;
import com.keycloak.admin.client.models.EnableMfaResponse;
import com.keycloak.admin.client.models.SendOtpRequest;
import com.keycloak.admin.client.models.TotpRequest;
import com.keycloak.admin.client.models.UserRegistrationRequest;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.oauth.service.it.UserCredentialService;
import com.keycloak.admin.client.router.config.IndexFunctionRouterConfig;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Log4j2
@WebFluxTest(controllers = RegistrationHandler.class) // <1>
@DisplayName("Registration endpoints API")
@Import({ AuthProperties.class, ResponseCreator.class, CustomMessageSourceAccessor.class, AuthenticatedUserMgr.class,
				RegistrationHandler.class })
@ExtendWith({ RestDocumentationExtension.class, SpringExtension.class })
@ContextConfiguration(classes = { SecurityConfig.class, KeycloakConfiguration.class, IndexFunctionRouterConfig.class })
class RegistrationApiHandlerTest {
	
	@Autowired 
	private ApplicationContext applicationContext;

	@Autowired
	private WebTestClient webTestClient;
	
	@MockBean
	private UserCredentialService userAuthenticationService;	
	    
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
		  	@WithMockUser(roles = { "ADMIN" })
			@SuppressWarnings("unchecked")
			@Test
		    @DisplayName("to hello greetings to user")
		    void verifyPingAUserRequest() throws JsonProcessingException { 
	
				String endPointURI = "/ping";		
				
				final Map<String, String> params = new HashMap<>();				
				final Map<String, Object> templateVar = new HashMap<>();	
								
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
				          "ping a user", 			                    
				             preprocessRequest(prettyPrint()), 
				             preprocessResponse(prettyPrint())));
	    }
	
	    /**
	     * @throws JsonProcessingException 
	     * 
	     */
		@WithMockUser(roles = { "ADMIN" })
	    @Test
	    @DisplayName("to register an Admin user")
	    void verifyRegisterAdminUser() throws JsonProcessingException {
	
	      UUID userId = UUID.randomUUID();
		  UserVO expectedUser = UserBuilder.user().userVo(userId);
		  
		  UserRegistrationRequest userResource = UserBuilder.user().build();
		  	      
	      given(userAuthenticationService.signupUser(
	    		  	any(UserRegistrationRequest.class), 
	    		  	any(Role.class),
					any(ServerHttpRequest.class)))
	      		.willReturn(Mono.just(expectedUser)); 
	      
	      String endPointURI = "/signup";	
	      
	      Map<String, Object> templateVar = new HashMap<>();
	      Map<String, String> params = new HashMap<>();
	      params.put("lang", "fr");
	
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
	        		  .writeValueAsString(userResource)))
	          .exchange()
	          .expectStatus()
	          .isCreated()
	          .expectBody()
	          .consumeWith(
			       document(
			          "register a User", 			                    
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
		    @DisplayName("to say hello greetings")
		    void verifySayHelloUnAuthenticated() {
		
		    	String endPointURI = "/ping";		
			      
			    Map<String, Object> templateVar = new HashMap<>();
			    Map<String, String> params = new HashMap<>();
			    params.put("lang", "fr");
			
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
			       .isUnauthorized();
		    }
		    
		    /**
			 * 
			 */
		    @Test
		    @DisplayName("to register Admin user")
		    void verifyRegisterAdminUserUnAuthenticated() {
		
		    	String endPointURI = "/signup";	
			      
			    Map<String, Object> templateVar = new HashMap<>();
			    Map<String, String> params = new HashMap<>();
			    params.put("lang", "fr");
		
			    webTestClient
		          .mutateWith(csrf())
		          .post()
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

