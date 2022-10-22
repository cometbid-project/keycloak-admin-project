/**
 * 
 */
package com.keycloak.admin.client.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

import com.github.javafaker.Faker;
import com.keycloak.admin.client.KeycloakAdminProjectApplication;
import com.keycloak.admin.client.common.enums.StatusType;
import com.keycloak.admin.client.common.geo.GeolocationUtils;
import com.keycloak.admin.client.common.utils.LocaleContextUtils;
import com.keycloak.admin.client.common.utils.RandomGenerator;
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;
import com.keycloak.admin.client.config.AppConfiguration;
import com.keycloak.admin.client.config.AuthProfile;
import com.keycloak.admin.client.config.AuthProperties;
import com.keycloak.admin.client.config.LoginNotificationConfig;
import com.keycloak.admin.client.config.MessageConfig;
import com.keycloak.admin.client.config.SecurityConfig;
import com.keycloak.admin.client.dataacess.AuthBuilder;
import com.keycloak.admin.client.dataacess.LoginLocationBuilder;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.exceptions.ApplicationDefinedRuntimeException;
import com.keycloak.admin.client.exceptions.AuthenticationError;
import com.keycloak.admin.client.exceptions.BlockedCredentialsAttemptsLoginWarning;
import com.keycloak.admin.client.exceptions.SessionExpiredException;
import com.keycloak.admin.client.models.AuthenticationRequest;
import com.keycloak.admin.client.models.AuthenticationResponse;
import com.keycloak.admin.client.models.EmailStatusUpdateRequest;
import com.keycloak.admin.client.models.EnableTwoFactorAuthResponse;
import com.keycloak.admin.client.models.LoginLocation;
import com.keycloak.admin.client.models.ProfileStatusUpdateRequest;
import com.keycloak.admin.client.models.SendOtpRequest;
import com.keycloak.admin.client.models.StatusUpdateRequest;
import com.keycloak.admin.client.models.TotpRequest;
import com.keycloak.admin.client.models.UserDetailsUpdateRequest;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.models.mappers.GroupMapper;
import com.keycloak.admin.client.models.mappers.UserActivationMapper;
import com.keycloak.admin.client.oauth.service.CommonUtil;
import com.keycloak.admin.client.oauth.service.GatewayRedisCache;
import com.keycloak.admin.client.oauth.service.GroupServiceImpl;
import com.keycloak.admin.client.oauth.service.KeycloakOauthClient;
import com.keycloak.admin.client.oauth.service.KeycloakRestService;
import com.keycloak.admin.client.oauth.service.UserAuthenticationServiceImpl;
import com.keycloak.admin.client.oauth.service.UserLocationServiceImpl;
import com.keycloak.admin.client.oauth.service.it.KeycloakOauthClientService;
import com.keycloak.admin.client.oauth.service.it.UserAuthenticationService;
import com.keycloak.admin.client.oauth.service.it.UserLocationService;
import com.keycloak.admin.client.token.utils.TotpManager;
import com.keycloak.admin.client.token.utils.TotpManagerImpl;
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
@DisplayName("Verify Authentication service")
@ContextConfiguration(classes = { AuthProfile.class, AppConfiguration.class, SecurityConfig.class, MessageConfig.class,
		AuthProperties.class, LoginNotificationConfig.class })
@Import({ UserAuthenticationServiceImpl.class, UserActivationMapper.class, LocaleContextUtils.class,
		KeycloakOauthClient.class, UserLocationServiceImpl.class, CustomMessageSourceAccessor.class,
		ApplicationEventPublisher.class, CommonUtil.class, TotpManagerImpl.class })
//@ExtendWith({ SpringExtension.class })
class AuthServiceAuthorizationTest {

	@MockBean
	private UserLocationService userLocationService;

	@Autowired
	private UserAuthenticationService userAuthService;
	private static DatabaseReader databaseReader;
	private static Parser parser;

	@MockBean(name = "keycloak-client")
	private KeycloakOauthClientService keycloakClient;
	@MockBean
	private KeycloakRestService keycloakRestService;

	@MockBean
	private GatewayRedisCache redisCache;
	@MockBean(name = "TotpManager")
	private TotpManager totpManager;

	@MockBean
	private CommonUtil commonUtil;
	@MockBean
	private ApplicationEventPublisher eventPublisher;

