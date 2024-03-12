/**
 * 
 */
package com.keycloak.admin.client.integration;

import static com.keycloak.admin.client.error.handlers.ExceptionHandler.processResponse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.UserInfo;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
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
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.keycloak.admin.client.models.SearchUserRequest;
import com.keycloak.admin.client.models.StatusUpdateRequest;
import com.keycloak.admin.client.common.enums.SocialProvider;
import com.keycloak.admin.client.common.enums.StatusType;
import com.keycloak.admin.client.common.utils.RandomGenerator;
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;
import com.keycloak.admin.client.components.MyReactiveClient;
import com.keycloak.admin.client.config.AppConfiguration;
import com.keycloak.admin.client.config.AuthProfile;
import com.keycloak.admin.client.config.AuthProperties;
import com.keycloak.admin.client.config.KeycloakClientSslProperties;
import com.keycloak.admin.client.config.LoginNotificationConfig;
import com.keycloak.admin.client.config.MessageConfig;
import com.keycloak.admin.client.exceptions.ResourceNotFoundException;
import com.keycloak.admin.client.exceptions.UserProfileUnverifiedException;
import com.keycloak.admin.client.filters.LogFilters;
import com.keycloak.admin.client.filters.WebClientFilters;
import com.keycloak.admin.client.models.AuthenticationRequest;
import com.keycloak.admin.client.models.AuthenticationResponse;
import com.keycloak.admin.client.models.PagingModel;
import com.keycloak.admin.client.models.PasswordUpdateRequest;
import com.keycloak.admin.client.models.ProfileActivationUpdateRequest;
import com.keycloak.admin.client.models.SocialLink;
import com.keycloak.admin.client.models.UserDetailsUpdateRequest;
import com.keycloak.admin.client.models.UserVO;
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
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;
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
		KeycloakIntegration.WebTestConfiguration.class })

@Import({ AuthProperties.class, AuthProfile.class, CustomMessageSourceAccessor.class, KeycloakRestService.class,
		GatewayRedisCache.class, TotpManagerImpl.class, MyReactiveClient.class, KeycloakOauthClient.class,
		KeycloakClientSslProperties.class, KeycloakProperties.class })

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = { MongoReactiveAutoConfiguration.class, MongoAutoConfiguration.class,
		MongoDataAutoConfiguration.class, 
		MongoReactiveRepositoriesAutoConfiguration.class, MongoRepositoriesAutoConfiguration.class })
class KeycloakIntegration {

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
			
			webClientBuilder.filter(LogFilters.logRequest());
			webClientBuilder.filter(LogFilters.logResponse());
			webClientBuilder.filter(WebClientFilters.tracingFilter());
			
