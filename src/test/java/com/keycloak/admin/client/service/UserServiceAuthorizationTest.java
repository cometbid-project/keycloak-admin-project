/**
 * 
 */
package com.keycloak.admin.client.service;

import com.github.javafaker.Faker;
import ua_parser.Parser;

import com.keycloak.admin.client.common.enums.Role;
import com.keycloak.admin.client.common.events.GenericSpringEvent;
import com.keycloak.admin.client.common.geo.GeolocationUtils;
import com.keycloak.admin.client.common.utils.LocaleContextUtils;
import com.keycloak.admin.client.common.utils.RandomGenerator;
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;
import com.keycloak.admin.client.config.AppConfiguration;
import com.keycloak.admin.client.config.AuthProfile;
import com.keycloak.admin.client.config.MessageConfig;
import com.keycloak.admin.client.config.SecurityConfig;
import com.keycloak.admin.client.dataacess.AuthBuilder;
import com.keycloak.admin.client.dataacess.JwtUtil;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.entities.ActivationToken;
import com.keycloak.admin.client.entities.UserloginLocation;
import com.keycloak.admin.client.exceptions.ResourceNotFoundException;
import com.keycloak.admin.client.exceptions.ServiceUnavailableException;
import com.keycloak.admin.client.exceptions.UserAlreadyExistException;
import com.keycloak.admin.client.models.LoginLocation;
import com.keycloak.admin.client.models.PagingModel;
import com.keycloak.admin.client.models.SearchUserRequest;
import com.keycloak.admin.client.models.SocialLink;
import com.keycloak.admin.client.models.UserDetailsUpdateRequest;
import com.keycloak.admin.client.models.UserRegistrationRequest;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.models.mappers.UserMapper;
import com.keycloak.admin.client.oauth.service.ActivationTokenServiceImpl;
import com.keycloak.admin.client.oauth.service.CommonUtil;
import com.keycloak.admin.client.oauth.service.KeycloakOauthClient;
import com.keycloak.admin.client.oauth.service.UserCredentialFinderServiceImpl;
import com.keycloak.admin.client.oauth.service.UserCredentialServiceImpl;
import com.keycloak.admin.client.oauth.service.UserLocationServiceImpl;
import com.keycloak.admin.client.oauth.service.it.ActivationTokenService;
import com.keycloak.admin.client.oauth.service.it.KeycloakOauthClientService;
import com.keycloak.admin.client.oauth.service.it.UserCredentialFinderService;
import com.keycloak.admin.client.oauth.service.it.UserCredentialService;
import com.keycloak.admin.client.oauth.service.it.UserLocationService;
import com.maxmind.geoip2.DatabaseReader;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

import java.time.temporal.ChronoUnit;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.ClientErrorException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Gbenga
 *
 */
@Log4j2
@WebFluxTest
@DisplayName("Verify User service")
@ContextConfiguration(classes = { AuthProfile.class, AppConfiguration.class, SecurityConfig.class,
		MessageConfig.class })
@Import({ UserCredentialServiceImpl.class, UserCredentialFinderServiceImpl.class, KeycloakOauthClient.class,
		UserLocationServiceImpl.class, ActivationTokenServiceImpl.class, UserMapper.class, LocaleContextUtils.class,
		CustomMessageSourceAccessor.class, ApplicationEventPublisher.class, CommonUtil.class })
//@ExtendWith({ SpringExtension.class })
class UserServiceAuthorizationTest {

	@Autowired
	private UserCredentialService userService;
	@Autowired
	private UserCredentialFinderService userFinderService;

	private static DatabaseReader databaseReader;
	private static Parser parser;
	private static ServerRequest serverRequest;
	private static ServerHttpRequest httpRequest;
	private static ServerWebExchange sweExchange;

	@MockBean
	private ServerRequest mockServerRequest;
	@MockBean
	private ServerWebExchange mockServerWebExchange;
	@MockBean
	private ServerHttpRequest mockHttpRequest;
	@MockBean
	private CommonUtil commonUtil;
	@MockBean
	private KeycloakOauthClientService keycloakClient;

	@MockBean
	private ApplicationEventPublisher eventPublisher;
	@MockBean
	private ActivationTokenService activationTokenService;
	@MockBean
	private UserLocationService userLocationService;