	@MockBean
	private ServerRequest mockServerRequest;
	@MockBean
	private ServerWebExchange mockServerWebExchange;
	@MockBean
	private ServerHttpRequest mockHttpRequest;
	@MockBean
	private InetSocketAddress mockInetSocketAddr;

	private static ServerWebExchange sweExchange;
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
	}

	/**
	 * Re-test
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@DisplayName("grants access for login to roles 'USER', 'APP_MANAGER', 'MANAGER' and 'ADMIN'")
	@Disabled
	@WithMockUser(roles = { "USER", "APP_MANAGER", "MANAGER", "ADMIN" })
	void verifyAuthenticateUserAccessIsGrantedForAllRoles() throws URISyntaxException, UnknownHostException {

		String ipAddress = "166.197.174.199";
		UserBuilder user = UserBuilder.user();

		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);

		UserRepresentation userRepresentation = user.userRepresentation(userId);
		String username = userRepresentation.getUsername();

		AuthBuilder authBuilder = AuthBuilder.auth(userVO);
		AuthenticationRequest authRequest = authBuilder.build();
		AuthenticationResponse authResponse = authBuilder.authResponse();

		when(keycloakClient.findUserByUsername(any(String.class))).thenReturn(Mono.just(userRepresentation));
		when(redisCache.isBlockedIp(anyString())).thenReturn(Mono.just(Boolean.FALSE));
		when(keycloakClient.passwordGrantLogin(any(AuthenticationRequest.class))).thenReturn(Mono.just(authResponse));
		when(userLocationService.processNewLocationCheck(anyString(), any(ServerHttpRequest.class)))
				.thenReturn(Mono.just(AuthProperties.COMPLETED));

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

		Mono<AuthenticationResponse> monoUser = userAuthService.authenticate(authRequest, serverHttpRequest);

		StepVerifier.create(monoUser)
				// .expectNextCount(1)
				.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result.getAccessToken())
						&& StringUtils.isNotBlank(result.getUsername()) && result.getUsername().equals(username)
						&& StringUtils.isNotBlank(result.getRefreshToken()))
				.verifyComplete();
	}

	/**
	 * Re-test
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@DisplayName("grants access for login to anonymous User")
	@Disabled
	void verifyAuthenticateUserAccessIsGrantedForAnonymousUser() throws URISyntaxException, UnknownHostException {

		String ipAddress = "166.197.174.199";
		UserBuilder user = UserBuilder.user();

		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);

		UserRepresentation userRepresentation = user.userRepresentation(userId);
		String username = userRepresentation.getUsername();

		AuthBuilder authBuilder = AuthBuilder.auth(userVO);
		AuthenticationRequest authRequest = authBuilder.build();
		AuthenticationResponse authResponse = authBuilder.authResponse();

		when(keycloakClient.findUserByUsername(any(String.class))).thenReturn(Mono.just(userRepresentation));
		when(redisCache.isBlockedIp(anyString())).thenReturn(Mono.just(Boolean.FALSE));
		when(keycloakClient.passwordGrantLogin(any(AuthenticationRequest.class))).thenReturn(Mono.just(authResponse));
		when(userLocationService.processNewLocationCheck(anyString(), any(ServerHttpRequest.class)))
				.thenReturn(Mono.just(AuthProperties.COMPLETED));

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

		Mono<AuthenticationResponse> monoUser = userAuthService.authenticate(authRequest, serverHttpRequest);

		StepVerifier.create(monoUser)
				// .expectNextCount(1)
				.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result.getAccessToken())
						&& StringUtils.isNotBlank(result.getUsername()) && result.getUsername().equals(username)
						&& StringUtils.isNotBlank(result.getRefreshToken()))
				.verifyComplete();
	}

	/**
	 * Re-test
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@DisplayName("grants access for login with authentication error to anonymous User")
	@Disabled
	void verifyAuthenticateUserWithAuthenticationErrorAccessIsGrantedForAnonymousUser()
			throws URISyntaxException, UnknownHostException {

		String ipAddress = "166.197.174.199";
		UserBuilder user = UserBuilder.user();

		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);

		UserRepresentation userRepresentation = user.userRepresentation(userId);
		String username = userRepresentation.getUsername();

		AuthBuilder authBuilder = AuthBuilder.auth(userVO);
		AuthenticationRequest authRequest = authBuilder.build();
		AuthenticationResponse authResponse = authBuilder.authResponse();

		when(redisCache.incrementFailedLogin(anyString())).thenReturn(Mono.just(1L));
		when(redisCache.saveAsBlockedIp(anyString())).thenReturn(Mono.just(2L));

		when(redisCache.isBlockedIp(anyString())).thenReturn(Mono.just(Boolean.FALSE));
		when(keycloakClient.passwordGrantLogin(any(AuthenticationRequest.class)))
				.thenReturn(Mono.error(new AuthenticationError("login failed", new Object[] {})));

		when(keycloakClient.findUserByUsername(any(String.class))).thenReturn(Mono.just(userRepresentation));
		when(userLocationService.processNewLocationCheck(anyString(), any(ServerHttpRequest.class)))
				.thenReturn(Mono.just(AuthProperties.COMPLETED));

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

		Mono<AuthenticationResponse> monoUser = userAuthService.authenticate(authRequest, serverHttpRequest);

		StepVerifier.create(monoUser).expectError(ApplicationDefinedRuntimeException.class).verify();
	}

	/**
	 * Re-test
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@DisplayName("grants access attempted login with Blocked IP/Username to anonymous User")
	@Disabled
	void verifyAttemptedLoginWithBlockedIpOrUsernameAccessIsGrantedForAnonymousUser()
			throws URISyntaxException, UnknownHostException {

		String ipAddress = "166.197.174.199";
		UserBuilder user = UserBuilder.user();

		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);

		UserRepresentation userRepresentation = user.userRepresentation(userId);
		String username = userRepresentation.getUsername();

		AuthBuilder authBuilder = AuthBuilder.auth(userVO);
		AuthenticationRequest authRequest = authBuilder.build();
		AuthenticationResponse authResponse = authBuilder.authResponse();

		when(keycloakClient.findUserByUsername(any(String.class))).thenReturn(Mono.just(userRepresentation));
		when(redisCache.isBlockedIp(anyString())).thenReturn(Mono.just(Boolean.TRUE));
		when(keycloakClient.passwordGrantLogin(any(AuthenticationRequest.class))).thenReturn(Mono.just(authResponse));
		when(userLocationService.processNewLocationCheck(anyString(), any(ServerHttpRequest.class)))
				.thenReturn(Mono.just(AuthProperties.COMPLETED));

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

		Mono<AuthenticationResponse> monoUser = userAuthService.authenticate(authRequest, serverHttpRequest);

		StepVerifier.create(monoUser).expectError(BlockedCredentialsAttemptsLoginWarning.class).verify();
	}

	/**
	 * Re-test
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@DisplayName("grants access increment failed login to anonymous User")
	@Disabled
	void verifyIncrementFailedLoginAccessIsGrantedForAnonymousUser() throws URISyntaxException, UnknownHostException {
		String ipAddress = "166.197.174.199";
		UserBuilder user = UserBuilder.user();

		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);

		when(redisCache.incrementFailedLogin(anyString())).thenReturn(Mono.just(1L));
		when(redisCache.saveAsBlockedIp(anyString())).thenReturn(Mono.just(2L));
		when(redisCache.removeCacheEntry(anyString())).thenReturn(Mono.just(Boolean.TRUE));

		UserRepresentation userRepresentation = user.userRepresentation(userId);
		String username = userRepresentation.getUsername();

		Mono<Long> monoUser = userAuthService.incrementFailedLogins(ipAddress, username);

		StepVerifier.create(monoUser).expectNextMatches(result -> result != null && result == 1).verifyComplete();
	}

	/**
	 * Re-test
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@DisplayName("grants access increment failed login with max attempt reached to anonymous User")
	@Disabled
	void verifyIncrementFailedLoginMaxAttemptReachedAccessIsGrantedForAnonymousUser()
			throws URISyntaxException, UnknownHostException {
		String ipAddress = "166.197.174.199";
		UserBuilder user = UserBuilder.user();

		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);

		// Assuming maximum failed login attempt is 5
		when(redisCache.incrementFailedLogin(anyString())).thenReturn(Mono.just(5L));
		when(redisCache.saveAsBlockedIp(anyString())).thenReturn(Mono.just(2L));
		when(redisCache.removeCacheEntry(anyString())).thenReturn(Mono.just(Boolean.TRUE));

		UserRepresentation userRepresentation = user.userRepresentation(userId);
		String username = userRepresentation.getUsername();

		Mono<Long> monoUser = userAuthService.incrementFailedLogins(ipAddress, username);

		StepVerifier.create(monoUser).expectNextMatches(result -> result != null && result == 5).verifyComplete();
	}

	/**
	 * Re-test
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@DisplayName("grants access validate totp code to anonymous User")
	@Disabled
	void verifyTotpVerificationAccessIsGrantedForAnonymousUser() throws URISyntaxException, UnknownHostException {
		String ipAddress = "166.197.174.199";
		UserBuilder user = UserBuilder.user();

		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);

		UserRepresentation userRepresentation = user.userRepresentation(userId);
		String username = userRepresentation.getUsername();

		AuthBuilder authBuilder = AuthBuilder.auth(userVO);
		AuthenticationResponse authResponse = authBuilder.authResponse();

		// Assuming maximum failed login attempt is 5
		when(redisCache.getAuthenticationResponse(anyString())).thenReturn(Mono.just(authResponse));
		when(redisCache.saveAsBlockedIp(anyString())).thenReturn(Mono.just(2L));
		when(redisCache.removeCacheEntry(anyString())).thenReturn(Mono.just(Boolean.TRUE));
		when(redisCache.isBlockedIp(anyString())).thenReturn(Mono.just(Boolean.FALSE));

		when(keycloakRestService.refreshAccessToken(anyString(), anyString())).thenReturn(Mono.just(authResponse));
		when(userLocationService.processNewLocationCheck(anyString(), any(ServerHttpRequest.class)))
				.thenReturn(Mono.just(AuthProperties.COMPLETED));
		when(keycloakClient.doMfaValidation(any(AuthenticationResponse.class), anyString()))
				.thenReturn(Mono.just(AuthProperties.SUCCESS));

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

		final String totpSessionId = RandomGenerator.generateSessionId();
		String totpCode = Faker.instance().number().digits(6); // 6 digit code
		TotpRequest totpRequest = new TotpRequest(totpCode, totpSessionId);

		Mono<AuthenticationResponse> monoUser = userAuthService.verifyTotpCode(totpRequest, serverHttpRequest);

		StepVerifier.create(monoUser)
				// .expectNextCount(1)
				.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result.getAccessToken())
						&& StringUtils.isNotBlank(result.getUsername()) && result.getUsername().equals(username)
						&& StringUtils.isNotBlank(result.getRefreshToken()))

				.verifyComplete();
	}

	/**
	 * 
	 * @throws URISyntaxException
	 */
	@DisplayName("grants access validate totp With expired login session to anonymous User")
	@Disabled
	void verifyTotpVerificationWithOnlyOTPValidationAccessIsGrantedForAnonymousUser() throws URISyntaxException {
		// String ipAddress = "166.197.174.199";
		UserBuilder user = UserBuilder.user();
		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);

		UserRepresentation userRepresentation = user.userRepresentation(userId);
		String username = userRepresentation.getUsername();

		AuthBuilder authBuilder = AuthBuilder.auth(userVO);
		AuthenticationResponse authResponse = authBuilder.authResponse();

		// Assuming maximum failed login attempt is 5
		when(redisCache.getAuthenticationResponse(anyString())).thenReturn(Mono.just(new AuthenticationResponse()));

		when(redisCache.saveAsBlockedIp(anyString())).thenReturn(Mono.just(2L));
		when(redisCache.removeCacheEntry(anyString())).thenReturn(Mono.just(Boolean.TRUE));
		when(redisCache.isBlockedIp(anyString())).thenReturn(Mono.just(Boolean.FALSE));

		when(keycloakRestService.refreshAccessToken(anyString(), anyString())).thenReturn(Mono.just(authResponse));
		when(userLocationService.processNewLocationCheck(anyString(), any(ServerHttpRequest.class)))
				.thenReturn(Mono.just(AuthProperties.COMPLETED));

		HttpHeaders headers = new HttpHeaders();
		headers.add("Forwarded", "host=84.198.58.199;proto=https");
		// headers.add("X-Forwarded-For", "103.0.513.125, 80.91.3.17, 120.192.335.629");
		headers.add("User-Agent", USER_AGENT);

		InetSocketAddress mockInetSocketAddress = Mockito.mock(InetSocketAddress.class);

		ServerHttpRequest serverHttpRequest = MockServerHttpRequest
				.method(HttpMethod.GET, new URI("http://example.com/a%20b?q=a%2Bb"))
				.contentType(MediaType.APPLICATION_JSON).headers(headers).remoteAddress(mockInetSocketAddress).build();

		final String totpSessionId = RandomGenerator.generateSessionId();
		String totpCode = Faker.instance().number().digits(6); // 6 digit code
		TotpRequest totpRequest = new TotpRequest(totpCode, totpSessionId);

		Mono<AuthenticationResponse> monoUser = userAuthService.verifyTotpCode(totpRequest, serverHttpRequest);

		StepVerifier.create(monoUser).expectError(SessionExpiredException.class).verify();
	}

	/**
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@SuppressWarnings("unchecked")
	@DisplayName("grants access send otp code to anonymous User")
	@Disabled
	void verifySendOtpCodeAccessIsGrantedForAnonymousUser() throws URISyntaxException, UnknownHostException {
		String ipAddress = "166.197.174.199";

		UserBuilder user = UserBuilder.user();
		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Forwarded", "host=84.198.58.199;proto=https");
		// headers.add("X-Forwarded-For", "103.0.513.125, 80.91.3.17, 120.192.335.629");
		headers.add("User-Agent", USER_AGENT);

		AuthBuilder authBuilder = AuthBuilder.auth(userVO);
		AuthenticationResponse authResponse = authBuilder.authResponse();

		String totpCode = Faker.instance().number().digits(6); // 6 digit code

		// Assuming maximum failed login attempt is 5
		when(redisCache.getAuthenticationResponse(anyString())).thenReturn(Mono.just(authResponse));
		when(redisCache.saveAuthenticationResponse(anyString(), any(AuthenticationResponse.class)))
				.thenReturn(Mono.just(Boolean.TRUE));
		when(totpManager.generateOtp()).thenReturn(totpCode);

		InetSocketAddress mockInetSocketAddress = Mockito.mock(InetSocketAddress.class);

		ServerHttpRequest serverHttpRequest = MockServerHttpRequest
				.method(HttpMethod.GET, new URI("http://example.com/a%20b?q=a%2Bb"))
				.contentType(MediaType.APPLICATION_JSON).headers(headers).remoteAddress(mockInetSocketAddress).build();

		String deviceDetails = GeolocationUtils.getDeviceDetails(USER_AGENT, parser);
		log.info("Device details {}", deviceDetails);

		LoginLocation loginLoc = GeolocationUtils.getUserRelativeLocation(ipAddress, deviceDetails, databaseReader);

		Mono<LoginLocation> monoLoginLoc = Mono.just(loginLoc);
		when((Mono<LoginLocation>) userLocationService.decodeUserLocation(serverHttpRequest)).thenReturn(monoLoginLoc);

		InetAddress inetAddress = InetAddress.getByName("example.com");
		InetAddress mockInetAddress = Mockito.mock(InetAddress.class);
		Mockito.when(mockInetAddress.getHostAddress()).thenReturn(ipAddress);

		Mockito.when(mockInetSocketAddress.getAddress()).thenReturn(inetAddress);
		Mockito.when(mockInetSocketAddress.getPort()).thenReturn(2222);

		final String totpSessionId = RandomGenerator.generateSessionId();
		// String totpCode = Faker.instance().number().digits(6); // 6 digit code
		SendOtpRequest otpRequest = new SendOtpRequest(totpSessionId);

		Mono<String> monoResponse = userAuthService.sendOtpCode(otpRequest, serverHttpRequest);

		StepVerifier.create(monoResponse)
				// .expectNextCount(1)
				.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result)).verifyComplete();
	}

	/**
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@SuppressWarnings("unchecked")
	@DisplayName("grants access send otp code with expired login session to anonymous User")
	@Disabled
	void verifySendOtpCodeAndExpiredLoginSessionAccessIsGrantedForAnonymousUser()
			throws URISyntaxException, UnknownHostException {
		String ipAddress = "166.197.174.199";

		UserBuilder user = UserBuilder.user();
		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Forwarded", "host=84.198.58.199;proto=https");
		// headers.add("X-Forwarded-For", "103.0.513.125, 80.91.3.17, 120.192.335.629");
		headers.add("User-Agent", USER_AGENT);

		AuthBuilder authBuilder = AuthBuilder.auth(userVO);
		AuthenticationResponse authResponse = authBuilder.authResponse();

		String totpCode = Faker.instance().number().digits(6); // 6 digit code

		// Assuming maximum failed login attempt is 5
		when(redisCache.getAuthenticationResponse(anyString())).thenReturn(Mono.just(new AuthenticationResponse()));
		when(redisCache.saveAuthenticationResponse(anyString(), any(AuthenticationResponse.class)))
				.thenReturn(Mono.just(Boolean.TRUE));
		when(totpManager.generateOtp()).thenReturn(totpCode);

		InetSocketAddress mockInetSocketAddress = Mockito.mock(InetSocketAddress.class);

		ServerHttpRequest serverHttpRequest = MockServerHttpRequest
				.method(HttpMethod.GET, new URI("http://example.com/a%20b?q=a%2Bb"))
				.contentType(MediaType.APPLICATION_JSON).headers(headers).remoteAddress(mockInetSocketAddress).build();

		String deviceDetails = GeolocationUtils.getDeviceDetails(USER_AGENT, parser);
		log.info("Device details {}", deviceDetails);

		LoginLocation loginLoc = GeolocationUtils.getUserRelativeLocation(ipAddress, deviceDetails, databaseReader);

		Mono<LoginLocation> monoLoginLoc = Mono.just(loginLoc);
		when((Mono<LoginLocation>) userLocationService.decodeUserLocation(serverHttpRequest)).thenReturn(monoLoginLoc);

		InetAddress inetAddress = InetAddress.getByName("example.com");
		InetAddress mockInetAddress = Mockito.mock(InetAddress.class);
		Mockito.when(mockInetAddress.getHostAddress()).thenReturn(ipAddress);

		Mockito.when(mockInetSocketAddress.getAddress()).thenReturn(inetAddress);
		Mockito.when(mockInetSocketAddress.getPort()).thenReturn(2222);

		final String totpSessionId = RandomGenerator.generateSessionId();
		SendOtpRequest otpRequest = new SendOtpRequest(totpSessionId);

		Mono<String> monoResponse = userAuthService.sendOtpCode(otpRequest, serverHttpRequest);

		StepVerifier.create(monoResponse).expectError(SessionExpiredException.class).verify();
	}

	/**
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@DisplayName("grants access on update(Enable) multifactor authentication to authenticated Users")
	@Disabled
	@WithMockUser(roles = { "USER", "APP_MANAGER", "MANAGER", "ADMIN" })
	void verifyUpdateToEnableMultiFactorAuthenticationAccessIsGrantedForAnonymousUser()
			throws URISyntaxException, UnknownHostException {
		String ipAddress = "166.197.174.199";

		UserBuilder user = UserBuilder.user();
		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);

		UserRepresentation userRepresentation = user.userRepresentation(userId);
		String username = userRepresentation.getUsername();

		boolean enableMfa = true;
		String totpSecret = Faker.instance().internet().password();
		String QrImage = Faker.instance().internet().image();

		when(keycloakClient.updateUserMfa(anyString(), ArgumentMatchers.eq(enableMfa)))
				.thenReturn(Mono.just(totpSecret));
		when(totpManager.generateQrImage(anyString(), anyString())).thenReturn(QrImage);

		Mono<EnableTwoFactorAuthResponse> monoResponse = userAuthService.updateTwoFactorAuth(username, enableMfa);

		StepVerifier.create(monoResponse)
				// .expectNextCount(1)
				.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result.getQrCodeImage())
						&& StringUtils.isNotBlank(result.getMessage()))

				.verifyComplete();
	}

	/**
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@DisplayName("grants access on update(Disable) multifactor authentication to authenticated Users")
	@Disabled
	@WithMockUser(roles = { "USER", "APP_MANAGER", "MANAGER", "ADMIN" })
	void verifyUpdateToDisableMultiFactorAuthenticationAccessIsGrantedForAnonymousUser()
			throws URISyntaxException, UnknownHostException {
		// String ipAddress = "166.197.174.199";

		UserBuilder user = UserBuilder.user();
		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);

		UserRepresentation userRepresentation = user.userRepresentation(userId);
		String username = userRepresentation.getUsername();

		boolean enableMfa = false;
		String totpSecret = Faker.instance().internet().password();
		String QrImage = Faker.instance().internet().image();

		when(keycloakClient.updateUserMfa(anyString(), ArgumentMatchers.eq(enableMfa))).thenReturn(Mono.empty());
		when(totpManager.generateQrImage(anyString(), anyString())).thenReturn(QrImage);

		Mono<EnableTwoFactorAuthResponse> monoResponse = userAuthService.updateTwoFactorAuth(username, enableMfa);

		StepVerifier.create(monoResponse)
				// .expectNextCount(1)
				.expectNextMatches(result -> result != null && StringUtils.isBlank(result.getQrCodeImage())
						&& StringUtils.isNotBlank(result.getMessage())
						&& result.getMessage().equals("MFA has been disabled"))

				.verifyComplete();
	}

	/**
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@DisplayName("grants access on update Profile status to authenticated Users")
	@Disabled
	@WithMockUser(roles = { "USER", "APP_MANAGER", "MANAGER", "ADMIN" })
	void verifyUpdateUserStatusAccessIsGrantedForAnonymousUser() throws URISyntaxException, UnknownHostException {

		UserBuilder user = UserBuilder.user();
		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);

		UserRepresentation userRepresentation = user.userRepresentation(userId);
		String username = userRepresentation.getUsername();

		StatusUpdateRequest statusChangeRequest = new StatusUpdateRequest(username, StatusType.EXPIRED.toString());

		when(keycloakClient.updateUserStatus(any(StatusUpdateRequest.class))).thenReturn(Mono.just("SUCCESS"));

		Mono<String> monoResponse = userAuthService.updateUserStatus(statusChangeRequest);

		StepVerifier.create(monoResponse)
				// .expectNextCount(1)
				.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result)
						&& result.equals("Profile Status update was successful"))

				.verifyComplete();
	}

	/**
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@DisplayName("grants access on update Profile details to authenticated Users")
	@Disabled
	@WithMockUser(roles = { "USER", "APP_MANAGER", "MANAGER", "ADMIN" })
	void verifyUpdateUserDetailsAccessIsGrantedForAnonymousUser() throws URISyntaxException, UnknownHostException {

		UserBuilder user = UserBuilder.user();
		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);

		UserRepresentation userRepresentation = user.userRepresentation(userId);
		String username = userRepresentation.getUsername();

		UserDetailsUpdateRequest statusChangeRequest = new UserDetailsUpdateRequest(userVO.getFirstName(),
				userVO.getLastName());

		when(keycloakClient.updateOauthUser(any(UserDetailsUpdateRequest.class), anyString())).thenReturn(Mono.just(userVO));

		Mono<UserVO> monoResponse = userAuthService.updateUserDetails(username, statusChangeRequest);

		StepVerifier.create(monoResponse)
				// .expectNextCount(1)
				.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result.getId())
						&& StringUtils.isNotBlank(result.getUsername()) && result.getId().equals(userId.toString())
						&& result.getFirstName().equals(statusChangeRequest.getFirstName())
						&& result.getLastName().equals(statusChangeRequest.getLastName()))
				.verifyComplete();
	}
	
	/**
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@DisplayName("grants access on logout to authenticated Users")
	@Disabled
	@WithMockUser(roles = { "USER", "APP_MANAGER", "MANAGER", "ADMIN" })
	void verifyLogoutAccessIsGrantedForAnonymousUser() throws URISyntaxException, UnknownHostException {

		UserBuilder user = UserBuilder.user();
		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);
		
		AuthBuilder authBuilder = AuthBuilder.auth(userVO);
		AuthenticationResponse authResponse = authBuilder.authResponse();

		UserRepresentation userRepresentation = user.userRepresentation(userId);
		String username = userRepresentation.getUsername();
		String refreshToken = authResponse.getRefreshToken();

		when(keycloakClient.logout(anyString(), anyString())).thenReturn(Mono.empty());

		Mono<String> monoResponse = userAuthService.logout(username, refreshToken); 

		StepVerifier.create(monoResponse)
				// .expectNextCount(1)
				.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result)
						&& result.equals("Logout succeeded"))
				.verifyComplete();
	}
	
	/**
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@DisplayName("grants access on refresh token to authenticated Users")
	@Disabled
	@WithMockUser(roles = { "USER", "APP_MANAGER", "MANAGER", "ADMIN" })
	void verifyRefreshTokenAccessIsGrantedForAnonymousUser() throws URISyntaxException, UnknownHostException {

		UserBuilder user = UserBuilder.user();
		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);
		
		AuthBuilder authBuilder = AuthBuilder.auth(userVO);
		AuthenticationResponse authResponse = authBuilder.authResponse();

		UserRepresentation userRepresentation = user.userRepresentation(userId);
		String username = userRepresentation.getUsername();
		String refreshToken = authResponse.getRefreshToken();

		when(keycloakRestService.refreshAccessToken(anyString(), anyString())).thenReturn(Mono.just(authResponse)); 

		Mono<AuthenticationResponse> monoResponse = userAuthService.refreshToken(username, refreshToken); 

		StepVerifier.create(monoResponse)
		// .expectNextCount(1)
		.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result.getAccessToken())
				&& StringUtils.isNotBlank(result.getUsername()) && result.getUsername().equals(username)
				&& StringUtils.isNotBlank(result.getRefreshToken()))

		.verifyComplete();
	}
	
	/**
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@DisplayName("grants access on update email status to ADMIN")
	@Test
	@WithMockUser(roles = { "USER", "APP_MANAGER", "MANAGER", "ADMIN" })
	void verifyUpdateEmailStatusAccessIsGrantedForAnonymousUser() throws URISyntaxException, UnknownHostException {

		UserBuilder user = UserBuilder.user();
		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);
		
		AuthBuilder authBuilder = AuthBuilder.auth(userVO);
		AuthenticationResponse authResponse = authBuilder.authResponse();

		UserRepresentation userRepresentation = user.userRepresentation(userId);
		String username = userRepresentation.getUsername();
		String refreshToken = authResponse.getRefreshToken();

		when(keycloakClient.updateEmailStatus(anyString(), anyBoolean())).thenReturn(Mono.just("SUCCESS")); 

		boolean verifiedEmail = true;
		EmailStatusUpdateRequest statusChangeRequest = new EmailStatusUpdateRequest(userVO.getEmail(), verifiedEmail);		
		Mono<String> monoResponse = userAuthService.updateEmailStatus(statusChangeRequest);   

		StepVerifier.create(monoResponse)
		// .expectNextCount(1)
		.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result)
				&& result.equals("SUCCESS"))
		.verifyComplete();
	}
	
	/**
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@DisplayName("grants access on enable user profile to ADMIN")
	@Test
	@WithMockUser(roles = { "USER", "APP_MANAGER", "MANAGER", "ADMIN" })
	void verifyEnableUserProfileAccessIsGrantedForAnonymousUser() throws URISyntaxException, UnknownHostException {

		UserBuilder user = UserBuilder.user();
		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);
		
		AuthBuilder authBuilder = AuthBuilder.auth(userVO);
		AuthenticationResponse authResponse = authBuilder.authResponse();

		UserRepresentation userRepresentation = user.userRepresentation(userId);
		String username = userRepresentation.getUsername();
		String refreshToken = authResponse.getRefreshToken();

		when(keycloakClient.enableOauthUser(any(ProfileStatusUpdateRequest.class))).thenReturn(Mono.just("SUCCESS")); 

		boolean enableProfile = true;
		ProfileStatusUpdateRequest statusChangeRequest = new ProfileStatusUpdateRequest(userVO.getEmail(), enableProfile);		
		Mono<String> monoResponse = userAuthService.enableUserProfile(statusChangeRequest);   

		StepVerifier.create(monoResponse)
		// .expectNextCount(1)
		.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result)
				&& result.equals("SUCCESS"))
		.verifyComplete();
	}
	
	/**
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@DisplayName("grants access on disable user profile to ADMIN")
	@Test
	@WithMockUser(roles = { "USER", "APP_MANAGER", "MANAGER", "ADMIN" })
	void verifyDisableUserProfileAccessIsGrantedForAnonymousUser() throws URISyntaxException, UnknownHostException {

		UserBuilder user = UserBuilder.user();
		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);
		
		AuthBuilder authBuilder = AuthBuilder.auth(userVO);
		AuthenticationResponse authResponse = authBuilder.authResponse();

		UserRepresentation userRepresentation = user.userRepresentation(userId);
		String username = userRepresentation.getUsername();
		String refreshToken = authResponse.getRefreshToken();

		when(keycloakClient.enableOauthUser(any(ProfileStatusUpdateRequest.class))).thenReturn(Mono.just("SUCCESS")); 

		boolean enableProfile = true;
		ProfileStatusUpdateRequest statusChangeRequest = new ProfileStatusUpdateRequest(userVO.getEmail(), enableProfile);		
		Mono<String> monoResponse = userAuthService.enableUserProfile(statusChangeRequest);   

		StepVerifier.create(monoResponse)
		// .expectNextCount(1)
		.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result)
				&& result.equals("SUCCESS"))
		.verifyComplete();
	}
}