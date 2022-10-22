package com.keycloak.admin.client.it;

import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;

/**
 * 
 * @author Gbenga
 *
 */
@SpringBootTest
class KeycloakAdminRoleITests {
	
	@Autowired private ApplicationRouter applicationRouter;
	@Autowired private ApplicationHandler applicationHandler;
	@Autowired private ErrorHandler errorHandler;
	@Autowired private ServiceLogger serviceLogger;
	@MockBean private DbService dbService;

	/**
	 * 
	 */
	@BeforeEach
	void mockCouch() {
		when(databaseService.findById(any())).thenReturn(
				Mono.just(JsonDocument.create("Document_key", JsonObject.fromJson("{your_json_response}"))));
	}
	
	@Test
	@DisplayName("Should Get Success on valid request")
	public void validRequestIntegrationTest() {
	    
	    WebTestClient
	        .bindToRouterFunction(
	            applicationRouter.mainRouterFunction(applicationHandler, errorHandler, serviceLogger))
	        .build()
	        .post()
	        .uri("/base-path")
	        .accept(MediaType.APPLICATION_JSON)
	        .contentType(MediaType.APPLICATION_JSON)
	        .exchange()
	        .expectStatus()
	        .isOk()
	        .expectBody(ApplicationResponseDTO.class)
	        .consumeWith(serverResponse ->
	            assertNotNull(serverResponse.getResponseBody()));
	}

	@Test
	void contextLoads() {
	}

	@Test
	public void testCreateRealmRole() {

	}

	@Test
	public void testCreateClientRole() {

	}

	@Test
	public void testMakeRealmRoleComposite() {

	}

	@Test
	public void testMakeClientRoleComposite() {

	}

	@Test
	public void testFindAllRealmRole() {

	}

	@Test
	public void testFindAllClientRole() {

	}

	@Test
	public void testFindRealmRoleByName() {

	}

	@Test
	public void testFindClientRoleByName() {

	}

	@Test
	public void testMakeRealmCompositeWithClientRole() {

	}
}
