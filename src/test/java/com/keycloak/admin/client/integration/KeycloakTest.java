/**
 * 
 */
package com.keycloak.admin.client.integration;

import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.keycloak.admin.client.common.utils.RandomGenerator;
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;
import com.keycloak.admin.client.components.MyReactiveClient;
import com.keycloak.admin.client.config.AppConfiguration;
import com.keycloak.admin.client.config.AuthProfile;
import com.keycloak.admin.client.config.AuthProperties;
import com.keycloak.admin.client.config.KeycloakClientSslProperties;
import com.keycloak.admin.client.config.LoginNotificationConfig;
import com.keycloak.admin.client.config.MessageConfig;
import com.keycloak.admin.client.models.AuthenticationRequest;
import com.keycloak.admin.client.models.AuthenticationResponse;
import com.keycloak.admin.client.oauth.service.ActivationTokenServiceImpl;
import com.keycloak.admin.client.oauth.service.ActivityLogServiceImpl;
import com.keycloak.admin.client.oauth.service.CommonUtil;
import com.keycloak.admin.client.oauth.service.GatewayReactiveUserDetailsService;
import com.keycloak.admin.client.oauth.service.GatewayRedisCache;
import com.keycloak.admin.client.oauth.service.GroupServiceImpl;
import com.keycloak.admin.client.oauth.service.KeycloakOauthClient;
import com.keycloak.admin.client.oauth.service.KeycloakRestService;
import com.keycloak.admin.client.oauth.service.PasswordServiceImpl;
import com.keycloak.admin.client.oauth.service.RoleServiceImpl;
import com.keycloak.admin.client.oauth.service.UserAuthenticationServiceImpl;
import com.keycloak.admin.client.oauth.service.UserCredentialServiceImpl;
import com.keycloak.admin.client.oauth.service.UserLocationServiceImpl;
import com.keycloak.admin.client.oauth.service.it.KeycloakOauthClientService;
import com.keycloak.admin.client.test.config.KeycloakProperties;
import com.keycloak.admin.client.token.utils.KeycloakJwtTokenUtil;
import com.keycloak.admin.client.token.utils.TotpManagerImpl;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Testcontainers
@DirtiesContext
@Tag("integration")
@DisplayName("Admin api")
@ContextConfiguration(classes = { AppConfiguration.class, MessageConfig.class, LoginNotificationConfig.class,
		KeycloakTest.WebTestConfiguration.class })

@Import({ AuthProperties.class, AuthProfile.class, CustomMessageSourceAccessor.class, KeycloakRestService.class,
		GatewayRedisCache.class, TotpManagerImpl.class, MyReactiveClient.class, KeycloakOauthClient.class,
		KeycloakClientSslProperties.class, KeycloakProperties.class })

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = { MongoReactiveAutoConfiguration.class, MongoAutoConfiguration.class,
		MongoDataAutoConfiguration.class, EmbeddedMongoAutoConfiguration.class,
		MongoReactiveRepositoriesAutoConfiguration.class, MongoRepositoriesAutoConfiguration.class })
class KeycloakTest {

	@Autowired
	@Qualifier("keycloak-client")
	private KeycloakOauthClientService keycloakClientService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	@Qualifier("keycloak-webClient")
	private WebClient webClient;

	@Autowired
	private KeycloakProperties keycloakProperties;

	@MockBean
	private GatewayRedisCache redisCacheUtil;
	@MockBean
	private AuthProperties authProperties;

	private String tokenEndpointUrl;
	private String logoutEndpointUrl;
	private String userInfoEndpointUrl;
	private String revokeTokenEndpointUrl;

	@Container
	protected static final KeycloakContainer keycloakContainer = new KeycloakContainer()
			.withRealmImportFile("keycloak/realm-export.json")
			.withFeaturesEnabled("docker", "impersonation", "scripts", "token-exchange", "admin-fine-grained-authz")
			.withFeaturesDisabled("authorization");

