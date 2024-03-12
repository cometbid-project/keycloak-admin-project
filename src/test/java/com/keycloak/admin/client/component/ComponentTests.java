/**
 * 
 */
package com.keycloak.admin.client.component;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import com.github.tomakehurst.wiremock.client.WireMock;

/**
 * @author Gbenga
 *
 */
//tag::code[]
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT) //<1>
@AutoConfigureWebTestClient // <2>
@ComponentTest
public class ComponentTests {
	
    @Autowired
    WebTestClient webTestClient;

    @BeforeAll
    void initStubs() {
    	
        WireMock.stubFor(post("/downstream/system").
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody("{\"status\": \"success\"}")
                .withBodyFile("ping-response.json")
            ));
    }

    @Test
    void shouldGetSuccessOnValidCalls() {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        WebTestClient.ResponseSpec response = webTestClient
            .post()
            .headers(httpHeaders -> httpHeaders.addAll(headers))
            .body(BodyInserters.fromValue("{\"key\":\"value\"}"))
            .exchange();
        
        response
            .expectStatus()
            .isEqualTo(HttpStatus.OK)
            .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE) 
            .expectBody(ApplicationResponseDTO.class)
            .consumeWith(serverResponse -> {
                ApplicationResponseDTO responseBody = serverResponse.getResponseBody();
                //all assertions go here

            });

    }
    
    class ApplicationResponseDTO {
    	
    }
}
