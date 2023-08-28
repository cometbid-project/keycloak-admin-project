/**
 * 
 */
package com.keycloak.admin.client.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
//import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mockito;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

import com.keycloak.admin.client.common.enums.StatusType;
import com.keycloak.admin.client.common.utils.DateUtil;
import com.keycloak.admin.client.common.utils.LocaleContextUtils;
import com.keycloak.admin.client.common.utils.RandomGenerator;
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;
import com.keycloak.admin.client.config.AppConfiguration;
import com.keycloak.admin.client.config.AuthProfile;
import com.keycloak.admin.client.config.AuthProperties;
import com.keycloak.admin.client.config.LoginNotificationConfig;
import com.keycloak.admin.client.config.MessageConfig;
import com.keycloak.admin.client.config.SchedulerConfig;
import com.keycloak.admin.client.config.SecurityConfig;
import com.keycloak.admin.client.dataacess.LoginLocationBuilder;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.entities.NewLocationToken;
import com.keycloak.admin.client.entities.UserloginLocation;
import com.keycloak.admin.client.exceptions.BadRequestException;
import com.keycloak.admin.client.exceptions.NewLocationTokenValidationException;
import com.keycloak.admin.client.exceptions.UserAlreadyExistException;
import com.keycloak.admin.client.models.LoginLocation;
import com.keycloak.admin.client.models.StatusUpdateRequest;
import com.keycloak.admin.client.models.mappers.UserMapper;
import com.keycloak.admin.client.oauth.service.CommonUtil;
import com.keycloak.admin.client.oauth.service.KeycloakOauthClient;
import com.keycloak.admin.client.oauth.service.UserLocationServiceImpl;
import com.keycloak.admin.client.oauth.service.it.UserLocationService;
import com.keycloak.admin.client.repository.NewLocationTokenRepository;
import com.keycloak.admin.client.repository.UserloginLocationRepository;
import com.maxmind.geoip2.DatabaseReader;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ua_parser.Parser;

/**
 * @author Gbenga
 *
 */
@Log4j2
@WebFluxTest
@DisplayName("Verify User service")
@ContextConfiguration(classes = { 
		AuthProfile.class, AppConfiguration.class, 
		SecurityConfig.class, MessageConfig.class,
		SchedulerConfig.class, LoginNotificationConfig.class })
@Import({ KeycloakOauthClient.class, UserLocationServiceImpl.class, UserMapper.class, LocaleContextUtils.class,
		CustomMessageSourceAccessor.class, ApplicationEventPublisher.class, CommonUtil.class })
//@ExtendWith({ SpringExtension.class })
class LocationServiceAuthorizationTest {

	@Autowired
	private UserLocationService userLocationService;

	@MockBean
	private KeycloakOauthClient keycloakClient;
	@MockBean
	private CommonUtil commonUtil;
	@MockBean
	private ApplicationEventPublisher eventPublisher;
	@MockBean
	private UserloginLocationRepository successLoginRepository;
	@MockBean
	private NewLocationTokenRepository newLocationTokenRepository;

	private static ServerWebExchange sweExchange;

	@MockBean
	private ServerRequest mockServerRequest;
	@MockBean
	private ServerWebExchange mockServerWebExchange;
	@MockBean
	private ServerHttpRequest mockHttpRequest;
	@MockBean
	private InetSocketAddress mockInetSocketAddr;