			webClientBuilder.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE);
			//webClientBuilder.defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT);

			webClientBuilder.exchangeStrategies(ExchangeStrategies.builder()
					.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(30 * 1024 * 1024)).build());

			return webClientBuilder.clientConnector(getConnector()).build();
		}

		private ReactorClientHttpConnector getConnector() throws SSLException {

			Integer connectTimeoutInMillis = 30000;
			Integer readTimeoutInMillis = 30000;
			Integer writeTimeoutInMillis = 30000;
			
			final ConnectionProvider theTcpClientPool = ConnectionProvider.create("tcp-client-pool"); 
			final LoopResources theTcpClientLoopResources = LoopResources.create("tcp-client-loop", 100, true);

			HttpClient httpClient = HttpClient.create(theTcpClientPool).compress(true)
					.secure(sslContextSpec -> sslContextSpec.sslContext(noSecureSSL())
							.handshakeTimeout(Duration.ofSeconds(30)).closeNotifyFlushTimeout(Duration.ofSeconds(10))
							.closeNotifyReadTimeout(Duration.ofSeconds(10)))
					// configure a response timeout
					.responseTimeout(Duration.ofSeconds(3))
					.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutInMillis)
					.option(ChannelOption.SO_KEEPALIVE, true)				
					.runOn(theTcpClientLoopResources).doOnConnected(connection -> {
						// set the read and write timeouts
						connection.addHandlerLast(
								new ReadTimeoutHandler(readTimeoutInMillis, TimeUnit.MILLISECONDS));
						connection.addHandlerLast(
								new WriteTimeoutHandler(writeTimeoutInMillis, TimeUnit.MILLISECONDS));
					});

			return new ReactorClientHttpConnector(httpClient);
		}
		
		private SslContext noSecureSSL() {

			try {
				return SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
			} catch (SSLException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				throw new RuntimeException(e);
			}
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
					.resteasyClient(resteasyClient2())
					//
					.build();

			// Keycloak keycloakAdmin = keycloakContainer.getKeycloakAdminClient();
			return keycloak;
		}
		
		private ResteasyClient resteasyClient2() throws InterruptedException {
			Integer connectionPoolSize = keycloakProperties.getConnectionPoolSize();
			Integer readTimeoutInMillis = keycloakProperties.getReadTimeoutInMillis();
			Integer connectTimeoutInMillis = keycloakProperties.getConnectTimeoutInMillis();
			Integer connectionTTLInSecs = keycloakProperties.getConnectTTLInSeconds();
			Integer checkoutInMillis = keycloakProperties.getConnectCheckoutTimeoutInMillis();
					
			ResteasyClient resteasyClient = new ResteasyClientBuilderImpl()
				      .connectTimeout(connectTimeoutInMillis, TimeUnit.MILLISECONDS)
				      .readTimeout(readTimeoutInMillis, TimeUnit.MILLISECONDS)
				      .connectionPoolSize(connectionPoolSize)
				      .connectionTTL(connectionTTLInSecs, TimeUnit.SECONDS)
				      .connectionCheckoutTimeout(checkoutInMillis, TimeUnit.SECONDS)
				      .executorService(keycloackWorkerExecutor()) 
				      //.trustStore(trustStore)
				      .hostnameVerification(ResteasyClientBuilder.HostnameVerificationPolicy.ANY)
				      .build();
			
			return resteasyClient;
		}	
		
		/**
		 * 
		 * @return
		 * @throws InterruptedException
		 */
		private ExecutorService keycloackWorkerExecutor() throws InterruptedException {

			BasicThreadFactory customThreadfactory = new BasicThreadFactory.Builder()
					.namingPattern("Keycloak-Client-thread-%d")
					.daemon(false)
					.priority(Thread.MAX_PRIORITY)
					.uncaughtExceptionHandler(new UncaughtExceptionHandler() {
						@Override
						public void uncaughtException(Thread t, Throwable e) {

							log.error(String.format("Thread %s threw exception - %s", t.getName(), e.getMessage()), e);
						}
					}).build();

			int awaitTerminationInMillis = 20000;
			int threadSize = 2;

			ExecutorService executorService = Executors.newFixedThreadPool(threadSize, customThreadfactory);
			executorService.awaitTermination(awaitTerminationInMillis, TimeUnit.MILLISECONDS);

			return executorService;
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
	 * 
	 */
	@DisplayName("to test generate token works")
	@Disabled
	public void testThatGenerateTokenWorks() {
		String username = "janedoe";
		String targetClientId = "client-a";

		Mono<AuthenticationResponse> tokenGenerated = keycloakClientService.generateToken(username, targetClientId);

		StepVerifier.create(tokenGenerated).expectNextMatches(p -> StringUtils.isBlank(p.getAccessToken()))
				.verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("to test create user works")
	@Disabled
	public void testThatCreateOauthUserWorks() {
		// correct email
		String emailAddr = "jane.doe@gmail.com";
		String newPassword = RandomGenerator.generateRandomPassword();

		SocialLink socialLink = new SocialLink("12345", SocialProvider.GOOGLE);
		String[] roles = { "ROLE_ADMIN" };

		UserRepresentation userRepresentation = new UserRepresentation();
		userRepresentation.setId("8af75f52-d4ff-4f49-abf8-3df44d9deea5");
		userRepresentation.setUsername("janedoe");
		userRepresentation.setFirstName("Jane");
		userRepresentation.setLastName("Doe");
		userRepresentation.setEmail(emailAddr);
		userRepresentation.setRealmRoles(Arrays.asList(roles));

		Mono<String> response = keycloakClientService.createOauthUser(userRepresentation, newPassword, socialLink);

		StepVerifier.create(response).expectNextMatches(msg -> StringUtils.isNotBlank(msg)).verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("to test get all users")
	@Test
	public void testThatGetUserListWorks() {
		Flux<UserRepresentation> fluxUsers = keycloakClientService.findAllUsers();

		fluxUsers.toStream().forEach(user -> log.info("User: {} {}", user.getUsername(), user.getFirstName()));

		StepVerifier.create(fluxUsers).expectNextCount(2).verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("to test get all users with pagination")
	@Test
	public void testThatGetUserListWithPaginationWorks() {
		PagingModel pagingModel = PagingModel.builder().pageNo(1).pageSize(10).build();
		Mono<List<UserRepresentation>> usersList = keycloakClientService.findAllUsers(pagingModel);

		List<UserRepresentation> listUsers = usersList.block();
		listUsers.stream().forEach(user -> log.info("User: {} {}", user.getUsername(), user.getFirstName()));

		StepVerifier.create(usersList).expectNextMatches(users -> users.size() == 2).verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("to test search users works")
	@Test
	public void testThatSearchUserWorks() {
		// correct username
		String username = "sam";
		String lastName = "ADEBOWALE";
		String firstName = "SAMUEL";
		String email = "gbenga.java@gmail.com";
		// String targetClientId = "test-client";

		PagingModel pagingModel = PagingModel.builder().pageNo(1).pageSize(10).build();
		SearchUserRequest searchRequest = SearchUserRequest.builder().username(username).lastName(lastName)
				.firstName(firstName).email(email).emailVerified(true).build(); 

		Mono<List<UserRepresentation>> usersList = keycloakClientService.search(pagingModel, searchRequest);

		List<UserRepresentation> listUsers = usersList.block();
		;
		listUsers.stream().forEach(user -> log.info("Test search users&&User: {} {}", user.getUsername(), user.getFirstName()));

		StepVerifier.create(usersList).expectNextMatches(users -> users.size() == 1).verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("to test search users by email works")
	@Test
	public void testThatSearchUserByEmailWorks() {
		// correct email
		String emailAddr = "jane.doe@gmail.com";

		PagingModel pagingModel = PagingModel.builder().pageNo(1).pageSize(10).build();

		Mono<List<UserRepresentation>> usersList = keycloakClientService.findUserByEmail(emailAddr, pagingModel);

		List<UserRepresentation> listUsers = usersList.block();
		listUsers.stream().forEach(user -> log.info("User: {} {}", user.getUsername(), user.getFirstName()));

		StepVerifier.create(usersList).expectNextMatches(users -> users.size() == 1).verifyComplete();

		// ========================================================================================

		// correct email
		emailAddr = "gbenga.java@gmail.com";

		pagingModel = PagingModel.builder().pageNo(1).pageSize(10).build();

		usersList = keycloakClientService.findUserByEmail(emailAddr, pagingModel);

		listUsers = usersList.block();
		listUsers.stream().forEach(user -> log.info("User: {} {}", user.getUsername(), user.getFirstName()));

		StepVerifier.create(usersList).expectNextMatches(users -> users.size() == 1).verifyComplete();

		// ========================================================================================

		// Partial email
		emailAddr = "gbenga.java";

		pagingModel = PagingModel.builder().pageNo(1).pageSize(10).build();

		usersList = keycloakClientService.findUserByEmail(emailAddr, pagingModel);

		listUsers = usersList.block();
		listUsers.stream().forEach(user -> log.info("User: {} {}", user.getUsername(), user.getFirstName()));

		StepVerifier.create(usersList).expectNextMatches(users -> users.size() == 1).verifyComplete();

		// ========================================================================================

		// Partial email
		emailAddr = "gmail.com";

		pagingModel = PagingModel.builder().pageNo(1).pageSize(10).build();
 
		usersList = keycloakClientService.findUserByEmail(emailAddr, pagingModel);

		listUsers = usersList.block();		
		listUsers.stream().forEach(user -> log.info("Test search users by email&&User: {} {}", user.getUsername(), user.getFirstName()));

		StepVerifier.create(usersList).expectNextMatches(users -> CollectionUtils.isEmpty(users)).verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("to test get a user by firstname works")
	@Test
	void testThatGetUserByFirstNameWorks() {

		// correct email
		String firstName = "SAMueL";
		Mono<List<UserRepresentation>> usersList = keycloakClientService.findUserByFirstName(firstName);

		List<UserRepresentation> listUsers = usersList.block();
		listUsers.stream().forEach(user -> log.info("User: {} {}", user.getUsername(), user.getFirstName()));

		StepVerifier.create(usersList).expectNextMatches(
				user -> StringUtils.isNotBlank(user.get(0).getUsername()) && user.get(0).getUsername().equals("sam"))
				.verifyComplete();

		// =============================================================================================

		// correct email
		firstName = "Jane";
		usersList = keycloakClientService.findUserByFirstName(firstName);

		listUsers = usersList.block();
		listUsers.stream().forEach(user -> log.info("User: {} {}", user.getUsername(), user.getFirstName()));

		StepVerifier.create(usersList).expectNextMatches(user -> StringUtils.isNotBlank(user.get(0).getUsername())
				&& user.get(0).getUsername().equals("janedoe")).verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("to test get a user by lastname works")
	@Test
	void testThatGetUserByLastNameWorks() {

		// correct email
		String lastName = "ADEBOWALE";
		Mono<List<UserRepresentation>> usersList = keycloakClientService.findUserByLastName(lastName);

		List<UserRepresentation> listUsers = usersList.block();
		listUsers.stream().forEach(user -> log.info("User: {} {}", user.getUsername(), user.getFirstName()));

		StepVerifier.create(usersList).expectNextMatches(
				user -> StringUtils.isNotBlank(user.get(0).getUsername()) && user.get(0).getUsername().equals("sam"))
				.verifyComplete();

		// ==================================================================================================

		// correct email
		lastName = "Doe";
		usersList = keycloakClientService.findUserByLastName(lastName);

		listUsers = usersList.block();
		listUsers.stream().forEach(user -> log.info("User: {} {}", user.getUsername(), user.getFirstName()));

		StepVerifier.create(usersList).expectNextMatches(user -> StringUtils.isNotBlank(user.get(0).getUsername())
				&& user.get(0).getUsername().equals("janedoe")).verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("to test get a user by email works")
	@Test
	void testThatGetUserByEmailWorks() {

		// correct email
		String emailAddr = "jane.doe@gmail.com";
		Mono<UserRepresentation> userRep = keycloakClientService.findUserByEmail(emailAddr);

		UserRepresentation user = userRep.block();
		log.info("User: {} {}", user.getUsername(), user.getFirstName());

		StepVerifier.create(userRep).expectNextMatches(myUser -> StringUtils.isNotBlank(myUser.getId()))
				.verifyComplete();

		// =============================================================================================

		// correct email
		emailAddr = "gbenga.java@gmail.com";
		userRep = keycloakClientService.findUserByEmail(emailAddr);

		user = userRep.block();
		log.info("User: {} {}", user.getUsername(), user.getFirstName());

		StepVerifier.create(userRep).expectNextMatches(myUser -> StringUtils.isNotBlank(myUser.getId()))
				.verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("to test update email status by username")
	@Test
	public void testThatUpdateEmailStatusWorks() {
		String username = "janedoe";
		boolean setToVerified = true;
		Mono<String> response = keycloakClientService.updateEmailStatus(username, setToVerified);

		String responseMsg = response.block();
		log.info("Response: {}", responseMsg);

		StepVerifier.create(response).expectNextMatches(msg -> msg.equalsIgnoreCase("auth.message.success"))
				.verifyComplete();

		// =================================================================================================

		username = "janedoe";
		setToVerified = false;
		response = keycloakClientService.updateEmailStatus(username, setToVerified);

		responseMsg = response.block();
		log.info("Response: {}", responseMsg);

		StepVerifier.create(response).expectNextMatches(msg -> msg.equalsIgnoreCase("auth.message.success"))
				.verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("to test search user with username")
	@Test
	void testSearchUserByUsernameWorks() {
		// correct username
		String username = "sam";

		Mono<UserRepresentation> userFound = keycloakClientService.findUserByUsername(username);

		UserRepresentation userRep = userFound.block();
		log.info("User rep {}", userRep);

		StepVerifier.create(userFound)
				.expectNextMatches(
						user -> StringUtils.isNotBlank(user.getUsername()) && user.getUsername().equals("sam"))
				.verifyComplete();

		// correct username with unverified Email
		username = "janedoe";

		userFound = keycloakClientService.findUserByUsername(username);

		StepVerifier.create(userFound).expectError(UserProfileUnverifiedException.class);
	}

	/**
	 * 
	 * @throws InterruptedException
	 */
	@Test
	void adminGenerateToken() throws InterruptedException {

		String startingClientId = "admin-client";
		String startingClientSecret = "wPTbmhWLBjP1A2vlMff4oARmm30ehj46";
		String serverUrl = keycloakContainer.getAuthServerUrl();

		Keycloak kc = KeycloakBuilder.builder()
				//
				.serverUrl(serverUrl)
				//
				.realm("myrealm")
				//
				.clientId(startingClientId)
				//
				.clientSecret(startingClientSecret)
				//
				.grantType(OAuth2Constants.CLIENT_CREDENTIALS)
				//
				.scope("profile")
				//
				.build();

		assertNotNull(kc);

		// Keycloak keycloakAdmin = keycloakContainer.getKeycloakAdminClient();
		AccessTokenResponse token = kc.tokenManager().getAccessToken();
		String accessToken = token.getToken();
		log.info("Token generated {}", accessToken);

		assert StringUtils.isNotBlank(accessToken);
	}
	
	/**
	 * 
	 * @throws InterruptedException
	 */
	@Test
	void adminTokenExchange() throws InterruptedException {

		String startingClientId = "admin-client";
		String startingClientSecret = "wPTbmhWLBjP1A2vlMff4oARmm30ehj46";

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);

		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("client_id", startingClientId);
		formData.add("client_secret", startingClientSecret);
		formData.add("grant_type", OAuth2Constants.CLIENT_CREDENTIALS);
		formData.add("scope", "profile");

		Mono<AccessTokenResponse> tokenGen = performPostFormToMono(webClient, URI.create(tokenEndpointUrl), formData,
				AccessTokenResponse.class, headers, null);

		AccessTokenResponse token = tokenGen.block();
		String accessToken = token.getToken();
		log.info("Token generated for exchange {}", accessToken);

		/*
		 * String username = "sam"; String targetClientId = "client-a";
		 * 
		 * Mono<AccessTokenResponse> tokenExchangedGenerated =
		 * doTokenExchange(accessToken, startingClientId, startingClientSecret,
		 * username, targetClientId);
		 * 
		 * AccessTokenResponse tokenExchanged = tokenExchangedGenerated.block();
		 * log.info("Token exchanged {}", tokenExchanged.getToken());
		 */
	}
	
	/**
	 * User-info not working as expected
	 */
	@DisplayName("to test get user client roles endpoint")
	@Test
	public void testThatGetUserClientRolesWorks() {

		Mono<AccessTokenResponse> tokenGen = login("sam", "secret");

		AccessTokenResponse token = tokenGen.block();
		String accessToken = token.getToken();
		log.info("Access Token generated {}", accessToken);

		Mono<Map<String, List<String>>> response = keycloakClientService.getUserClientRoles(accessToken);

		Map<String, List<String>> userInfo = response.block();
		log.info("User Role: {}", userInfo);

		StepVerifier.create(response).expectNextMatches(res -> MapUtils.isEmpty(res)).verifyComplete();

		// =========================================================================================
	}


	/**
	 * 
	 */
	@DisplayName("to test signout")
	@Test
	public void testThatSignoutWorks() {
		String username = "janedoe";
		// String targetClientId = "client-a";

		Mono<AccessTokenResponse> tokenGen = login("sam", "secret");

		AccessTokenResponse token = tokenGen.block();
		String refreshToken = token.getRefreshToken();
		log.info("Refresh Token generated {}", refreshToken);

		Mono<String> response = keycloakClientService.signout(username, refreshToken).thenReturn("SUCCESS");

		String responseMsg = response.block();
		log.info("Response: {}", responseMsg);

		StepVerifier.create(response).expectNextMatches(msg -> msg.equalsIgnoreCase("SUCCESS")).verifyComplete();

		// =================================================================================================
		username = "sam";
		tokenGen = login("sam", "secret");

		token = tokenGen.block();
		refreshToken = token.getRefreshToken();
		log.info("Refresh Token generated {}", refreshToken);

		response = keycloakClientService.signout(username, refreshToken).thenReturn("SUCCESS");

		responseMsg = response.block();
		log.info("Response: {}", responseMsg);

		StepVerifier.create(response).expectNextMatches(msg -> msg.equalsIgnoreCase("SUCCESS")).verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("to test logout")
	@Test
	public void testThatLogoutWorks() {
		String userId = "8af75f52-d4ff-4f49-abf8-3df44d9deea5";
		Mono<AccessTokenResponse> tokenGen = login("sam", "secret");

		AccessTokenResponse token = tokenGen.block();
		String refreshToken = token.getRefreshToken();
		log.info("Refresh Token generated {}", refreshToken);

		Mono<String> response = keycloakClientService.logout(userId, refreshToken).thenReturn("SUCCESS");

		String responseMsg = response.block();
		log.info("Response: {}", responseMsg);

		StepVerifier.create(response).expectNextMatches(msg -> msg.equalsIgnoreCase("SUCCESS")).verifyComplete();

		// =================================================================================================
		userId = "76a6d636-9f95-4334-b682-f4570e4a8933";
		tokenGen = login("sam", "secret");

		token = tokenGen.block();
		refreshToken = token.getRefreshToken();
		log.info("Refresh Token generated {}", refreshToken);

		response = keycloakClientService.logout(userId, refreshToken).thenReturn("SUCCESS");

		responseMsg = response.block();
		log.info("Response: {}", responseMsg);

		StepVerifier.create(response).expectNextMatches(msg -> msg.equalsIgnoreCase("SUCCESS")).verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("to test reset password")
	@Test
	public void testThatResetPasswordWorks() {
		String username = "janedoe";
		String newPassword = RandomGenerator.generateRandomPassword();
		Mono<String> response = keycloakClientService.resetPassword(username, newPassword);

		String responseMsg = response.block();
		log.info("Response: {}", responseMsg);

		StepVerifier.create(response).expectNextMatches(msg -> msg.equalsIgnoreCase("reset.password.success"))
				.verifyComplete();

		// =================================================================================================

		username = "sam";
		newPassword = RandomGenerator.generateRandomPassword();
		response = keycloakClientService.resetPassword(username, newPassword);

		responseMsg = response.block();
		log.info("Response: {}", responseMsg);

		StepVerifier.create(response).expectNextMatches(msg -> msg.equalsIgnoreCase("reset.password.success"))
				.verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("to test save new password")
	@Test
	void testThatSaveNewPasswordWorks() {

		// correct email
		String emailAddr = "jane.doe@gmail.com";
		String oldPassword = RandomGenerator.generateRandomPassword();
		String newPassword = RandomGenerator.generateRandomPassword();

		CredentialRepresentation credRepresentation = preparePasswordRepresentation(oldPassword);
		PasswordUpdateRequest passwdUpdate = PasswordUpdateRequest.builder().oldPassword(oldPassword)
				.newPassword(newPassword).build();

		UserRepresentation userRepresentation = new UserRepresentation();
		userRepresentation.setId("8af75f52-d4ff-4f49-abf8-3df44d9deea5");
		userRepresentation.setFirstName("Jane");
		userRepresentation.setLastName("Doe");
		userRepresentation.setEmail(emailAddr);
		userRepresentation.setCredentials(Arrays.asList(credRepresentation));

		Mono<String> userRep = keycloakClientService.saveNewPassword(passwdUpdate, userRepresentation);

		String responseMessage = userRep.block();
		log.info("Response: {}", responseMessage);

		StepVerifier.create(userRep).expectNextMatches(msg -> StringUtils.isNotBlank(msg)).verifyComplete();

		// ====================================================================================================

		// correct email
		/*
		 * emailAddr = "gbenga.java@gmail.com"; oldPassword =
		 * RandomGenerator.generateRandomPassword(); newPassword =
		 * RandomGenerator.generateRandomPassword();
		 * 
		 * credRepresentation = preparePasswordRepresentation(oldPassword); passwdUpdate
		 * = PasswordUpdateRequest.builder().oldPassword(oldPassword).newPassword(
		 * newPassword).build();
		 * 
		 * userRepresentation = new UserRepresentation();
		 * userRepresentation.setId("76a6d636-9f95-4334-b682-f4570e4a8933");
		 * userRepresentation.setFirstName("SAMUEL");
		 * userRepresentation.setLastName("ADEBOWALE");
		 * userRepresentation.setEmail(emailAddr);
		 * userRepresentation.setCredentials(Arrays.asList(credRepresentation));
		 * 
		 * userRep = keycloakClientService.saveNewPassword(passwdUpdate,
		 * userRepresentation);
		 * 
		 * responseMessage = userRep.block(); log.info("Response: {}", responseMessage);
		 * 
		 * StepVerifier.create(userRep).expectNextMatches(msg ->
		 * StringUtils.isNotBlank(msg)).verifyComplete();
		 */
	}

	private CredentialRepresentation preparePasswordRepresentation(String plainPassword) {
		CredentialRepresentation credentials = new CredentialRepresentation();
		credentials.setTemporary(false);
		credentials.setType(CredentialRepresentation.PASSWORD);

		String hashedPassword = passwordEncoder.encode(plainPassword);
		credentials.setValue(hashedPassword);

		credentials.setCreatedDate(System.currentTimeMillis());

		return credentials;
	}

	/**
	 * 
	 */
	@DisplayName("to test search users with change in Capitalization")
	@Test
	void testSearchUserWithChangeInCapitalizationWorks() {
		// correct username with change in Capitalization
		String username = "sam";
		String lastName = "Adebowale";
		String firstName = "SAMueL";
		String email = "gbenga.java@gmail.com";
		boolean emailVerified = true;

		PagingModel pagingModel = PagingModel.builder().pageNo(1).pageSize(10).build();
		SearchUserRequest searchRequest = SearchUserRequest.builder().username(username).lastName(lastName)
				.firstName(firstName).email(email).emailVerified(emailVerified).build();

		Mono<List<UserRepresentation>> usersList = keycloakClientService.search(pagingModel, searchRequest);
		List<UserRepresentation> listUsers = usersList.block();
		listUsers.stream().forEach(user -> log.info("User: {} {}", user.getUsername(), user.getFirstName()));

		StepVerifier.create(usersList).expectNextMatches(users -> users.size() == 1).verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("to test search users with wrong username")
	@Test
	void testSearchUserWithWrongUsernameWorks() {
		// wrong username
		String username = "samuel";
		String lastName = "ADEBOWALE";
		String firstName = "SAMUEL";
		String email = "gbenga.java@gmail.com";
		boolean emailVerified = true;

		PagingModel pagingModel = PagingModel.builder().pageNo(1).pageSize(10).build();
		SearchUserRequest searchRequest = SearchUserRequest.builder().username(username).lastName(lastName)
				.firstName(firstName).email(email).emailVerified(emailVerified).build();

		Mono<List<UserRepresentation>> usersList = keycloakClientService.search(pagingModel, searchRequest);
		List<UserRepresentation> listUsers = usersList.block();
		listUsers.stream().forEach(user -> log.info("User: {} {}", user.getUsername(), user.getFirstName()));

		StepVerifier.create(usersList).expectNextMatches(users -> users.isEmpty()).verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("to test search users with email verified")
	@Test
	public void testSearchUserWithVerifiedEmailsWorks() {

		PagingModel pagingModel = PagingModel.builder().pageNo(1).pageSize(10).build();

		// Email is verified
		boolean emailVerified = true;
		Mono<List<UserRepresentation>> usersList = keycloakClientService.findUsersWithVerifiedEmails(emailVerified,
				pagingModel);

		List<UserRepresentation> listUsers = usersList.block();
		listUsers.stream().forEach(user -> log.info("User: {} {}", user.getUsername(), user.getFirstName()));

		StepVerifier.create(usersList).expectNextMatches(users -> users.size() == 1).verifyComplete();

		// Email is not verified
		/*
		 * emailVerified = false; usersList =
		 * keycloakClientService.findUsersWithVerifiedEmails(emailVerified,
		 * pagingModel);
		 * 
		 * listUsers = usersList.block(); listUsers.stream().forEach(user ->
		 * log.info("User: {} {}", user.getUsername(), user.getFirstName()));
		 * 
		 * StepVerifier.create(usersList).expectNextMatches(users -> users.size() ==
		 * 1).verifyComplete();
		 */
	}

	/**
	 * 
	 */
	@DisplayName("to test search user with id")
	@Test
	void testSearchUserByIdWorks() {
		// correct user id
		String userId = "76a6d636-9f95-4334-b682-f4570e4a8933";

		Mono<UserRepresentation> userFound = keycloakClientService.findUserById(userId);

		UserRepresentation userRep = userFound.block();
		log.info("User rep {}", userRep);

		StepVerifier.create(userFound)
				.expectNextMatches(
						user -> StringUtils.isNotBlank(user.getUsername()) && user.getUsername().equals("sam"))
				.verifyComplete();

		// correct userid with unverified Email
		userId = "8af75f52-d4ff-4f49-abf8-3df44d9deea5";

		userFound = keycloakClientService.findUserByUsername(userId);

		StepVerifier.create(userFound).expectError(UserProfileUnverifiedException.class);
	}

	/**
	 * 
	 */
	@DisplayName("to test update user Mfa")
	@Test
	public void testThatUpdateMfaWorks() {
		String username = "janedoe";
		boolean wantToEnable = true;
		Mono<String> response = keycloakClientService.updateUserMfa(username, wantToEnable);

		String genSecret = response.block();
		log.info("Generated Secret {}", genSecret);

		StepVerifier.create(response).expectNextMatches(msg -> StringUtils.isNotBlank(msg)).verifyComplete();

		// =================================================================================================

		username = "janedoe";
		wantToEnable = false;
		response = keycloakClientService.updateUserMfa(username, wantToEnable).switchIfEmpty(Mono.just("Mfa disabled"));

		genSecret = response.block();
		log.info("Generated Secret {}", genSecret);

		StepVerifier.create(response).expectNextMatches(msg -> msg.equalsIgnoreCase("Mfa disabled")).verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("to test update user status by username")
	@Test
	public void testThatUpdateUserStatusWorks() {
		String username = "janedoe";
		StatusUpdateRequest statusUpdate = StatusUpdateRequest.builder().status(StatusType.LOCKED.toString()).build();
		Mono<String> response = keycloakClientService.updateUserStatus(username, statusUpdate)
				.defaultIfEmpty("SUCCESS");

		String responseMsg = response.block();
		log.info("Response: {}", responseMsg);

		StepVerifier.create(response).expectNextMatches(msg -> msg.equalsIgnoreCase("SUCCESS")).verifyComplete();

		// =================================================================================================

		username = "janedoe";
		statusUpdate = StatusUpdateRequest.builder().status(StatusType.EXPIRED.toString()).build();
		response = keycloakClientService.updateUserStatus(username, statusUpdate).defaultIfEmpty("SUCCESS");

		responseMsg = response.block();
		log.info("Response: {}", responseMsg);

		StepVerifier.create(response).expectNextMatches(msg -> msg.equalsIgnoreCase("SUCCESS")).verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("to test update user status by id")
	@Test
	public void testThatUpdateUserIdStatusWorks() {
		String userId = "8af75f52-d4ff-4f49-abf8-3df44d9deea5";
		StatusUpdateRequest statusUpdate = StatusUpdateRequest.builder().status(StatusType.LOCKED.toString()).build();
		Mono<String> response = keycloakClientService.updateUserByIdStatus(userId, statusUpdate)
				.defaultIfEmpty("SUCCESS");

		String responseMsg = response.block();
		log.info("Response: {}", responseMsg);

		StepVerifier.create(response).expectNextMatches(msg -> msg.equalsIgnoreCase("SUCCESS")).verifyComplete();

		// =================================================================================================

		userId = "76a6d636-9f95-4334-b682-f4570e4a8933";
		statusUpdate = StatusUpdateRequest.builder().status(StatusType.EXPIRED.toString()).build();
		response = keycloakClientService.updateUserByIdStatus(userId, statusUpdate).defaultIfEmpty("SUCCESS");

		responseMsg = response.block();
		log.info("Response: {}", responseMsg);

		StepVerifier.create(response).expectNextMatches(msg -> msg.equalsIgnoreCase("SUCCESS")).verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("to test assign client role to user")
	@Test
	public void testThatAssignClientRoleWorks() {
		final String userIdA = "8af75f52-d4ff-4f49-abf8-3df44d9deea5";
		String clientId = "admin-client";
		final String roleNameA = "ADMIN";
		Mono<String> response = keycloakClientService.assignUserClientRole(userIdA, clientId, roleNameA);

		String responseMsg = response.block();
		log.info("Response: {}", responseMsg);

		StepVerifier.create(response)
				.expectNextMatches(msg -> msg.equalsIgnoreCase("message.clientrole.assign.success")).verifyComplete();

		// =================================================================================================

		final String userIdB = "76a6d636-9f95-4334-b682-f4570e4a8933";
		final String roleNameB = "REPORTING";
		response = keycloakClientService.assignUserClientRole(userIdB, clientId, roleNameB);

		responseMsg = response.block();
		log.info("Response: {}", responseMsg);

		StepVerifier.create(response)
				.expectNextMatches(msg -> msg.equalsIgnoreCase("message.clientrole.assign.success")).verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("to test assign realm role to user")
	@Test
	public void testThatAssignRealmRoleWorks() {
		final String userIdA = "8af75f52-d4ff-4f49-abf8-3df44d9deea5";
		final String roleNameA = "ROLE_ADMIN";
		Mono<String> response = keycloakClientService.assignUserRealmRole(userIdA, roleNameA);

		String responseMsg = response.block();
		log.info("Response: {}", responseMsg);

		StepVerifier.create(response).expectNextMatches(msg -> msg.equalsIgnoreCase("message.realmrole.assign.success"))
				.verifyComplete();

		// =================================================================================================

		final String userIdB = "76a6d636-9f95-4334-b682-f4570e4a8933";
		final String roleNameB = "ROLE_USER";
		response = keycloakClientService.assignUserRealmRole(userIdB, roleNameB);

		responseMsg = response.block();
		log.info("Response: {}", responseMsg);

		StepVerifier.create(response).expectNextMatches(msg -> msg.equalsIgnoreCase("message.realmrole.assign.success"))
				.verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("to test assign user to group")
	@Test
	public void testThatAssignGroupWorks() {
		final String userIdA = "8af75f52-d4ff-4f49-abf8-3df44d9deea5";
		final String groupIdA = "d481fbde-2215-4aa4-9911-5248f409ff7b";
		Mono<String> response = keycloakClientService.assignUserToGroup(userIdA, groupIdA);

		String responseMsg = response.block();
		log.info("Response: {}", responseMsg);

		StepVerifier.create(response).expectNextMatches(msg -> msg.equalsIgnoreCase("message.group.assign.success"))
				.verifyComplete();

		// =================================================================================================

		final String userIdB = "76a6d636-9f95-4334-b682-f4570e4a8933";
		final String groupIdB = "31292a37-f2df-484d-bb32-c54d594d90ca";
		response = keycloakClientService.assignUserToGroup(userIdB, groupIdB);

		responseMsg = response.block();
		log.info("Response: {}", responseMsg);

		StepVerifier.create(response).expectNextMatches(msg -> msg.equalsIgnoreCase("message.group.assign.success"))
				.verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("to test user info endpoint")
	@Test
	public void testThatUserInfoWorks() {

		Mono<AccessTokenResponse> tokenGen = login("sam", "secret");

		AccessTokenResponse token = tokenGen.block();
		String accessToken = token.getToken();
		log.info("Access Token generated {}", accessToken);

		Mono<UserInfo> response = keycloakClientService.userInfo(accessToken);

		UserInfo userInfo = response.block();
		log.info("User info {}", userInfo);

		StepVerifier.create(response).expectNextMatches(res -> !Objects.isNull(userInfo)).verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("to test refresh token")
	@Test
	public void testThatRefreshTokenWorks() {
		String username = "janedoe";
		// String targetClientId = "client-a";

		Mono<AccessTokenResponse> tokenGen = login("janedoe", "secret");

		AccessTokenResponse token = tokenGen.block();
		String refreshToken = token.getRefreshToken();
		log.info("Old Refresh Token generated {}", refreshToken);

		Mono<AuthenticationResponse> response = keycloakClientService.refreshToken(username, refreshToken);

		AuthenticationResponse authToken = response.block();
		String newRefreshToken = authToken.getRefreshToken();
		log.info("New Refresh token generated {}", newRefreshToken);

		Assert.assertTrue(!refreshToken.equalsIgnoreCase(newRefreshToken));
	}

	/**
	 * User-info not working as expected
	 */
	@DisplayName("to test token validity endpoint")
	@Test
	public void testThatTokenValidityWorks() {

		Mono<AccessTokenResponse> tokenGen = login("sam", "secret");

		AccessTokenResponse token = tokenGen.block();
		String accessToken = token.getToken();
		log.info("Access Token generated {}", accessToken);

		Mono<Boolean> response = keycloakClientService.validateToken(accessToken);

		Boolean bool = response.block();
		log.info("Is token valid {}", bool);

		StepVerifier.create(response).expectNextMatches(res -> bool == Boolean.TRUE).verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("to test enable App user works")
	@Test
	public void testThatEnableOauthUserWorks() {
		final String userId = "8af75f52-d4ff-4f49-abf8-3df44d9deea5";
		ProfileActivationUpdateRequest profileUpdate = new ProfileActivationUpdateRequest(userId, true);

		Mono<String> monoResult = keycloakClientService.enableOauthUser(profileUpdate);

		StepVerifier.create(monoResult)
				.expectNextMatches(
						result -> StringUtils.isNotBlank(result) && result.equalsIgnoreCase("message.user.enabled"))
				.verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("to test update App user works")
	@Test
	public void testThatUpdateOauthUserWorks() {
		final String username = "sam";

		UserDetailsUpdateRequest profileUpdate = new UserDetailsUpdateRequest("SAMUEL", "ADEBOWALE");
		Mono<UserVO> monoResult = keycloakClientService.updateOauthUser(profileUpdate, username);

		StepVerifier.create(monoResult)
				.expectNextMatches(result -> !Objects.isNull(result) && result.getFirstName().equalsIgnoreCase("SAMUEL")
						&& result.getLastName().equalsIgnoreCase("ADEBOWALE"))
				.verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("to test update App user by id works")
	@Test
	public void testThatUpdateOauthUserByIdWorks() {
		final String userId = "76a6d636-9f95-4334-b682-f4570e4a8933";

		UserDetailsUpdateRequest profileUpdate = new UserDetailsUpdateRequest("SAMUEL", "ADEBOWALE");
		Mono<UserVO> monoResult = keycloakClientService.updateOauthUserById(profileUpdate, userId);

		StepVerifier.create(monoResult)
				.expectNextMatches(result -> !Objects.isNull(result) && result.getFirstName().equalsIgnoreCase("SAMUEL")
						&& result.getLastName().equalsIgnoreCase("ADEBOWALE"))
				.verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("to test delete App user works")
	@Test
	public void testThatDeleteAppUserWorks() {
		final String userId = "8af75f52-d4ff-4f49-abf8-3df44d9deea5";

		Mono<String> monoResult = keycloakClientService.deleteAppUser(userId);

		StepVerifier.create(monoResult)
				.expectNextMatches(
						result -> StringUtils.isNotBlank(result) && result.equalsIgnoreCase("message.user.disabled"))
				.verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("to test expire password works")
	@Test
	public void testThatExpirePasswordWorks() {
		// correct email
		String emailAddr = "jane.doe@gmail.com";
		String newPassword = RandomGenerator.generateRandomPassword();

		SocialLink socialLink = new SocialLink("12345", SocialProvider.GOOGLE);
		String[] roles = { "ROLE_ADMIN" };

		UserRepresentation userRepresentation = new UserRepresentation();
		userRepresentation.setId("8af75f52-d4ff-4f49-abf8-3df44d9deea5");
		userRepresentation.setUsername("janedoe");
		userRepresentation.setFirstName("Jane");
		userRepresentation.setLastName("Doe");
		userRepresentation.setEmail(emailAddr);
		userRepresentation.setRealmRoles(Arrays.asList(roles));

		Mono<String> monoResult = keycloakClientService.expireUserPassword(userRepresentation);

		StepVerifier.create(monoResult)
				.expectNextMatches(
						result -> StringUtils.isNotBlank(result) && result.equalsIgnoreCase("auth.message.success"))
				.verifyComplete();

		// ===================================================================================
		// Specify unknown username
		UserRepresentation userRepresentation2 = new UserRepresentation();
		userRepresentation2.setId("8af75f52-d4ff-4f49-abf8-3df44d9deea5");
		userRepresentation2.setUsername("jane");
		userRepresentation2.setFirstName("Jane");
		userRepresentation2.setLastName("Doe");
		userRepresentation2.setEmail(emailAddr);
		userRepresentation2.setRealmRoles(Arrays.asList(roles));

		monoResult = keycloakClientService.expireUserPassword(userRepresentation2);

		StepVerifier.create(monoResult).expectError(ResourceNotFoundException.class).verify();
	}

	/**
	 * 
	 * @throws InterruptedException
	 */
	@Test
	void userLogin() throws InterruptedException {

		Mono<AccessTokenResponse> tokenGen = login("sam", "secret");

		AccessTokenResponse token = tokenGen.block();
		String accessToken = token.getToken();
		log.info("Token generated for exchange {}", accessToken);
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
		HttpStatusCode status = clientResponse.statusCode();

		Mono<R> respObj = Mono.empty();

		if (status.is2xxSuccessful()) {
			respObj = clientResponse.bodyToMono(clazzResponse);

		} else if (status.isError()) {
			return clientResponse.createException().flatMap(ex -> Mono.error(ex));
		}

		return respObj;
	}
}
