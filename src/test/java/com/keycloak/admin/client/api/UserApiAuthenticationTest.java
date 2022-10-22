/**
 * 
 */
package com.keycloak.admin.client.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keycloak.admin.client.common.enums.Role;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.models.PagingModel;
import com.keycloak.admin.client.models.UserRegistrationRequest;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.oauth.service.it.UserCredentialFinderService;
import com.keycloak.admin.client.oauth.service.it.UserCredentialService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

/**
 * @author Gbenga
 *
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = UserApiAuthenticationTest.TestConfig.class)
@DisplayName("Access to user api")
class UserApiAuthenticationTest {

  @ComponentScan(
          basePackages = {
                  "com.example.library.server.api",
                  "com.example.library.server.business",
                  "com.example.library.server.config"
          })
  @EnableWebFlux
  @EnableWebFluxSecurity
  @EnableAutoConfiguration(
          exclude = {
                  MongoReactiveAutoConfiguration.class,
                  MongoAutoConfiguration.class,
                  MongoDataAutoConfiguration.class,
                  EmbeddedMongoAutoConfiguration.class,
                  MongoReactiveRepositoriesAutoConfiguration.class,
                  MongoRepositoriesAutoConfiguration.class
          })
  static class TestConfig {}

  @Autowired private ApplicationContext applicationContext;

  private WebTestClient webTestClient;

  @MockBean private UserCredentialService userService;
  
  @MockBean private UserCredentialFinderService userFinderService;

  
  @BeforeEach
  void setUp() {
    this.webTestClient =
        WebTestClient.bindToApplicationContext(applicationContext)
            .apply(springSecurity())
            .configureClient()
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
	   * 
	   */
    @Test
    @DisplayName("to get list of users")
    void verifyGetUsersAuthenticated() {

      UUID userId = UUID.randomUUID();
      UserVO expectedUser = UserBuilder.user().userVo(userId);
      
      PagingModel pagingModel = PagingModel.builder().pgNo(0).pgSize(10).build();
      
      given(userFinderService.findAll(pagingModel)).willReturn(Flux.just(expectedUser));

      webTestClient
          .mutateWith(mockUser().roles("LIBRARY_ADMIN"))
          .get()
          .uri("/users")
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus()
          .isOk();
    }

    /**
     * 
     */
    @Test
    @DisplayName("to get single user")
    void verifyGetUserAuthenticated() {

      UUID userId = UUID.randomUUID();
      UserVO expectedUser = UserBuilder.user().userVo(userId);

      given(userFinderService.findUserById(userId.toString()))
          .willReturn(Mono.just(expectedUser));

      webTestClient
          .mutateWith(mockUser().roles("LIBRARY_ADMIN"))
          .get()
          .uri("/users/{userId}", userId)
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus()
          .isOk();
    }

    /*
    @Test
    @DisplayName("to delete a user")
    void verifyDeleteUserAuthenticated() {

      UUID userId = UUID.randomUUID();
      given(userService.deleteById(userId)).willReturn(Mono.empty());

      webTestClient
          .mutateWith(mockUser().roles("LIBRARY_ADMIN"))
          .mutateWith(csrf())
          .delete()
          .uri("/users/{userId}", userId)
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus()
          .isOk();
    }
    */

    /**
     * 
     * @throws JsonProcessingException
     */
    @Test
    @DisplayName("to create a new user")
    void verifyCreateUserAuthenticated() throws JsonProcessingException {

    	UUID userId = UUID.randomUUID();
    	
	    UserVO expectedUser = UserBuilder.user().userVo(userId);
	
	    UserRegistrationRequest userResource = UserBuilder.user().build();
	
	    given(userService.signupUser(any(UserRegistrationRequest.class), 
	    		any(Role.class), any(ServerRequest.class))).willAnswer(i -> Mono.just(expectedUser));

      webTestClient
          .mutateWith(mockUser().roles("LIBRARY_ADMIN"))
          .mutateWith(csrf())
          .post()
          .uri("/users")
          .accept(MediaType.APPLICATION_JSON)
          .contentType(MediaType.APPLICATION_JSON)
          .body(BodyInserters.fromValue(new ObjectMapper().writeValueAsString(userResource)))
          .exchange()
          .expectStatus()
          .isOk();
    }
  }

  @DisplayName("as unauthenticated user is denied")
  @Nested
  class UnAuthenticatedUserApi {

	  /**
	   * 
	   */
    @Test
    @DisplayName("to get list of users")
    void verifyGetUsersUnAuthenticated() {

      webTestClient
          .get()
          .uri("/users")
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus()
          .isUnauthorized();
    }

    /**
     * 
     */
    @Test
    @DisplayName("to get single user")
    void verifyGetUserUnAuthenticated() {

      UUID userId = UUID.randomUUID();

      webTestClient
          .get()
          .uri("/users/{userId}", userId)
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

      webTestClient
          .mutateWith(csrf())
          .delete()
          .uri("/users/{userId}", userId)
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
    	
	    UserVO expectedUser = UserBuilder.user().userVo(userId);
	
	    UserRegistrationRequest userResource = UserBuilder.user().build();
	
	    given(userService.signupUser(any(UserRegistrationRequest.class), 
	    		any(Role.class), any(ServerRequest.class))).willAnswer(i -> Mono.just(expectedUser));

      webTestClient
          .mutateWith(csrf())
          .post()
          .uri("/users")
          .accept(MediaType.APPLICATION_JSON)
          .contentType(MediaType.APPLICATION_JSON)
          .body(BodyInserters.fromValue(new ObjectMapper()
        		  .writeValueAsString(userResource)))
          .exchange()
          .expectStatus()
          .isUnauthorized();
    }
  }
}