	@SuppressWarnings("unused")
	@DynamicPropertySource
	static void jwtValidationProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
				() -> keycloakContainer.getAuthServerUrl() + "realms/myrealm");
		registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
				() -> keycloakContainer.getAuthServerUrl() + "realms/myrealm/protocol/openid-connect/certs");
	}

	/**
	 * 
	 */
	@ComponentScan(basePackages = {
			"com.keycloak.admin.client.oauth.service", }, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
					ActivationTokenServiceImpl.class, ActivityLogServiceImpl.class, UserLocationServiceImpl.class,
					UserCredentialServiceImpl.class, CommonUtil.class, KeycloakJwtTokenUtil.class,
					PasswordServiceImpl.class, UserAuthenticationServiceImpl.class, GroupServiceImpl.class,
					RoleServiceImpl.class, GatewayReactiveUserDetailsService.class }))
	@TestConfiguration
	public static class WebTestConfiguration {

		@Autowired
		private KeycloakProperties keycloakProperties;

		@Bean
		public PasswordEncoder passwordEncoder() {
			// with new spring security 5
			return PasswordEncoderFactories.createDelegatingPasswordEncoder();
		}

		@Bean("keycloak-webClient")
		public WebClient keycloackWebClient(WebClient.Builder webClientBuilder) throws SSLException {
			// String BASE_URL = keycloakContainer.getAuthServerUrl();
			// log.info("Keycloak Container Base Url {}", BASE_URL);

			// DefaultUriBuilderFactory uriBuilderFactory = new
			// DefaultUriBuilderFactory(BASE_URL);
			// uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.URI_COMPONENT);

			return webClientBuilder.build();
		}

		/**
		 * User "admin client" needs at least "manage-users, view-clients, view-realm,
		 * view-users" roles for "realm-management"
		 */
		@Bean
		public Keycloak keycloakAdminClientFactory(KeycloakClientSslProperties keycloakSslProps) throws Exception {
			String startingClientId = keycloakProperties.getAdminClientId();
			String startingClientSecret = keycloakProperties.getAdminClientSecret();
			String serverUrl = keycloakContainer.getAuthServerUrl();
			String realm = keycloakProperties.getAppRealm();

			log.info("Keycloak admin clientId...{}", startingClientId);
			log.info("Keycloak admin client-secret...{}", startingClientSecret);
			log.info("Keycloak server Url...{}", serverUrl);
			log.info("Keycloak realm...{}", realm);

			// Get keycloak client
			Keycloak keycloak = KeycloakBuilder.builder()
					//
					.serverUrl(serverUrl)
					//
					.realm(realm)
					//
					.clientId(startingClientId)
					//
					.clientSecret(startingClientSecret)
					//
					.grantType(OAuth2Constants.CLIENT_CREDENTIALS)
					//
					// .resteasyClient(resteasyClient2())
					//
					.build();

			// Keycloak keycloakAdmin = keycloakContainer.getKeycloakAdminClient();
			return keycloak;
		}
	}

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);

		tokenEndpointUrl = keycloakContainer.getAuthServerUrl() + "realms/myrealm/protocol/openid-connect/token";
		logoutEndpointUrl = keycloakContainer.getAuthServerUrl() + "realms/myrealm/protocol/openid-connect/logout";
		userInfoEndpointUrl = keycloakContainer.getAuthServerUrl() + "realms/myrealm/protocol/openid-connect/userinfo";
		revokeTokenEndpointUrl = keycloakContainer.getAuthServerUrl() + "realms/myrealm/protocol/openid-connect/revoke";
		log.info("Token endpoint Url {}", tokenEndpointUrl);

		when(authProperties.getTokenUrl()).thenReturn(tokenEndpointUrl);
		when(authProperties.getBaseUrl()).thenReturn(keycloakContainer.getAuthServerUrl());

		when(authProperties.getAdminClientId()).thenReturn(keycloakProperties.getAdminClientId());
		when(authProperties.getAdminClientSecret()).thenReturn(keycloakProperties.getAdminClientSecret());

		when(authProperties.getAppRealm()).thenReturn(keycloakProperties.getAppRealm());
		when(authProperties.getScope()).thenReturn("profile openid");

		when(authProperties.getKeycloakLogout()).thenReturn(logoutEndpointUrl);
		when(authProperties.getRevokeTokenUrl()).thenReturn(revokeTokenEndpointUrl);
		when(authProperties.getKeycloakUserInfo()).thenReturn(userInfoEndpointUrl);
	}


	/**
	 * User-info not working as expected
	 */
	@DisplayName("to test get user realm roles endpoint")
	@Test
	public void testThatGetUserRolesWorks() {

		Mono<AccessTokenResponse> tokenGen = login("sam", "secret");

		AccessTokenResponse token = tokenGen.block();
		String accessToken = token.getToken();
		log.info("Access Token generated {}", accessToken);

		Mono<List<String>> response = keycloakClientService.getUserRealmRoles(accessToken);

		List<String> userInfo = response.block();
		log.info("User Role: {}", userInfo);

		StepVerifier.create(response)
				.expectNextMatches(res -> CollectionUtils.isEmpty(res) /* && res.equalsIgnoreCase("ROLE_ADMIN") */)
				.verifyComplete();

		// =========================================================================================
	}

	/**
	 * 
	 */
	@DisplayName("to test password login grant works")
	@Test
	public void testThatPasswordLoginGrantWorks() {
		String username = "sam";
		String password = "secret";

		AuthenticationRequest request = AuthenticationRequest.builder().username(username).password(password).build();
		Mono<AuthenticationResponse> tokenGenerated = keycloakClientService.passwordGrantLogin(request);

		AuthenticationResponse response = tokenGenerated.block();
		log.info("Response: {} {}", response.getUsername(), response.getAccessToken());
		log.info("Refresh token: {}", response.getRefreshToken());

		StepVerifier.create(tokenGenerated).expectNextMatches(p -> StringUtils.isNotBlank(p.getAccessToken()))
				.verifyComplete();
	}
	
	/**
	 * 
	 */
	@DisplayName("to test revoke access token")
	@Test
	public void testThatRevokeAccessTokenWorks() {

		Mono<AccessTokenResponse> tokenGen = login("sam", "secret");

		AccessTokenResponse token = tokenGen.block();
		String accessToken = token.getToken();
		log.info("Access Token generated {}", accessToken);

		Mono<String> response = keycloakClientService.revokeAccessToken(accessToken);

		String responseMsg = response.block();
		log.info("Response: {}", responseMsg);

		StepVerifier.create(response).expectNextMatches(msg -> msg.equalsIgnoreCase("message.token-revocation.success"))
				.verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("to test mfa validation")
	@Disabled
	public void testThatMfaValidationWorks() {
		String username = "sam";
		String password = "secret";

		AuthenticationRequest request = AuthenticationRequest.builder().username(username).password(password).build();
		Mono<AuthenticationResponse> tokenGenerated = keycloakClientService.passwordGrantLogin(request);

		AuthenticationResponse authResponse = tokenGenerated.block();
		log.info("Response: {} {}", authResponse.getUsername(), authResponse.getAccessToken());
		log.info("Refresh token: {}", authResponse.getRefreshToken());

		String totpCode = RandomGenerator.randomCode(8);

		Mono<String> response = keycloakClientService.doMfaValidation(authResponse, totpCode);

		String responseMsg = response.block();
		log.info("Response: {}", responseMsg);

		StepVerifier.create(response).expectNextMatches(msg -> msg.equalsIgnoreCase("message.token-revocation.success"))
				.verifyComplete();
	}

	public Mono<AccessTokenResponse> login(final String username, final String password) {
		String startingClientId = "admin-client";
		String startingClientSecret = "wPTbmhWLBjP1A2vlMff4oARmm30ehj46";

		Map<String, List<String>> headers = new HashMap<>();
		headers.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("username", username);
		formData.add("password", password);
		formData.add("client_id", startingClientId);
		formData.add("client_secret", startingClientSecret);
		formData.add("grant_type", OAuth2Constants.PASSWORD);
		formData.add("scope", "profile openid");

		return performPostFormToMono(webClient, URI.create(tokenEndpointUrl), formData, AccessTokenResponse.class,
				headers, null);
	}

	private <T> Mono<T> performPostFormToMono(WebClient webClient, URI uri, MultiValueMap<String, String> formData,
			Class<? extends T> clazzResponse, Map<String, List<String>> headerFields,
			MultiValueMap<String, String> params) {

		return webClient.post().uri(uriBuilder -> uriBuilder.scheme(uri.getScheme()).host(uri.getHost())
				.port(uri.getPort()).path(uri.getPath()).queryParams(params).build()).headers(headers -> {
					if (headerFields != null) {
						headers.putAll(headerFields);
					}
				}).body(BodyInserters.fromFormData(formData))
				.exchangeToMono(clientResponse -> processResponse(clientResponse, clazzResponse));
	}

	private static <R> Mono<R> processResponse(ClientResponse clientResponse, Class<? extends R> clazzResponse) {
		HttpStatus status = clientResponse.statusCode();

		Mono<R> respObj = Mono.empty();

		if (status.is2xxSuccessful()) {
			respObj = clientResponse.bodyToMono(clazzResponse);

		} else if (status.isError()) {
			return clientResponse.createException().flatMap(ex -> Mono.error(ex));
		}

		return respObj;
	}
}