	private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel MacOS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.114 Safari/537.36";

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

	}

	@BeforeAll
	static void initializeData() throws FileNotFoundException, IOException, URISyntaxException {

		// BlockHound.install(new ReactorBlockHoundIntegration(), new MyIntegration());
		// =============================================================================

		HttpHeaders headers = new HttpHeaders();
		headers.add("Forwarded", "host=84.198.58.199;proto=https");
		headers.add("User-Agent", USER_AGENT);

		ServerHttpRequest httpRequest = MockServerHttpRequest
				.method(HttpMethod.GET, new URI("http://example.com/a%20b?q=a%2Bb"))
				.contentType(MediaType.APPLICATION_JSON).headers(headers).build();

		sweExchange = MockServerWebExchange.from((MockServerHttpRequest) httpRequest);

		ServerRequest other = ServerRequest.create(sweExchange, HandlerStrategies.withDefaults().messageReaders());

		DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
		Flux<DataBuffer> body = Flux.just("baz").map(s -> s.getBytes(StandardCharsets.UTF_8))
				.map(dataBufferFactory::wrap);

	}

	@SuppressWarnings("unchecked")
	private void createUserPostConditions(UUID userId, String username)
			throws UnknownHostException, URISyntaxException {

		LoginLocationBuilder loginBuilder = LoginLocationBuilder.loginLoc();
		UserloginLocation usrLoginLoc = loginBuilder.withUsername(username).build();

		when(successLoginRepository.findOneByUsernameIgnoreCase(anyString())).thenReturn(Mono.just(usrLoginLoc));
		// when(successLoginRepository.findOneByUsernameIgnoreCase(anyString())).thenReturn(Mono.empty());
		when(successLoginRepository.save(any(UserloginLocation.class))).thenReturn(Mono.just(usrLoginLoc));
		when(keycloakClient.updateUserStatus(username, any(StatusUpdateRequest.class)))
				.thenReturn(Mono.just(AuthProperties.SUCCESS));

		NewLocationToken newestToken = loginBuilder.newLoginLocToken();

		when(newLocationTokenRepository.save(any(NewLocationToken.class))).thenReturn(Mono.just(newestToken));
		when(commonUtil.recordNewUserLocation(anyString(), any(ServerHttpRequest.class)))
				.thenReturn(Mono.just(usrLoginLoc));
	}

	/**
	 * @throws UnknownHostException
	 * @throws URISyntaxException
	 * 
	 */
	@DisplayName("grants access to record user location for Authenticated users")
	@Disabled
	@WithMockUser(roles = { "USER", "APP_MANAGER", "MANAGER", "ADMIN" })
	void verifyRecordUserLocationAccessIsGrantedForAuthenticatedUsers()
			throws UnknownHostException, URISyntaxException {
		String ipAddress = "166.197.174.199";
		UUID userId = UUID.randomUUID();
		UserRepresentation userRepresentation = UserBuilder.user().userRepresentation(userId);
		String username = userRepresentation.getUsername();

		createUserPostConditions(userId, username);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Forwarded", "host=84.198.58.199;proto=https");
		// headers.add("X-Forwarded-For", "103.0.513.125, 80.91.3.17, 120.192.335.629");
		headers.add("User-Agent", USER_AGENT);

		// InetSocketAddress inetSocketAddress = new InetSocketAddress("103.0.513.125",
		// 2222);
		InetSocketAddress mockInetSocketAddress = Mockito.mock(InetSocketAddress.class);

		ServerHttpRequest serverHttpRequest = MockServerHttpRequest
				.method(HttpMethod.GET, new URI("http://example.com/a%20b?q=a%2Bb"))
				.contentType(MediaType.APPLICATION_JSON).headers(headers).remoteAddress(mockInetSocketAddress).build();

		InetAddress inetAddress = InetAddress.getByName("example.com");
		InetAddress mockInetAddress = Mockito.mock(InetAddress.class);
		Mockito.when(mockInetAddress.getHostAddress()).thenReturn(ipAddress);

		Mockito.when(mockInetSocketAddress.getAddress()).thenReturn(inetAddress);
		Mockito.when(mockInetSocketAddress.getPort()).thenReturn(2222);

		StepVerifier.create(userLocationService.recordNewUserLocation(username, serverHttpRequest))
				// .expectNextCount(1)
				.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result.getId())
						&& StringUtils.isNotBlank(result.getUsername()) && result.getUsername().equals(username))
				.verifyComplete();
	}

	/**
	 * @throws UnknownHostException
	 * @throws URISyntaxException
	 * 
	 */
	@DisplayName("grants access to record user location for Anonymous user")
	@Disabled
	void verifyRecordUserLocationAccessIsGrantedForAnonymousUser() throws UnknownHostException, URISyntaxException {
		String ipAddress = "166.197.174.199";
		UUID userId = UUID.randomUUID();
		UserRepresentation userRepresentation = UserBuilder.user().userRepresentation(userId);
		String username = userRepresentation.getUsername();

		createUserPostConditions(userId, username);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Forwarded", "host=84.198.58.199;proto=https");
		headers.add("X-Forwarded-For", "103.0.513.125, 80.91.3.17, 120.192.335.629"); // enabled
		headers.add("User-Agent", USER_AGENT);

		InetSocketAddress mockInetSocketAddress = Mockito.mock(InetSocketAddress.class);

		ServerHttpRequest serverHttpRequest = MockServerHttpRequest
				.method(HttpMethod.GET, new URI("http://example.com/a%20b?q=a%2Bb"))
				.contentType(MediaType.APPLICATION_JSON).headers(headers).remoteAddress(mockInetSocketAddress).build();

		InetAddress inetAddress = InetAddress.getByName("example.com");
		InetAddress mockInetAddress = Mockito.mock(InetAddress.class);
		Mockito.when(mockInetAddress.getHostAddress()).thenReturn(ipAddress);

		Mockito.when(mockInetSocketAddress.getAddress()).thenReturn(inetAddress);
		Mockito.when(mockInetSocketAddress.getPort()).thenReturn(2222);

		StepVerifier.create(userLocationService.recordNewUserLocation(username, serverHttpRequest))
				// .expectNextCount(1)
				.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result.getId())
						&& StringUtils.isNotBlank(result.getUsername()) && result.getUsername().equals(username))
				.verifyComplete();
	}

	/**
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 * 
	 */
	@DisplayName("grants access to decode user location for Anonymous user")
	@Disabled
	void verifyDecodeUserLocationAccessIsGrantedForAnonymousUser() throws UnknownHostException, URISyntaxException {
		String ipAddress = "166.197.174.199";

		HttpHeaders headers = new HttpHeaders();
		headers.add("Forwarded", "host=84.198.58.199;proto=https");
		headers.add("X-Forwarded-For", "103.0.513.125, 80.91.3.17, 120.192.335.629"); // enabled
		headers.add("User-Agent", USER_AGENT);

		InetSocketAddress mockInetSocketAddress = Mockito.mock(InetSocketAddress.class);

		ServerHttpRequest serverHttpRequest = MockServerHttpRequest
				.method(HttpMethod.GET, new URI("http://example.com/a%20b?q=a%2Bb"))
				.contentType(MediaType.APPLICATION_JSON).headers(headers).remoteAddress(mockInetSocketAddress).build();

		InetAddress inetAddress = InetAddress.getByName("example.com");
		InetAddress mockInetAddress = Mockito.mock(InetAddress.class);
		Mockito.when(mockInetAddress.getHostAddress()).thenReturn(ipAddress);

		Mockito.when(mockInetSocketAddress.getAddress()).thenReturn(inetAddress);
		Mockito.when(mockInetSocketAddress.getPort()).thenReturn(2222);

		StepVerifier.create(userLocationService.decodeUserLocation(serverHttpRequest))
				// .expectNextCount(1)
				.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result.getIpAddr())
						&& StringUtils.isNotBlank(result.getDeviceDetails()))
				.verifyComplete();
	}

	/**
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 * 
	 */
	@DisplayName("grants access to decode user location for Authenticated users")
	@Disabled
	@WithMockUser(roles = { "USER", "APP_MANAGER", "MANAGER", "ADMIN" })
	void verifyDecodeUserLocationAccessIsGrantedForAuthenticatedUsers()
			throws UnknownHostException, URISyntaxException {
		String ipAddress = "166.197.174.199";

		HttpHeaders headers = new HttpHeaders();
		headers.add("Forwarded", "host=84.198.58.199;proto=https");
		// headers.add("X-Forwarded-For", "103.0.513.125, 80.91.3.17, 120.192.335.629");
		// // enabled
		headers.add("User-Agent", USER_AGENT);

		InetSocketAddress mockInetSocketAddress = Mockito.mock(InetSocketAddress.class);

		ServerHttpRequest serverHttpRequest = MockServerHttpRequest
				.method(HttpMethod.GET, new URI("http://example.com/a%20b?q=a%2Bb"))
				.contentType(MediaType.APPLICATION_JSON).headers(headers).remoteAddress(mockInetSocketAddress).build();

		InetAddress inetAddress = InetAddress.getByName("example.com");
		InetAddress mockInetAddress = Mockito.mock(InetAddress.class);
		Mockito.when(mockInetAddress.getHostAddress()).thenReturn(ipAddress);

		Mockito.when(mockInetSocketAddress.getAddress()).thenReturn(inetAddress);
		Mockito.when(mockInetSocketAddress.getPort()).thenReturn(2222);

		StepVerifier.create(userLocationService.decodeUserLocation(serverHttpRequest))
				// .expectNextCount(1)
				.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result.getIpAddr())
						&& StringUtils.isNotBlank(result.getDeviceDetails()))
				.verifyComplete();
	}

	/**
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 * 
	 */
	@DisplayName("grants access to is new login location for Authenticated users")
	@Disabled
	@WithMockUser(roles = { "USER", "APP_MANAGER", "MANAGER", "ADMIN" })
	void verifyIsNewLoginLocationAccessIsGrantedForAuthenticatedUsers()
			throws UnknownHostException, URISyntaxException {

		String ipAddress = "166.197.174.199";
		UUID userId = UUID.randomUUID();
		UserRepresentation userRepresentation = UserBuilder.user().userRepresentation(userId);
		String username = userRepresentation.getUsername();

		createUserPostConditions(userId, username);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Forwarded", "host=84.198.58.199;proto=https");
		// headers.add("X-Forwarded-For", "103.0.513.125, 80.91.3.17, 120.192.335.629");
		// // enabled
		headers.add("User-Agent", USER_AGENT);

		InetSocketAddress mockInetSocketAddress = Mockito.mock(InetSocketAddress.class);

		ServerHttpRequest serverHttpRequest = MockServerHttpRequest
				.method(HttpMethod.GET, new URI("http://example.com/a%20b?q=a%2Bb"))
				.contentType(MediaType.APPLICATION_JSON).headers(headers).remoteAddress(mockInetSocketAddress).build();

		InetAddress inetAddress = InetAddress.getByName("example.com");
		InetAddress mockInetAddress = Mockito.mock(InetAddress.class);
		Mockito.when(mockInetAddress.getHostAddress()).thenReturn(ipAddress);

		Mockito.when(mockInetSocketAddress.getAddress()).thenReturn(inetAddress);
		Mockito.when(mockInetSocketAddress.getPort()).thenReturn(2222);

		StepVerifier.create(userLocationService.isNewLoginLocation(username, serverHttpRequest))
				// .expectNextCount(1)
				.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result.getId())
						&& StringUtils.isNotBlank(result.getStateCode())
						&& StringUtils.isNotBlank(result.getCountryCode())
						&& StringUtils.isNotBlank(result.getUsername()) && StringUtils.isNotBlank(result.getToken())
						&& result.getCreationDate() != null)
				.verifyComplete();
	}

	/**
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 * 
	 */
	@DisplayName("grants access to is new login location for Authenticated users")
	@Disabled
	@WithMockUser(roles = { "USER", "APP_MANAGER", "MANAGER", "ADMIN" })
	void verifyIsFirstLoginLocationAccessIsGrantedForAuthenticatedUsers()
			throws UnknownHostException, URISyntaxException {

		String ipAddress = "166.197.174.199";
		UUID userId = UUID.randomUUID();
		UserRepresentation userRepresentation = UserBuilder.user().userRepresentation(userId);
		String username = userRepresentation.getUsername();

		LoginLocationBuilder loginBuilder = LoginLocationBuilder.loginLoc();
		UserloginLocation usrLoginLoc = loginBuilder.withUsername(username).build();

		when(successLoginRepository.findOneByUsernameIgnoreCase(anyString())).thenReturn(Mono.empty());
		when(successLoginRepository.save(any(UserloginLocation.class))).thenReturn(Mono.just(usrLoginLoc));
		when(keycloakClient.updateUserStatus(username, any(StatusUpdateRequest.class)))
				.thenReturn(Mono.just(AuthProperties.SUCCESS));

		NewLocationToken newestToken = loginBuilder.newLoginLocToken();

		when(newLocationTokenRepository.save(any(NewLocationToken.class))).thenReturn(Mono.just(newestToken));
		when(commonUtil.recordNewUserLocation(anyString(), any(ServerHttpRequest.class)))
				.thenReturn(Mono.just(usrLoginLoc));

		HttpHeaders headers = new HttpHeaders();
		headers.add("Forwarded", "host=84.198.58.199;proto=https");
		// headers.add("X-Forwarded-For", "103.0.513.125, 80.91.3.17, 120.192.335.629");
		// // enabled
		headers.add("User-Agent", USER_AGENT);

		InetSocketAddress mockInetSocketAddress = Mockito.mock(InetSocketAddress.class);

		ServerHttpRequest serverHttpRequest = MockServerHttpRequest
				.method(HttpMethod.GET, new URI("http://example.com/a%20b?q=a%2Bb"))
				.contentType(MediaType.APPLICATION_JSON).headers(headers).remoteAddress(mockInetSocketAddress).build();

		InetAddress inetAddress = InetAddress.getByName("example.com");
		InetAddress mockInetAddress = Mockito.mock(InetAddress.class);
		Mockito.when(mockInetAddress.getHostAddress()).thenReturn(ipAddress);

		Mockito.when(mockInetSocketAddress.getAddress()).thenReturn(inetAddress);
		Mockito.when(mockInetSocketAddress.getPort()).thenReturn(2222);

		StepVerifier.create(userLocationService.isNewLoginLocation(username, serverHttpRequest))
				// .expectNextCount(1)
				.expectNextMatches(result -> result != null && StringUtils.isBlank(result.getId())
						&& StringUtils.isBlank(result.getStateCode()) && StringUtils.isBlank(result.getCountryCode())
						&& StringUtils.isBlank(result.getUsername()) && StringUtils.isBlank(result.getToken())
						&& result.getCreationDate() == null)
				.verifyComplete();
	}

	/**
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 * 
	 */
	@DisplayName("grants access to process new user location check no login History for Anonymous user")
	@Disabled
	void verifyProcessNewLocationNoExistingRecordAccessIsGrantedForAnonymousUser()
			throws UnknownHostException, URISyntaxException {
		String ipAddress = "166.197.174.199";
		UUID userId = UUID.randomUUID();
		UserRepresentation userRepresentation = UserBuilder.user().userRepresentation(userId);
		String username = userRepresentation.getUsername();

		LoginLocationBuilder loginBuilder = LoginLocationBuilder.loginLoc();
		UserloginLocation usrLoginLoc = loginBuilder.withUsername(username).build();

		when(successLoginRepository.findOneByUsernameIgnoreCase(anyString())).thenReturn(Mono.empty());
		when(successLoginRepository.save(any(UserloginLocation.class))).thenReturn(Mono.just(usrLoginLoc));
		when(keycloakClient.updateUserStatus(username, any(StatusUpdateRequest.class)))
				.thenReturn(Mono.just(AuthProperties.SUCCESS));

		NewLocationToken newestToken = loginBuilder.newLoginLocToken();

		when(newLocationTokenRepository.save(any(NewLocationToken.class))).thenReturn(Mono.just(newestToken));
		when(commonUtil.recordNewUserLocation(anyString(), any(ServerHttpRequest.class)))
				.thenReturn(Mono.just(usrLoginLoc));

		HttpHeaders headers = new HttpHeaders();
		headers.add("Forwarded", "host=84.198.58.199;proto=https");
		// headers.add("X-Forwarded-For", "103.0.513.125, 80.91.3.17, 120.192.335.629");
		// // enabled
		headers.add("User-Agent", USER_AGENT);

		InetSocketAddress mockInetSocketAddress = Mockito.mock(InetSocketAddress.class);

		ServerHttpRequest serverHttpRequest = MockServerHttpRequest
				.method(HttpMethod.GET, new URI("http://example.com/a%20b?q=a%2Bb"))
				.contentType(MediaType.APPLICATION_JSON).headers(headers).remoteAddress(mockInetSocketAddress).build();

		InetAddress inetAddress = InetAddress.getByName("example.com");
		InetAddress mockInetAddress = Mockito.mock(InetAddress.class);
		Mockito.when(mockInetAddress.getHostAddress()).thenReturn(ipAddress);

		Mockito.when(mockInetSocketAddress.getAddress()).thenReturn(inetAddress);
		Mockito.when(mockInetSocketAddress.getPort()).thenReturn(2222);

		StepVerifier.create(userLocationService.processNewLocationCheck(username, serverHttpRequest))
				// .expectNextCount(1)
				.expectNextMatches(result -> result.equals(AuthProperties.COMPLETED) && StringUtils.isNotBlank(result))
				.verifyComplete();

	}

	/**
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 * 
	 */
	@DisplayName("grants access to process new user location check with existing login history for Anonymous user")
	@Test
	void verifyProcessNewLocationWithExistingRecordAccessIsGrantedForAnonymousUser()
			throws UnknownHostException, URISyntaxException {
		String ipAddress = "166.197.174.199";
		UUID userId = UUID.randomUUID();
		UserRepresentation userRepresentation = UserBuilder.user().userRepresentation(userId);
		String username = userRepresentation.getUsername();

		createUserPostConditions(userId, username);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Forwarded", "host=84.198.58.199;proto=https");
		// headers.add("X-Forwarded-For", "103.0.513.125, 80.91.3.17, 120.192.335.629");
		headers.add("User-Agent", USER_AGENT);

		InetSocketAddress mockInetSocketAddress = Mockito.mock(InetSocketAddress.class);

		ServerHttpRequest serverHttpRequest = MockServerHttpRequest
				.method(HttpMethod.GET, new URI("http://example.com/a%20b?q=a%2Bb"))
				.contentType(MediaType.APPLICATION_JSON).headers(headers).remoteAddress(mockInetSocketAddress).build();

		InetAddress inetAddress = InetAddress.getByName("example.com");
		InetAddress mockInetAddress = Mockito.mock(InetAddress.class);
		Mockito.when(mockInetAddress.getHostAddress()).thenReturn(ipAddress);

		Mockito.when(mockInetSocketAddress.getAddress()).thenReturn(inetAddress);
		Mockito.when(mockInetSocketAddress.getPort()).thenReturn(2222);

		StepVerifier.create(userLocationService.processNewLocationCheck(username, serverHttpRequest))
				// .expectNextCount(1)
				.expectNextMatches(result -> result.equals(AuthProperties.COMPLETED) && StringUtils.isNotBlank(result))
				.verifyComplete();

	}

	/**
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 * 
	 */
	@DisplayName("grants access for validate new Location token to Anonymous user")
	@Disabled
	void verifyIsValidNewLocationTokenAccessIsGrantedForAnonymousUser()
			throws UnknownHostException, URISyntaxException {

		UUID userId = UUID.randomUUID();
		UserRepresentation userRepresentation = UserBuilder.user().userRepresentation(userId);
		String username = userRepresentation.getUsername();

		LoginLocationBuilder loginBuilder = LoginLocationBuilder.loginLoc();		
		UserloginLocation usrLoginLoc = loginBuilder.withUsername(username).build();
		
		LoginLocation loginLoc = ((List<LoginLocation>)usrLoginLoc.getDisabledLocations()).get(0);
		NewLocationToken newestToken = loginBuilder
						.newLoginLocToken(
								loginLoc.getCountryCode(), 
								loginLoc.getStateCode());
		
		when(successLoginRepository.findOneByUsernameIgnoreCase(anyString())).thenReturn(Mono.just(usrLoginLoc));
		// when(successLoginRepository.findOneByUsernameIgnoreCase(anyString())).thenReturn(Mono.empty());
		when(successLoginRepository.save(any(UserloginLocation.class))).thenReturn(Mono.just(usrLoginLoc));
		when(keycloakClient.updateUserStatus(username, any(StatusUpdateRequest.class)))
				.thenReturn(Mono.just(AuthProperties.SUCCESS));

		when(newLocationTokenRepository.findByToken(anyString())).thenReturn(Mono.just(newestToken));
		when(newLocationTokenRepository.delete(any(NewLocationToken.class))).thenReturn(Mono.empty());

		String token = newestToken.getToken();

		StepVerifier.create(userLocationService.isValidNewLocationToken(token))
				// .expectNextCount(1)
				.expectNextMatches(result -> StringUtils.isNotBlank(result)).verifyComplete();
	}
	
	/**
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 * 
	 */
	@DisplayName("grants access for validate new Location token with token not found to Anonymous user")
	@Disabled
	void verifyIsValidNewLocationTokenWithTokenNotfoundAccessIsGrantedForAnonymousUser()
			throws UnknownHostException, URISyntaxException {

		UUID userId = UUID.randomUUID();
		UserRepresentation userRepresentation = UserBuilder.user().userRepresentation(userId);
		String username = userRepresentation.getUsername();

		LoginLocationBuilder loginBuilder = LoginLocationBuilder.loginLoc();
		NewLocationToken newestToken = loginBuilder.newLoginLocToken();
		UserloginLocation usrLoginLoc = loginBuilder.withUsername(username).build();

		when(successLoginRepository.findOneByUsernameIgnoreCase(anyString())).thenReturn(Mono.just(usrLoginLoc));
		// when(successLoginRepository.findOneByUsernameIgnoreCase(anyString())).thenReturn(Mono.empty());
		when(successLoginRepository.save(any(UserloginLocation.class))).thenReturn(Mono.just(usrLoginLoc));
		when(keycloakClient.updateUserStatus(username, any(StatusUpdateRequest.class)))
				.thenReturn(Mono.just(AuthProperties.SUCCESS));

		when(newLocationTokenRepository.findByToken(anyString())).thenReturn(Mono.empty());
		when(newLocationTokenRepository.delete(any(NewLocationToken.class))).thenReturn(Mono.empty());

		String token = "";
		StepVerifier.create(userLocationService.isValidNewLocationToken(token))
					.expectError(BadRequestException.class).verify();
		
		token = newestToken.getToken();		
		StepVerifier.create(userLocationService.isValidNewLocationToken(token))
				.expectError(NewLocationTokenValidationException.class).verify();
	}
	
}
