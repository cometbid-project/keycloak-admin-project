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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.BodyInserters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keycloak.admin.client.dataacess.RoleBuilder;
import com.keycloak.admin.client.models.RoleVO;
import com.keycloak.admin.client.oauth.service.it.GroupService;
import com.keycloak.admin.client.oauth.service.it.RoleService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = GroupApiAuthenticationTest.TestConfig.class)
@DisplayName("Access to book api")
class GroupApiAuthenticationTest {

	@Autowired private ApplicationContext applicationContext;

	  private WebTestClient webTestClient;

	  @MockBean private GroupService grpService;

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

	  @DisplayName("as authenticated user is granted")
	  @Nested
	  class AuthenticatedBookApi {

	    @WithMockUser
	    @Test
	    @DisplayName("to get list of books")
	    void verifyGetBooksAuthenticated() {

	      given(grpService.findAllRealmGroups()).willReturn(Flux.just(RoleBuilder.role().build()));

	      webTestClient
	          .get()
	          .uri("/books")
	          .accept(MediaType.APPLICATION_JSON)
	          .exchange()
	          .expectStatus()
	          .isOk()
	          .expectHeader()
	          .exists("X-XSS-Protection")
	          .expectHeader()
	          .valueEquals("X-Frame-Options", "DENY");
	    }

	    @Test
	    @DisplayName("to get single book")
	    void verifyGetBookAuthenticated() {

	      UUID bookId = UUID.randomUUID();

	      given(roleService.findRealmRoleByName(bookId))
	          .willReturn(Mono.just(RoleBuilder.role().withId(bookId).build()));

	      webTestClient
	          .mutateWith(mockUser())
	          .get()
	          .uri("/books/{bookId}", bookId)
	          .accept(MediaType.APPLICATION_JSON)
	          .exchange()
	          .expectStatus()
	          .isOk();
	    }

	    /*
	    @Test
	    @DisplayName("to delete a book")
	    void verifyDeleteBookAuthenticated() {

	      UUID bookId = UUID.randomUUID();
	      given(roleService.deleteById(bookId)).willReturn(Mono.empty());

	      webTestClient
	          .mutateWith(mockUser().roles("LIBRARY_CURATOR"))
	          .mutateWith(csrf())
	          .delete()
	          .uri("/books/{bookId}", bookId)
	          .accept(MediaType.APPLICATION_JSON)
	          .exchange()
	          .expectStatus()
	          .isOk();
	    }
	    */

	    @Test
	    @DisplayName("to borrow a book")
	    void verifyBorrowBookAuthenticated() {

	      UUID bookId = UUID.randomUUID();

	      webTestClient
	          .mutateWith(mockUser().roles("LIBRARY_USER"))
	          .mutateWith(csrf())
	          .post()
	          .uri("/books/{bookId}/borrow", bookId)
	          .accept(MediaType.APPLICATION_JSON)
	          .exchange()
	          .expectStatus()
	          .isOk()
	          .expectBody();
	    }

	    @Test
	    @DisplayName("to return a borrowed book")
	    void verifyReturnBookAuthenticated() {

	      UUID bookId = UUID.randomUUID();

	      webTestClient
	          .mutateWith(mockUser().roles("LIBRARY_USER"))
	          .mutateWith(csrf())
	          .post()
	          .uri("/books/{bookId}/return", bookId)
	          .accept(MediaType.APPLICATION_JSON)
	          .exchange()
	          .expectStatus()
	          .isOk()
	          .expectBody();
	    }

	    @Test
	    @DisplayName("to create a new book")
	    void verifyCreateBookAuthenticated() throws JsonProcessingException {

	      UUID bookId = UUID.randomUUID();
	      RoleVO expectedBook = RoleBuilder.role().withId(bookId).build(); 

	      BookResource bookResource =
	          new BookResource(
	              bookId,
	              expectedBook.getIsbn(),
	              expectedBook.getTitle(),
	              expectedBook.getDescription(),
	              expectedBook.getAuthors(),
	              expectedBook.isBorrowed(),
	              null);

	      given(roleService.create(any())).willAnswer(b -> Mono.empty());

	      webTestClient
	          .mutateWith(mockUser().roles("LIBRARY_CURATOR"))
	          .mutateWith(csrf())
	          .post()
	          .uri("/books")
	          .accept(MediaType.APPLICATION_JSON)
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(BodyInserters.fromValue(new ObjectMapper().writeValueAsString(bookResource)))
	          .exchange()
	          .expectStatus()
	          .isCreated();
	    }
	  }

	  @DisplayName("as unauthenticated user is denied with 401")
	  @Nested
	  class UnAuthenticatedBookApi {

	    @Test
	    @DisplayName("to get list of books")
	    void verifyGetBooksUnAuthenticated() {

	      webTestClient
	          .get()
	          .uri("/books")
	          .accept(MediaType.APPLICATION_JSON)
	          .exchange()
	          .expectStatus()
	          .isUnauthorized();
	    }

	    @Test
	    @DisplayName("to get single book")
	    void verifyGetBookUnAuthenticated() {

	      UUID bookId = UUID.randomUUID();

	      webTestClient
	          .get()
	          .uri("/books/{bookId}", bookId)
	          .accept(MediaType.APPLICATION_JSON)
	          .exchange()
	          .expectStatus()
	          .isUnauthorized();
	    }

	    @Test
	    @DisplayName("to delete a book")
	    void verifyDeleteBookUnAuthenticated() {

	      UUID bookId = UUID.randomUUID();

	      webTestClient
	          .mutateWith(csrf())
	          .delete()
	          .uri("/books/{bookId}", bookId)
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
	          .isUnauthorized();
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
	          .isUnauthorized();
	    }

	    @Test
	    @DisplayName("to create a new book")
	    void verifyCreateBookUnAuthenticated() throws JsonProcessingException {

	      UUID bookId = UUID.randomUUID();
	      Book expectedBook = BookBuilder.book().withId(bookId).build();

	      BookResource bookResource =
	          new BookResource(
	              bookId,
	              expectedBook.getIsbn(),
	              expectedBook.getTitle(),
	              expectedBook.getDescription(),
	              expectedBook.getAuthors(),
	              expectedBook.isBorrowed(),
	              null);

	      given(roleService.create(any())).willAnswer(b -> Mono.empty());

	      webTestClient
	          .mutateWith(csrf())
	          .post()
	          .uri("/books")
	          .accept(MediaType.APPLICATION_JSON)
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(BodyInserters.fromValue(new ObjectMapper().writeValueAsString(bookResource)))
	          .exchange()
	          .expectStatus()
	          .isUnauthorized();
	    }
	  }
}
