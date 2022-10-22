/**
 * 
 */
package com.keycloak.admin.client.it;

import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

/**
 * @author Gbenga
 *
 */
//tag::code[]
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT) //<1>
@AutoConfigureWebTestClient // <2>
class KeycloakRestServiceITest {

}
