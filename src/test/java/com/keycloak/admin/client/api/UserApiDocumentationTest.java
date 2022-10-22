/**
 * 
 */
package com.keycloak.admin.client.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keycloak.admin.client.common.enums.Role;
import com.keycloak.admin.client.controllers.UserController;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.models.PagingModel;
import com.keycloak.admin.client.models.UserRegistrationRequest;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.oauth.service.it.UserCredentialFinderService;
import com.keycloak.admin.client.oauth.service.it.UserCredentialService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

/**
 * @author Gbenga
 *
 */
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@WebFluxTest
@Import({
  //UserRouter.class,
  //UserHandler.class,
  UserController.class,
  //ModelMapperConfiguration.class,
  //IdGeneratorConfiguration.class
})
@WithMockUser
@DisplayName("Verify user api")
class UserApiDocumentationTest {

  @Autowired private ApplicationContext applicationContext;

  private WebTestClient webTestClient;

  @MockBean private UserCredentialFinderService userFinderService;
  
  @MockBean private UserCredentialService userService;


  /**
   * 
   * @param restDocumentation
   */
  @BeforeEach
  void setUp(RestDocumentationContextProvider restDocumentation) {
	  
    this.webTestClient =
        WebTestClient.bindToApplicationContext(applicationContext)
            .configureClient()
                .baseUrl("http://localhost:9091")
                .filter(
                documentationConfiguration(restDocumentation)
                    .operationPreprocessors()
                    .withRequestDefaults(prettyPrint())
                    .withResponseDefaults(prettyPrint()))
            .build();
  }

  
  /**
   * 
   */
  @Test
  @DisplayName("to get list of users")
  void verifyAndDocumentGetUsers() {

    UUID userId = UUID.randomUUID();
    PagingModel pagingModel = PagingModel.builder().pgNo(0).pgSize(10).build();
    
    UserVO expectedUser = UserBuilder.user().userVo(userId);
    
    given(userFinderService.findAll(pagingModel)).willReturn(Flux.just(expectedUser));

    webTestClient
        .get()
        .uri("/users")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .json(
            "[{\"id\":\""
                + userId
                + "\",\"email\":\"john.doe@example.com\","
                + "\"firstName\":\"John\",\"lastName\":\"Doe\"}]")
        .consumeWith(
            document(
                "get-users", 
                preprocessRequest(prettyPrint()), 
                preprocessResponse(prettyPrint())));
  }

  /**
   * 
   */
  @Test
  @DisplayName("to get single user")
  void verifyAndDocumentGetUser() {

    UUID userId = UUID.randomUUID();
    
    UserVO expectedUser = UserBuilder.user().userVo(userId);

    given(userFinderService.findUserById(userId.toString()))
        .willReturn(Mono.just(expectedUser));

    webTestClient
        .get()
        .uri("/users/{userId}", userId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .json(
            "{\"id\":\""
                + userId
                + "\",\"email\":\"john.doe@example.com\","
                + "\"firstName\":\"John\",\"lastName\":\"Doe\","
                + "\"roles\":[\"ADMIN\"]}")
        .consumeWith(
            document(
                "get-user", 
                preprocessRequest(prettyPrint()), 
                preprocessResponse(prettyPrint())));
  }

  /*
  @Test
  @DisplayName("to delete a user")
  void verifyAndDocumentDeleteUser() {

    UUID userId = UUID.randomUUID();
    given(userService.deleteById(userId)).willReturn(Mono.empty());

    webTestClient
        .mutateWith(csrf())
        .delete()
        .uri("/users/{userId}", userId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .consumeWith(
            document(
                "delete-user",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint())));
  }
  */

  /**
   * 
   * @throws JsonProcessingException
   */
  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("to create a new user")
  void verifyAndDocumentCreateUser() throws JsonProcessingException {

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
	        .body(BodyInserters.fromValue(new ObjectMapper().writeValueAsString(userResource)))
	        .exchange()
	        .expectStatus()
	        .isOk()
	        .expectBody()
	        .consumeWith(
	            document(
	                "create-user",
	                preprocessRequest(prettyPrint()),
	                preprocessResponse(prettyPrint())));
	
	    ArgumentCaptor<UserRegistrationRequest> userReqArg = ArgumentCaptor.forClass(UserRegistrationRequest.class);
	    
	    verify(userService).signupUser(any(UserRegistrationRequest.class), 
	    		any(Role.class), any(ServerRequest.class));
	
	    assertThat(userReqArg.getValue()).isNotNull();
	    assertThat(userReqArg.getValue().getDisplayName()).isEqualTo(expectedUser.getDisplayName());
	    assertThat(userReqArg.getValue().getEmail()).isEqualTo(expectedUser.getEmail());
  }
  
}
