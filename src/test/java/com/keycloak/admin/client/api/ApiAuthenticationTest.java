/**
 * 
 */
package com.keycloak.admin.client.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
//import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.BodyInserters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keycloak.admin.client.common.utils.ResponseCreator;
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;
import com.keycloak.admin.client.dataacess.AuthBuilder;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.models.AuthenticationRequest;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.oauth.service.KeycloakRestService;
import com.keycloak.admin.client.oauth.service.it.KeycloakOauthClientService;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
//@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ApiAuthenticationTest.TestConfig.class)
@DisplayName("Access to book api")
class ApiAuthenticationTest {

	@Autowired 
	private ApplicationContext applicationContext;

	private WebTestClient webTestClient;

	@MockBean 
	private KeycloakRestService restService;

	@MockBean 
	private KeycloakOauthClientService keycloakClient;

	@Autowired
	private CustomMessageSourceAccessor i8nMessageAccessor;

	@Autowired
	private ResponseCreator responseCreator;

	@BeforeEach
	void setUp() {
	    this.webTestClient =
	        WebTestClient.bindToApplicationContext(applicationContext)
	            .apply(springSecurity())
	            .configureClient()
	            .build();
	}

	@ComponentScan(
	    basePackages = {
	    "com.keycloak.admin.client.controllers",
	    "com.keycloak.admin.client.oauth.service",
	    "com.keycloak.admin.client.oauth.service.it",
	    "com.keycloak.admin.client.config"
	})
	@EnableWebFlux
	@EnableWebFluxSecurity
	@EnableAutoConfiguration(
	      exclude = {
	        MongoReactiveAutoConfiguration.class,
	        MongoAutoConfiguration.class,
	        MongoDataAutoConfiguration.class,
	       // EmbeddedMongoAutoConfiguration.class,
	        MongoReactiveRepositoriesAutoConfiguration.class,
	        MongoRepositoriesAutoConfiguration.class
	})	
	static class TestConfig {}

	  @DisplayName("as authenticated user is granted")
	  @Nested
	  class AuthenticatedUserApi {

	    @Test
	    @DisplayName("login to get access token")
	    void verifyLoginAuthenticated() throws JsonProcessingException { 
		  UserBuilder user = UserBuilder.user();

		  UUID userId = UUID.randomUUID();
		  UserVO userVO = user.userVo(userId);

	      given(keycloakClient.passwordGrantLogin(any(AuthenticationRequest.class)))
	      			.willReturn(Mono.just(AuthBuilder.auth(userVO).authResponse()));

	      AuthenticationRequest requestBody = AuthBuilder.auth(userVO).build();
 
	      webTestClient
	          .post()
	          .uri("/v1/login")
	          .accept(MediaType.APPLICATION_JSON)
		      .contentType(MediaType.APPLICATION_JSON)
		      .body(BodyInserters.fromValue(new ObjectMapper().writeValueAsString(requestBody)))
	          .exchange()
	          .expectStatus()
	          .isOk()
	          .expectHeader()
	          .exists("X-XSS-Protection")
	          .expectBody();
	    }

	    @WithMockUser
	    @Test
	    @DisplayName("logout using refresh token")
	    void verifyGetBookAuthenticated() {

	      given(restService.logout(any(String.class)))
	          		.willReturn(Mono.empty());

	      webTestClient
	          .post()
	          .uri("/v1/logout")
	          .accept(MediaType.APPLICATION_JSON)
	          .exchange()
	          .expectStatus()
	          .isOk();
	    }
	  }

	  @DisplayName("as unauthenticated user is denied with 401")
	  @Nested
	  class UnAuthenticatedBookApi {

	    @Test
	    @DisplayName("to get list of books")
	    void verifyGetBooksUnAuthenticated() throws JsonProcessingException {
	    	UserBuilder user = UserBuilder.user();

			UUID userId = UUID.randomUUID();
			UserVO userVO = user.userVo(userId);
			  
	    	given(keycloakClient.passwordGrantLogin(any(AuthenticationRequest.class)))
  			.willReturn(Mono.just(AuthBuilder.auth(userVO).authResponse()));

			  AuthenticationRequest requestBody = AuthBuilder.auth(userVO).build();
			
			  webTestClient
			      .post()
			      .uri("/v1/login")
			      .accept(MediaType.APPLICATION_JSON)
			      .contentType(MediaType.APPLICATION_JSON)
			      .body(BodyInserters.fromValue(new ObjectMapper().writeValueAsString(requestBody)))
			      .exchange()
			      .expectStatus()
			      .isOk()
			      .expectHeader()
			      .exists("X-XSS-Protection")
			      .expectBody();
	    }

	    @Test
	    @DisplayName("to get single book")
	    void verifyGetBookUnAuthenticated() {

		   webTestClient
		  		.mutateWith(csrf())
	          .post()
	          .uri("/v1/logout")
	          .accept(MediaType.APPLICATION_JSON)
	          .exchange()
	          .expectStatus()
	          .isUnauthorized();
	    }

	    @Test
	    @DisplayName("to borrow a book")
	    void verifyBorrowBookUnAuthenticated() {

	      UUID bookId = UUID.randomUUID();

	      webTestClient
	          .mutateWith(csrf())
	          .post()
	          .uri("/books/{bookId}/borrow", bookId)
	          .accept(MediaType.APPLICATION_JSON)
	          .exchange()
	          .expectStatus()
	          .isBadRequest();
	    }

	    @Test
	    @DisplayName("to return a borrowed book")
	    void verifyReturnBookUnAuthenticated() {

	      UUID bookId = UUID.randomUUID();

	      webTestClient
	          .mutateWith(csrf())
	          .post()
	          .uri("/books/{bookId}/return", bookId)
	          .accept(MediaType.APPLICATION_JSON)
	          .exchange()
	          .expectStatus()
	          .isForbidden();
	    }
   }
}
