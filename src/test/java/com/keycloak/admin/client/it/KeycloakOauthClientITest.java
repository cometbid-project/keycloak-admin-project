/**
 * 
 */
package com.keycloak.admin.client.it;

import com.keycloak.admin.client.test.config.KeycloakTestContainers;

import lombok.extern.log4j.Log4j2;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

/**
 * @author Gbenga
 *
 */
@Log4j2
//@ContextConfiguration(classes = { SecurityConfig.class })
class KeycloakOauthClientITest extends KeycloakTestContainers {
	
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}
	
	@AfterAll
	static void tireDown() {

		log.info("Tearing and stopping Keycloak container server");
		keycloak.stop();
	}


    @Test
    void givenAuthenticatedUser_whenGetMe_shouldReturnMyInfo() {

        given().header("Authorization", getJaneDoeBearer())
            .when()
            .get("/users/me")
            .then()
            .body("username", equalTo("janedoe"))
            .body("lastname", equalTo("Doe"))
            .body("firstname", equalTo("Jane"))
            .body("email", equalTo("jane.doe@gmail.com"));
    }

}
