/**
 * 
 */
package com.keycloak.admin.client.test.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import javax.annotation.PostConstruct;

import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Gbenga
 *
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@DisplayName("Verify book api")
@Tag("end2end")
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public abstract class KeycloakTestContainers {

	private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakTestContainers.class.getName());

	@LocalServerPort
	private int port;

	//protected static final KeycloakContainer keycloak = new KeycloakContainer().withRealmImportFile("keycloak/realm-export.json");

	@Container 
	protected static final KeycloakContainer keycloak = new KeycloakContainer().withRealmImportFile("keycloak/realm-export.json");
	
	private String authServerUrl;
	
	@BeforeEach
	void setup() {
	    authServerUrl = keycloak.getAuthServerUrl() + "/realms/test-realm/protocol/openid-connect/token";
	    RestAssured.baseURI = "http://localhost";
	    RestAssured.port = port;
	}
	
	@SuppressWarnings("unused")
    @DynamicPropertySource
    static void jwtValidationProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloak.getAuthServerUrl() + "/realms/test-realm");
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> keycloak.getAuthServerUrl() + "/realms/test-realm/protocol/openid-connect/certs");
    }
	
	protected String getToken() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.put("grant_type", Collections.singletonList("password"));
        map.put("client_id", Collections.singletonList("test-client"));
        map.put("client_secret", Collections.singletonList("NeHNb2cVaiSxTfS6jtlj4MVgOmxFYITA"));
        map.put("username", Collections.singletonList("janedoe"));
        map.put("password", Collections.singletonList("secret"));
        KeyCloakToken token =
                restTemplate.postForObject(
                        authServerUrl, new HttpEntity<>(map, httpHeaders), KeyCloakToken.class);

        assert token != null;
        return token.getAccessToken();
    }

	protected String getJaneDoeBearer() {

		try {
			URI authorizationURI = new URIBuilder(authServerUrl).build();
			WebClient webclient = WebClient.builder().build();
			MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
			formData.put("grant_type", Collections.singletonList("password"));
			formData.put("client_id", Collections.singletonList("test-client"));
			formData.put("username", Collections.singletonList("janedoe"));
			formData.put("password", Collections.singletonList("secret"));

			String result = webclient.post()
					.uri(authorizationURI)
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.body(BodyInserters.fromFormData(formData))
					.retrieve()
					.bodyToMono(String.class)
					.block();

			JacksonJsonParser jsonParser = new JacksonJsonParser();

			return "Bearer " + jsonParser.parseMap(result).get("access_token").toString();
		} catch (URISyntaxException e) {
			LOGGER.error("Can't obtain an access token from Keycloak!", e);
		}

		return null;
	}
	
	private static class KeyCloakToken {

	    private final String accessToken;

	    @JsonCreator
	    KeyCloakToken(@JsonProperty("access_token") final String accessToken) {
	      this.accessToken = accessToken;
	    }

	    public String getAccessToken() {
	      return accessToken;
	    }
    }
}