	private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel MacOS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.114 Safari/537.36";

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

	}

	@BeforeAll
	static void initializeData() throws FileNotFoundException, IOException, URISyntaxException {

		parser = new Parser();

		databaseReader = new DatabaseReader.Builder(ResourceUtils.getFile("classpath:maxmind/GeoLite2-City.mmdb"))
				.build();
		// BlockHound.install(new ReactorBlockHoundIntegration(), new MyIntegration());
		// =============================================================================

		HttpHeaders headers = new HttpHeaders();
		headers.add("Forwarded", "host=84.198.58.199;proto=https");
		headers.add("User-Agent", USER_AGENT);

		httpRequest = MockServerHttpRequest.method(HttpMethod.GET, new URI("http://example.com/a%20b?q=a%2Bb"))
				.contentType(MediaType.APPLICATION_JSON).headers(headers).build();

		// when(httpRequest.getHeaders()).thenReturn(headers);
		sweExchange = MockServerWebExchange.from((MockServerHttpRequest) httpRequest);

		ServerRequest other = ServerRequest.create(sweExchange, HandlerStrategies.withDefaults().messageReaders());

		DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
		Flux<DataBuffer> body = Flux.just("baz").map(s -> s.getBytes(StandardCharsets.UTF_8))
				.map(dataBufferFactory::wrap);

		serverRequest = ServerRequest.from(other)
				// .method(HttpMethod.HEAD)
				.headers(httpHeaders -> httpHeaders.set("foo", "baar"))
				.cookies(cookies -> cookies.set("baz", ResponseCookie.from("baz", "quux").build())).body(body).build();
	}

	/**
	 * Re-test
	 */
	@DisplayName("grants access to find one user by username for anonymous user")
	@Test
	void verifyFindOneByUsernameAccessIsGrantedForUnauthenticated() {

		UUID userId = UUID.randomUUID();
		UserRepresentation userRepresentation = UserBuilder.user().userRepresentation(userId);

		String username = userRepresentation.getEmail();

		when(keycloakClient.findUserByUsername(any(String.class))).thenReturn(Mono.just(userRepresentation));

		Mono<UserVO> monoUser = userFinderService.findByUsername(username);

		StepVerifier.create(monoUser)
				// .expectNextCount(1)
				.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result.getId())
						&& StringUtils.isNotBlank(result.getUsername()) && result.getId().equals(userId.toString())
						&& result.getUsername().equals(username))
				.verifyComplete();
	}

	/**
	 * Re-test
	 */
	@DisplayName("grants access to find one user by username for roles 'USER', 'APP_MANAGER', 'MANAGER' and 'ADMIN'")
	@Test
	@WithMockUser(roles = { "USER", "APP_MANAGER", "MANAGER", "ADMIN" })
	void verifyFindOneByUsernameAccessIsGrantedForAllRoles() {

		UUID userId = UUID.randomUUID();
		UserRepresentation userRepresentation = UserBuilder.user().userRepresentation(userId);

		String username = userRepresentation.getEmail();

		when(keycloakClient.findUserByUsername(any(String.class))).thenReturn(Mono.just(userRepresentation));

		Mono<UserVO> monoUser = userFinderService.findByUsername(username);

		StepVerifier.create(monoUser)
				// .expectNextCount(1)
				.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result.getId())
						&& StringUtils.isNotBlank(result.getUsername()) && result.getId().equals(userId.toString())
						&& result.getUsername().equals(username))
				.verifyComplete();
	}

	@SuppressWarnings("unchecked")
	private void createUserPostConditions(UUID userId) {
		// UUID userId = UUID.randomUUID();
		String ipAddress = "166.197.174.199";
		UserRegistrationRequest userRequest = UserBuilder.user().build();
		String username = userRequest.getEmail();

		when(keycloakClient.createOauthUser(any(UserRepresentation.class), any(String.class), any(SocialLink.class)))
				.thenReturn(Mono.just(userId.toString()));

		ActivationToken activationToken = ActivationToken.builder().username(username)
				.token(RandomGenerator.generateNewToken()).build();

		when(commonUtil.getNewActivationToken(anyString())).thenReturn(Mono.just(activationToken));

		UserloginLocation usrLoginLoc = UserloginLocation.builder().id(UUID.randomUUID().toString()).ipAddr(ipAddress)
				.loginLocHis(Collections.emptyList()).username(username).build();

		when(commonUtil.recordNewUserLocation(anyString(), any(ServerHttpRequest.class)))
				.thenReturn(Mono.just(usrLoginLoc));

		String deviceDetails = GeolocationUtils.getDeviceDetails(USER_AGENT, parser);
		log.info("Device details {}", deviceDetails);

		LoginLocation loginLoc = GeolocationUtils.getUserRelativeLocation(ipAddress, deviceDetails, databaseReader);

		Mono<LoginLocation> monoLoginLoc = Mono.just(loginLoc);
		when((Mono<LoginLocation>) userLocationService.decodeUserLocation(httpRequest)).thenReturn(monoLoginLoc);

		when(mockServerRequest.exchange()).thenReturn(sweExchange);

		when(mockServerWebExchange.getRequest()).thenReturn(mockHttpRequest);

	}

	/**
	 * 
	 */
	@DisplayName("grants access to create a user for anonymous user")
	@Test
	void verifyCreateUserAccessIsGrantedForAnonymousUser() {

		UUID userId = UUID.randomUUID();
		UserBuilder user = UserBuilder.user();
		UserRegistrationRequest userRequest = user.withDisplayName().build();
		log.info("User Registration Request {}", userRequest);

		UserRepresentation userRepresentation = user.userRepresentation(userId);

		createUserPostConditions(userId);

		when(keycloakClient.findUserByUsername(any(String.class))).thenReturn(Mono.empty());

		when(keycloakClient.createOauthUser(any(UserRepresentation.class), any(String.class), any(SocialLink.class)))
				.thenReturn(Mono.just(userId.toString()));

		doNothing().when(eventPublisher).publishEvent(any(GenericSpringEvent.class));

		// =========================================================================================================

		when(keycloakClient.findUserById(any(String.class))).thenReturn(Mono.just(userRepresentation));

		StepVerifier.create(userService.signupUser(userRequest, Role.ROLE_USER, httpRequest))
				.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result.getId())
						&& StringUtils.isNotBlank(result.getUsername()) && result.getId().equals(userId.toString())
						&& result.getUsername().equals(userRequest.getEmail()))
				.verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("grants access to create user via Social login for anonymous user")
	@Test
	void verifyCreateSocialUserAccessIsGrantedForAnonymousUser() {

		UUID userId = UUID.randomUUID();
		UserBuilder user = UserBuilder.user();

		createUserPostConditions(userId);
		// =================================================================================================

		UserVO userVO = user.userVo(userId);
		UserRepresentation userRepresentation = user.userRepresentation(userId);
		log.info("Social links {}", userRepresentation.getSocialLinks().get(0).getSocialProvider());

		AuthBuilder authBuilder = AuthBuilder.auth(userVO);
		String registrationId = userRepresentation.getSocialLinks().get(0).getSocialProvider();
		// =================================================================================================

		when(keycloakClient.findUserByUsername(any(String.class))).thenReturn(Mono.empty());
		when(keycloakClient.findUserByEmail(anyString())).thenReturn(Mono.empty());
		when(keycloakClient.updateOauthUser(any(UserDetailsUpdateRequest.class), anyString()))
				.thenReturn(Mono.just(userVO));
		when(keycloakClient.createOauthUser(any(UserRepresentation.class), anyString(), any(SocialLink.class)))
				.thenReturn(Mono.just(userId.toString()));

		when(keycloakClient.findUserById(any(String.class))).thenReturn(Mono.just(userRepresentation));

		doNothing().when(eventPublisher).publishEvent(any(GenericSpringEvent.class));
		// =================================================================================================
		String tokenValue = authBuilder.token();
		Integer attrId = Faker.instance().number().numberBetween(100, 100000);

		Map<String, Object> attributes = new HashMap<>();
		attributes.put("id", String.valueOf(attrId));
		attributes.put("sub", authBuilder.getUsername());
		attributes.put("email", userVO.getEmail());
		attributes.put("name", userVO.getFirstName());
		attributes.put("localizedFirstName", userVO.getFirstName());
		attributes.put("localizedLastName", userVO.getLastName());
		attributes.put("emailAddress", userVO.getEmail());

		Instant issuedAt = Instant.now();
		Instant expiresAt = Instant.now().plus(2, ChronoUnit.DAYS);
		Map<String, Object> claims = JwtUtil.extractClaims(tokenValue);
		log.info("Claims {} ", claims);
		// =================================================================================================

		OidcIdToken oidcIdToken = new OidcIdToken(tokenValue, issuedAt, expiresAt, claims); // Retest
		OidcUserInfo oidcUserInfo = new OidcUserInfo(claims);

		StepVerifier.create(userService.processUserRegistration(registrationId, attributes, oidcIdToken, oidcUserInfo))
				.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result.getUsername())
						&& StringUtils.isNotBlank(result.getUser().getId())
						&& result.getUsername().equals(userVO.getEmail()))
				.verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("grants access to update existing user via Social login for anonymous user")
	@Test
	void verifyUpdateSocialUserAccessIsGrantedForAnonymousUser() {

		UUID userId = UUID.randomUUID();
		UserBuilder user = UserBuilder.user();

		createUserPostConditions(userId);

		UserVO userVO = user.userVo(userId);
		UserRegistrationRequest userRequest = user.withDisplayName().build();
		UserRepresentation userRepresentation = user.userRepresentation(userId);
		log.info("User Registration Request {}", userRequest);
		log.info("Social links {}", userRepresentation.getSocialLinks().get(0).getSocialProvider());

		AuthBuilder authBuilder = AuthBuilder.auth(userVO);
		String registrationId = userRepresentation.getSocialLinks().get(0).getSocialProvider();

		String tokenValue = authBuilder.token();
		Integer attrId = Faker.instance().number().numberBetween(100, 100000);

		Map<String, Object> attributes = new HashMap<>();
		attributes.put("id", String.valueOf(attrId));
		attributes.put("sub", authBuilder.getUsername());
		attributes.put("email", userVO.getEmail());
		attributes.put("name", userVO.getFirstName());
		attributes.put("localizedFirstName", userVO.getFirstName());
		attributes.put("localizedLastName", userVO.getLastName());
		attributes.put("emailAddress", userVO.getEmail());

		Instant issuedAt = Instant.now();
		Instant expiresAt = Instant.now().plus(2, ChronoUnit.DAYS);

		when(keycloakClient.findUserByUsername(any(String.class))).thenReturn(Mono.empty());
		when(keycloakClient.findUserByEmail(anyString())).thenReturn(Mono.just(userRepresentation));
		when(keycloakClient.updateOauthUser(any(UserDetailsUpdateRequest.class), anyString()))
				.thenReturn(Mono.just(userVO));

		when(keycloakClient.createOauthUser(any(UserRepresentation.class), any(String.class), any(SocialLink.class)))
				.thenReturn(Mono.just(userId.toString()));

		doNothing().when(eventPublisher).publishEvent(any(GenericSpringEvent.class));

		Map<String, Object> claims = JwtUtil.extractClaims(tokenValue);
		log.info("Claims {} ", claims);

		OidcIdToken oidcIdToken = new OidcIdToken(tokenValue, issuedAt, expiresAt, claims); // Retest
		OidcUserInfo oidcUserInfo = new OidcUserInfo(claims);

		StepVerifier.create(userService.processUserRegistration(registrationId, attributes, oidcIdToken, oidcUserInfo))
				.expectNextMatches(result -> result != null // && StringUtils.isNotBlank(result.getPassword())
						&& StringUtils.isNotBlank(result.getUsername())
						&& result.getUsername().equals(userRequest.getEmail()))
				.verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("grants access to create a user for role 'APP_MANAGER', 'MANAGER' and 'ADMIN'")
	@Test
	@WithMockUser(roles = { "APP_MANAGER", "MANAGER", "ADMIN" })
	void verifyCreateUserAccessIsGrantedForAdminAndManagerRoles() {

		UUID userId = UUID.randomUUID();
		UserBuilder user = UserBuilder.user();
		UserRegistrationRequest userRequest = user.build();

		createUserPostConditions(userId);

		UserRepresentation userRepresentation = user.userRepresentation(userId);

		when(keycloakClient.findUserByUsername(any(String.class))).thenReturn(Mono.empty());

		when(keycloakClient.createOauthUser(any(UserRepresentation.class), any(String.class), any(SocialLink.class)))
				.thenReturn(Mono.just(userId.toString()));

		doNothing().when(eventPublisher).publishEvent(any(GenericSpringEvent.class));

		when(keycloakClient.findUserById(any(String.class))).thenReturn(Mono.just(userRepresentation));

		StepVerifier.create(userService.signupUser(userRequest, Role.ROLE_USER, httpRequest))
				.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result.getId())
						&& StringUtils.isNotBlank(result.getUsername()) && result.getId().equals(userId.toString())
						&& result.getUsername().equals(userRequest.getEmail()))
				.verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("grants access to create a user while account exist with similar email")
	@Test
	void verifyCreateUserAccessAnonymousUserWithExistingEmail() {

		UUID userId = UUID.randomUUID();
		UserRegistrationRequest userRequest = UserBuilder.user().build();

		createUserPostConditions(userId);

		when(keycloakClient.findUserByUsername(anyString()))
				.thenReturn(Mono.just(UserBuilder.user().userRepresentation(userId)));

		when(keycloakClient.createOauthUser(any(UserRepresentation.class), any(String.class), any(SocialLink.class)))
				.thenReturn(Mono.just(userId.toString()));

		StepVerifier.create(userService.signupUser(userRequest, Role.ROLE_USER, httpRequest))
				.expectError(UserAlreadyExistException.class).verify();
	}

	/**
	 * 
	 */
	@DisplayName("grants access to create a user but connection to Authentication Server failed")
	@Test
	void verifyCreateUserAccessKeycloakConnectionFailed() {

		UUID userId = UUID.randomUUID();
		UserRegistrationRequest userRequest = UserBuilder.user().build();

		createUserPostConditions(userId);

		when(keycloakClient.findUserByUsername(any(String.class))).thenReturn(Mono.empty());

		when(keycloakClient.createOauthUser(any(UserRepresentation.class), any(String.class), any(SocialLink.class)))
				.thenThrow(ClientErrorException.class);

		doNothing().when(eventPublisher).publishEvent(any(GenericSpringEvent.class));

		StepVerifier.create(userService.signupUser(userRequest, Role.ROLE_USER, httpRequest))
				.expectError(ServiceUnavailableException.class).verify();
	}

	/**
	 * 
	 */
	@DisplayName("grants access to find a user by id for role 'ADMIN', 'APP_MANAGER', 'MANAGER'")
	@Test
	@WithMockUser(roles = { "APP_MANAGER", "MANAGER", "ADMIN" })
	void verifyFindByIdAccessIsGrantedForAdmin() {

		UUID userId = UUID.randomUUID();

		when(keycloakClient.findUserById(any(String.class)))
				.thenReturn(Mono.just(UserBuilder.user().userRepresentation(userId)));

		Mono<UserVO> monoUser = userFinderService.findUserById(userId.toString());

		StepVerifier.create(monoUser)
				// .expectNextCount(1)
				.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result.getId())
						&& StringUtils.isNotBlank(result.getUsername()) && result.getId().equals(userId.toString())
						&& StringUtils.isNotBlank(result.getDisplayName()))
				.verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("denies access to find a user by id for roles 'USER' and 'ACTUATOR'")
	@Test
	@WithMockUser(roles = { "USER", "ACTUATOR" })
	void verifyFindByIdAccessIsDeniedForUserAndActuator() {

		UUID userId = UUID.randomUUID();

		when(keycloakClient.findUserById(any(String.class)))
				.thenReturn(Mono.just(UserBuilder.user().userRepresentation(userId)));

		Mono<UserVO> monoUser = userFinderService.findUserById(userId.toString());

		StepVerifier.create(monoUser).verifyError(AccessDeniedException.class);
	}

	/**
	 * 
	 */
	@DisplayName("denies access to find a user by id for anonymous user")
	@Test
	void verifyFindByIdAccessIsDeniedForUnauthenticated() {

		UUID userId = UUID.randomUUID();

		when(keycloakClient.findUserById(any(String.class)))
				.thenReturn(Mono.just(UserBuilder.user().userRepresentation(userId)));

		Mono<UserVO> monoUser = userFinderService.findUserById(userId.toString());

		StepVerifier.create(monoUser).verifyError(AccessDeniedException.class);
	}

	/**
	 * 
	 */
	@DisplayName("grants access to find a user by id for roles but throws Resources not found")
	@Test
	@WithMockUser(roles = { "APP_MANAGER", "MANAGER", "ADMIN" })
	void verifyFindByIdAccessIsGrantedForAdminByResourceNotFound() {

		UUID userId = UUID.randomUUID();

		when(keycloakClient.findUserById(any(String.class))).thenReturn(Mono.empty());

		Mono<UserVO> monoUser = userFinderService.findUserById(userId.toString());

		StepVerifier.create(monoUser).verifyError(ResourceNotFoundException.class);
	}

	/**
	 * 
	 */
	@DisplayName("grants access to search all users for role 'ADMIN'")
	@Test
	@WithMockUser(roles = "ADMIN")
	void verifySearchUserAccessIsGrantedForAdmin() {

		PagingModel pagingModel = PagingModel.builder().build();

		List<UserRepresentation> listUser = Arrays.asList(UserBuilder.user().userRepresentation(UUID.randomUUID()),
				UserBuilder.user().userRepresentation(UUID.randomUUID()));

		SearchUserRequest searchRequest = UserBuilder.user().searchUserRepresentation();

		when(keycloakClient.search(any(PagingModel.class), any(SearchUserRequest.class)))
				.thenReturn(Mono.just(listUser));

		Flux<UserVO> fluxUser = userFinderService.search(searchRequest, pagingModel);

		StepVerifier.create(fluxUser).expectNextCount(2).verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("deny access to search all users to roles except 'ADMIN'")
	@Test
	@WithMockUser(roles = { "APP_MANAGER", "MANAGER", "USER" })
	void verifySearchUserDenyAccessToRolesExceptAdmin() {

		PagingModel pagingModel = PagingModel.builder().build();

		List<UserRepresentation> listUser = Arrays.asList(UserBuilder.user().userRepresentation(UUID.randomUUID()),
				UserBuilder.user().userRepresentation(UUID.randomUUID()));

		SearchUserRequest searchRequest = UserBuilder.user().searchUserRepresentation();

		when(keycloakClient.search(any(PagingModel.class), any(SearchUserRequest.class)))
				.thenReturn(Mono.just(listUser));

		Flux<UserVO> fluxUser = userFinderService.search(searchRequest, pagingModel);

		StepVerifier.create(fluxUser).verifyError(AccessDeniedException.class);
	}

	/**
	 * 
	 */
	@DisplayName("deny access to search all users to anonymous user")
	@Test
	void verifySearchUserDenyAccessToAnonymous() {

		PagingModel pagingModel = PagingModel.builder().build();

		List<UserRepresentation> listUser = Arrays.asList(UserBuilder.user().userRepresentation(UUID.randomUUID()),
				UserBuilder.user().userRepresentation(UUID.randomUUID()));

		SearchUserRequest searchRequest = UserBuilder.user().searchUserRepresentation();

		when(keycloakClient.search(any(PagingModel.class), any(SearchUserRequest.class)))
				.thenReturn(Mono.just(listUser));

		Flux<UserVO> fluxUser = userFinderService.search(searchRequest, pagingModel);

		StepVerifier.create(fluxUser).verifyError(AccessDeniedException.class);
	}

	/**
	 * 
	 */
	@DisplayName("grants access to find all users for role 'ADMIN'")
	@Test
	@WithMockUser(roles = "ADMIN")
	void verifyFindAllAccessIsGrantedForAdmin() {

		PagingModel pagingModel = PagingModel.builder().build();

		List<UserRepresentation> listUser = Arrays.asList(UserBuilder.user().userRepresentation(UUID.randomUUID()),
				UserBuilder.user().userRepresentation(UUID.randomUUID()));

		when(keycloakClient.findAllUsers(any(PagingModel.class))).thenReturn(Mono.just(listUser));

		Flux<UserVO> fluxUser = userFinderService.findAll(pagingModel);

		StepVerifier.create(fluxUser).expectNextCount(2).verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("denies access to find all users for roles 'USER' and 'ACTUATOR'")
	@Test
	@WithMockUser(roles = { "USER", "ACTUATOR" })
	void verifyFindAllAccessIsDeniedForUserAndCurator() {

		PagingModel pagingModel = PagingModel.builder().build();

		List<UserRepresentation> listUser = Arrays.asList(UserBuilder.user().userRepresentation(UUID.randomUUID()),
				UserBuilder.user().userRepresentation(UUID.randomUUID()));

		when(keycloakClient.findAllUsers(any(PagingModel.class))).thenReturn(Mono.just(listUser));

		Flux<UserVO> fluxUser = userFinderService.findAll(pagingModel);

		StepVerifier.create(fluxUser).verifyError(AccessDeniedException.class);
	}

	/**
	 * 
	 */
	@DisplayName("denies access to find all users for anonymous user")
	@Test
	void verifyFindAllAccessIsDeniedForUnauthenticated() {

		PagingModel pagingModel = PagingModel.builder().build();

		List<UserRepresentation> listUser = Arrays.asList(UserBuilder.user().userRepresentation(UUID.randomUUID()),
				UserBuilder.user().userRepresentation(UUID.randomUUID()));

		when(keycloakClient.findAllUsers(any(PagingModel.class))).thenReturn(Mono.just(listUser));

		Flux<UserVO> fluxUser = userFinderService.findAll(pagingModel);

		StepVerifier.create(fluxUser).verifyError(AccessDeniedException.class);
	}

	/**
	 * 
	 */
	@DisplayName("grants access to find users by email for role 'ADMIN'")
	@Test
	@WithMockUser(roles = "ADMIN")
	void verifyFindByEmailAccessIsGrantedForAdmin() {

		String email = "john.doe@example.com";
		PagingModel pagingModel = PagingModel.builder().build();

		List<UserRepresentation> listUser = Arrays.asList(UserBuilder.user().userRepresentation(UUID.randomUUID()),
				UserBuilder.user().userRepresentation(UUID.randomUUID()));

		when(keycloakClient.findUserByEmail(anyString(), any(PagingModel.class))).thenReturn(Mono.just(listUser));

		Flux<UserVO> fluxUser = userFinderService.findUserByEmail(email, pagingModel);

		StepVerifier.create(fluxUser).expectNextCount(2).verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("denies access to find all users for roles 'USER' and 'ACTUATOR'")
	@Test
	@WithMockUser(roles = { "USER", "ACTUATOR" })
	void verifyFindByEmailAccessIsDeniedForUserAndActuator() {

		String email = "john.doe@example.com";
		PagingModel pagingModel = PagingModel.builder().build();

		List<UserRepresentation> listUser = Arrays.asList(UserBuilder.user().userRepresentation(UUID.randomUUID()),
				UserBuilder.user().userRepresentation(UUID.randomUUID()));

		when(keycloakClient.findUserByEmail(anyString(), any(PagingModel.class))).thenReturn(Mono.just(listUser));

		Flux<UserVO> fluxUser = userFinderService.findUserByEmail(email, pagingModel);

		StepVerifier.create(fluxUser).verifyError(AccessDeniedException.class);
	}

	/**
	 * 
	 */
	@DisplayName("denies access to find all users for anonymous user")
	@Test
	void verifyFindByEmailAccessIsDeniedForUnauthenticated() {

		String email = "john.doe@example.com";
		PagingModel pagingModel = PagingModel.builder().build();

		List<UserRepresentation> listUser = Arrays.asList(UserBuilder.user().userRepresentation(UUID.randomUUID()),
				UserBuilder.user().userRepresentation(UUID.randomUUID()));

		when(keycloakClient.findUserByEmail(anyString(), any(PagingModel.class))).thenReturn(Mono.just(listUser));

		Flux<UserVO> fluxUser = userFinderService.findUserByEmail(email, pagingModel);

		StepVerifier.create(fluxUser).verifyError(AccessDeniedException.class);
	}

	/**
	 *   
	 */
	@DisplayName("grants access to assign user to groups for role 'ADMIN'")
	@Test
	@WithMockUser(roles = "ADMIN")
	void verifyAssignUserToGroupAccessIsGrantedForAdmin() {

		String id=UUID.randomUUID().toString();
		//
		String groupId=UUID.randomUUID().toString();
		//
		String msg=String.format("User %s has been added to new group '%s'",id,groupId);

		when(keycloakClient.assignUserToGroup(anyString(),anyString())).thenReturn(Mono.just(msg));

		StepVerifier.create(userService.assignToGroup(id,groupId)).expectNextMatches(result->StringUtils.isNotBlank(result)&&result.equals(msg)))).verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("denies access to assign user to groups for roles 'USER' and 'ACTUATOR'")
	@Test
	@WithMockUser(roles = { "USER", "ACTUATOR" })
	void verifyAssignUserToGroupAccessIsDeniedForUserAndActuator() {

		String id = UUID.randomUUID().toString();
		String groupId = UUID.randomUUID().toString();
		String msg = String.format("User %s has been added to new group '%s'", id, groupId);

		when(keycloakClient.assignUserToGroup(anyString(), anyString())).thenReturn(Mono.just(msg));

		StepVerifier.create(userService.assignToGroup(id, groupId)).verifyError(AccessDeniedException.class);
	}

	/**
	 * 
	 */
	@DisplayName("denies access to assign user to groups for anonymous user")
	@Test
	void verifyAssignUserToGroupAccessIsDeniedForUnauthenticated() {

		String id = UUID.randomUUID().toString();
		String groupId = UUID.randomUUID().toString();
		String msg = String.format("User %s has been added to new group '%s'", id, groupId);

		when(keycloakClient.assignUserToGroup(anyString(), anyString())).thenReturn(Mono.just(msg));

		StepVerifier.create(userService.assignToGroup(id, groupId)).verifyError(AccessDeniedException.class);
	}

	/**
	  *   
	  */
	@DisplayName("grants access to assign user to Realm roles for role 'ADMIN'")
	@Test
	@WithMockUser(roles = "ADMIN")
	void verifyAssignUserToRealmRoleAccessIsGrantedForAdmin() {

		String id = UUID.randomUUID().toString();
		String roleName = Role.ROLE_DEVELOPER.getName();
		String msg = String.format("User with id: %s has a new role of '%s'", id, roleName);

		when(keycloakClient.assignRealmRole(anyString(), anyString())).thenReturn(Mono.just(msg));

		StepVerifier.create(userService.assignRealmRole(id, roleName))
				.expectNextMatches(result -> StringUtils.isNotBlank(result) && result.equals(msg)).verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("denies access to assign user to groups for roles 'USER' and 'ACTUATOR'")
	@Test
	@WithMockUser(roles = { "USER", "APP_MANAGER", "MANAGER" })
	void verifyAssignUserToRealmRoleAccessIsDeniedForAllUsersExceptAdmin() {

		String id = UUID.randomUUID().toString();
		String roleName = Role.ROLE_DEVELOPER.getName();
		String msg = String.format("User with id: %s has a new role of '%s'", id, roleName);

		when(keycloakClient.assignRealmRole(anyString(), anyString())).thenReturn(Mono.just(msg));

		StepVerifier.create(userService.assignRealmRole(id, roleName)).verifyError(AccessDeniedException.class);
	}

	/**
	 * 
	 */
	@DisplayName("denies access to assign user to realm role for anonymous user")
	@Test
	void verifyAssignUserToRealmRoleAccessIsDeniedForUnauthenticated() {

		String id = UUID.randomUUID().toString();
		String roleName = Role.ROLE_DEVELOPER.getName();
		String msg = String.format("User with id: %s has a new role of '%s'", id, roleName);

		when(keycloakClient.assignRealmRole(anyString(), anyString())).thenReturn(Mono.just(msg));

		StepVerifier.create(userService.assignRealmRole(id, roleName)).verifyError(AccessDeniedException.class);
	}

	/**
	  *   
	  */
	@DisplayName("grants access to assign user to client role for 'ADMIN'")
	@Test
	@WithMockUser(roles = "ADMIN")
	void verifyAssignUserToClientRoleAccessIsGrantedForAdmin() {

		String id = UUID.randomUUID().toString();
		String clientId = UUID.randomUUID().toString();
		String roleName = Role.ROLE_ACTUATOR.getName();
		String msg = String.format("User with id: %s has a new Client(%s) role of '%s'", id, clientId, roleName);

		when(keycloakClient.assignClientRole(anyString(), anyString(), anyString())).thenReturn(Mono.just(msg));

		StepVerifier.create(userService.assignClientRoleToUser(id, roleName, clientId))
				.expectNextMatches(result -> StringUtils.isNotBlank(result) && result.equals(msg)).verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("denies access to assign user to new client roles for 'USER' and 'MANAGER'")
	@Test
	@WithMockUser(roles = { "USER", "APP_MANAGER", "MANAGER" })
	void verifyAssignUserToClientRoleAccessIsDeniedForAllUsersExceptAdmin() {

		String id = UUID.randomUUID().toString();
		String clientId = UUID.randomUUID().toString();
		String roleName = Role.ROLE_ACTUATOR.getName();
		String msg = String.format("User with id: %s has a new Client(%s) role of '%s'", id, clientId, roleName);

		when(keycloakClient.assignClientRole(anyString(), anyString(), anyString())).thenReturn(Mono.just(msg));

		StepVerifier.create(userService.assignClientRoleToUser(id, roleName, clientId))
				.verifyError(AccessDeniedException.class);
	}

	/**
	 * 
	 */
	@DisplayName("denies access to assign a user to new client role for anonymous user")
	@Test
	void verifyAssignUserToClientRoleAccessIsDeniedForUnauthenticated() {

		String id = UUID.randomUUID().toString();
		String clientId = UUID.randomUUID().toString();
		String roleName = Role.ROLE_ACTUATOR.getName();
		String msg = String.format("User with id: %s has a new Client(%s) role of '%s'", id, clientId, roleName);

		when(keycloakClient.assignClientRole(anyString(), anyString(), anyString())).thenReturn(Mono.just(msg));

		StepVerifier.create(userService.assignClientRoleToUser(id, roleName, clientId))
				.verifyError(AccessDeniedException.class);
	}
}