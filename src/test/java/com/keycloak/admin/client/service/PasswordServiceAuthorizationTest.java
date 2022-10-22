/**
 * 
 */
package com.keycloak.admin.client.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
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
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.javafaker.Faker;
import com.keycloak.admin.client.common.enums.StatusType;
import com.keycloak.admin.client.common.events.GenericSpringEvent;
import com.keycloak.admin.client.common.utils.LocaleContextUtils;
import com.keycloak.admin.client.common.utils.RandomGenerator;
import com.keycloak.admin.client.components.AuthenticatedUserMgr;
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;
import com.keycloak.admin.client.config.AppConfiguration;
import com.keycloak.admin.client.config.AuthProfile;
import com.keycloak.admin.client.config.AuthProperties;
import com.keycloak.admin.client.config.LoginNotificationConfig;
import com.keycloak.admin.client.config.MessageConfig;
import com.keycloak.admin.client.config.SecurityConfig;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.entities.ActivationToken;
import com.keycloak.admin.client.entities.PasswordResetToken;
import com.keycloak.admin.client.entities.UserloginLocation;
import com.keycloak.admin.client.events.CustomUserAuthActionEvent;
import com.keycloak.admin.client.exceptions.AuthenticationError;
import com.keycloak.admin.client.exceptions.ResetPasswordTokenValidationException;
import com.keycloak.admin.client.exceptions.ResourceNotFoundException;
import com.keycloak.admin.client.exceptions.ServiceUnavailableException;
import com.keycloak.admin.client.exceptions.SessionExpiredException;
import com.keycloak.admin.client.models.ForgotUsernameRequest;
import com.keycloak.admin.client.models.PasswordResetTokenResponse;
import com.keycloak.admin.client.models.PasswordUpdateRequest;
import com.keycloak.admin.client.models.ResetPasswordFinalRequest;
import com.keycloak.admin.client.models.ResetPasswordRequest;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.oauth.service.GatewayRedisCache;
import com.keycloak.admin.client.oauth.service.KeycloakOauthClient;
import com.keycloak.admin.client.oauth.service.PasswordServiceImpl;
import com.keycloak.admin.client.oauth.service.it.PasswordMgtService;
import com.keycloak.admin.client.oauth.service.it.UserLocationService;
import com.keycloak.admin.client.repository.PasswordResetTokenRepository;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import wiremock.org.eclipse.jetty.util.StringUtil;

/**
 * @author Gbenga
 *
 */
@Log4j2
@WebFluxTest
@RecordApplicationEvents
@DisplayName("Verify Password service")
@ContextConfiguration(classes = { AppConfiguration.class, SecurityConfig.class, MessageConfig.class,
		AuthProperties.class, AuthProfile.class, LoginNotificationConfig.class })
@Import({ PasswordServiceImpl.class, LocaleContextUtils.class, CustomMessageSourceAccessor.class,
		AuthenticatedUserMgr.class })
//@ExtendWith({ SpringExtension.class })
class PasswordServiceAuthorizationTest {

	@MockBean
	private UserLocationService userLocationService;
	@MockBean
	private PasswordResetTokenRepository passwordTokenRepository;
	@MockBean
	private KeycloakOauthClient keycloakClient;
	@MockBean
	private GatewayRedisCache redisCache;
	@MockBean
	private ReactiveMongoTemplate mongoTemplate;

	private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel MacOS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.114 Safari/537.36";

	@Autowired
	private ApplicationEvents applicationEvents;
	@Autowired
	private PasswordMgtService passwdService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

	}

	@BeforeAll
	static void initializeData() throws FileNotFoundException, IOException, URISyntaxException {

		// BlockHound.install(new ReactorBlockHoundIntegration(), new MyIntegration());
		// =============================================================================
	}

	/**
	 * Re-test
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@DisplayName("grants access to recover username for anonymous user")
	@Disabled
	void verifyRecoverUsernameAccessIsGrantedForUnauthenticated() throws URISyntaxException, UnknownHostException {
		UUID userId = UUID.randomUUID();
		UserRepresentation userRepresentation = UserBuilder.user().userRepresentation(userId);
		String username = userRepresentation.getEmail();

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

		when(keycloakClient.findUserByEmail(any(String.class))).thenReturn(Mono.just(userRepresentation));

		ForgotUsernameRequest usernameRequest = new ForgotUsernameRequest(username);
		Mono<String> monoUser = passwdService.recoverUsername(usernameRequest, serverHttpRequest);

		StepVerifier.create(monoUser).expectNextMatches(result -> result != null && StringUtils.isNotBlank(result))
				.verifyComplete();

		Assertions.assertEquals(1, applicationEvents.stream(CustomUserAuthActionEvent.class)
				.filter(event -> event.getUser().getEmail().equals(userRepresentation.getEmail())).count());

		applicationEvents.stream().forEach(System.out::println);

		Predicate<? super ApplicationEvent> predicate = p -> p instanceof CustomUserAuthActionEvent;

		Assertions.assertTrue(applicationEvents.stream().anyMatch(predicate));
	}

	/**
	 * Re-test
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@DisplayName("grants access to change password for authenticated user")
	@Disabled
	@WithMockUser(roles = { "USER", "APP_MANAGER", "MANAGER", "ADMIN" })
	void verifyChangePasswordAccessIsGrantedForUnauthenticated() throws URISyntaxException, UnknownHostException {
		UUID userId = UUID.randomUUID();
		UserRepresentation userRepresentation = UserBuilder.user().userRepresentation(userId);
		String username = userRepresentation.getEmail();

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

		UserloginLocation usrLoginLoc = UserloginLocation.builder().id(UUID.randomUUID().toString()).ipAddr(ipAddress)
				.loginLocHis(Collections.emptyList()).username(username).build();

		when(keycloakClient.findUserByUsername(any(String.class))).thenReturn(Mono.just(userRepresentation));
		when(keycloakClient.saveNewPassword(any(PasswordUpdateRequest.class), any(UserRepresentation.class)))
				.thenReturn(Mono.just("Success"));
		when(userLocationService.recordNewUserLocation(anyString(), any(ServerHttpRequest.class)))
				.thenReturn(Mono.just(usrLoginLoc));

		String oldPassword = Faker.instance().internet().password();
		String newPassword = Faker.instance().internet().password();
		PasswordUpdateRequest changePasswordRequest = new PasswordUpdateRequest(oldPassword, newPassword);

		Mono<String> monoUser = passwdService.changePassword(changePasswordRequest, username, serverHttpRequest);
		StepVerifier.create(monoUser).expectNextMatches(result -> result != null && StringUtils.isNotBlank(result))
				.verifyComplete();

		Assertions.assertEquals(1, applicationEvents.stream(CustomUserAuthActionEvent.class)
				.filter(event -> event.getUser().getEmail().equals(userRepresentation.getEmail())).count());
		Assertions.assertEquals(1, applicationEvents.stream(GenericSpringEvent.class)
				.filter(event -> StringUtil.isNotBlank(event.getMessage())).count());

		applicationEvents.stream().forEach(System.out::println);

		Predicate<? super ApplicationEvent> predicate = p -> p instanceof CustomUserAuthActionEvent
				|| p instanceof GenericSpringEvent;

		Assertions.assertTrue(applicationEvents.stream().anyMatch(predicate));
	}

	/**
	 * Re-test
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@DisplayName("grants access to reset password for anonymous user")
	@Disabled
	void verifyInitiateResetPasswordAccessIsGrantedForUnauthenticated()
			throws URISyntaxException, UnknownHostException {
		UUID userId = UUID.randomUUID();
		UserRepresentation userRepresentation = UserBuilder.user().userRepresentation(userId);
		String email = userRepresentation.getEmail();

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

		final String newToken = RandomGenerator.generate6RandomDigits();
		PasswordResetToken passwordToken = PasswordResetToken.builder().username(email).token(newToken)
				.status(StatusType.VALID.toString()).creationDate(LocalDateTime.now()).build();

		when(keycloakClient.findUserByEmail(any(String.class))).thenReturn(Mono.just(userRepresentation));
		when(passwordTokenRepository.save(any(PasswordResetToken.class))).thenReturn(Mono.just(passwordToken));

		when(userLocationService.processNewLocationCheck(anyString(), any(ServerHttpRequest.class)))
				.thenReturn(Mono.just("SUCCESS"));

		ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest(email);

		Mono<String> monoUser = passwdService.initiateResetPasswd(resetPasswordRequest, serverHttpRequest);
		StepVerifier.create(monoUser).expectNextMatches(result -> result != null && StringUtils.isNotBlank(result))
				.verifyComplete();

		Assertions.assertEquals(1, applicationEvents.stream(CustomUserAuthActionEvent.class)
				.filter(event -> event.getUser().getEmail().equals(userRepresentation.getEmail())).count());
		Assertions.assertEquals(1, applicationEvents.stream(GenericSpringEvent.class)
				.filter(event -> StringUtil.isNotBlank(event.getMessage())).count());

		applicationEvents.stream().forEach(System.out::println);

		Predicate<? super ApplicationEvent> predicate = p -> p instanceof CustomUserAuthActionEvent
				|| p instanceof GenericSpringEvent;

		Assertions.assertTrue(applicationEvents.stream().anyMatch(predicate));
	}

	/**
	 * Re-test
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@DisplayName("grants access to validate password reset for anonymous user")
	@Disabled
	void verifyValidatePasswordResetTokenAccessIsGrantedForUnauthenticated() {
		UUID userId = UUID.randomUUID();
		UserRepresentation userRepresentation = UserBuilder.user().userRepresentation(userId);
		String email = userRepresentation.getEmail();

		// String ipAddress = "166.197.174.199";

		final String token = RandomGenerator.generate6RandomDigits();
		PasswordResetToken passwordToken = PasswordResetToken.builder().username(email).token(token)
				.status(StatusType.VALID.toString()).creationDate(LocalDateTime.now()).build();

		when(passwordTokenRepository.findById(anyString())).thenReturn(Mono.just(passwordToken));

		when(passwordTokenRepository.deleteById(anyString())).thenReturn(Mono.empty());
		when(redisCache.savePasswordResetSession(anyString(), anyString())).thenReturn(Mono.just(Boolean.TRUE));

		Mono<PasswordResetTokenResponse> monoUser = passwdService.validatePasswordResetToken(token);
		StepVerifier.create(monoUser)
				.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result.getSessionId()))
				.verifyComplete();
	}

	/**
	 * Re-test
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@DisplayName("grants access to validate password reset with invalid token for anonymous user")
	@Disabled
	void verifyValidatePasswordResetWithInvalidTokenAccessIsGrantedForUnauthenticated() {
		UUID userId = UUID.randomUUID();
		UserRepresentation userRepresentation = UserBuilder.user().userRepresentation(userId);
		String email = userRepresentation.getEmail();

		// String ipAddress = "166.197.174.199";

		final String token = RandomGenerator.generate6RandomDigits();
		PasswordResetToken passwordToken = PasswordResetToken.builder().username(email).token(token)
				.status(StatusType.VALID.toString()).creationDate(LocalDateTime.now()).build();

		when(passwordTokenRepository.findById(anyString())).thenReturn(Mono.empty());

		when(passwordTokenRepository.deleteById(anyString())).thenReturn(Mono.empty());
		when(redisCache.savePasswordResetSession(anyString(), anyString())).thenReturn(Mono.just(Boolean.TRUE));

		Mono<PasswordResetTokenResponse> monoUser = passwdService.validatePasswordResetToken(token);

		StepVerifier.create(monoUser).verifyError(ResetPasswordTokenValidationException.class);
	}

	/**
	 * Re-test
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@DisplayName("grants access to validate password reset with expired token for anonymous user")
	@Disabled
	void verifyValidatePasswordResetWithExpiredTokenAccessIsGrantedForUnauthenticated() {
		UUID userId = UUID.randomUUID();
		UserRepresentation userRepresentation = UserBuilder.user().userRepresentation(userId);
		String email = userRepresentation.getEmail();

		// String ipAddress = "166.197.174.199";

		final String token = RandomGenerator.generate6RandomDigits();
		PasswordResetToken passwordToken = PasswordResetToken.builder().username(email).token(token)
				.status(StatusType.EXPIRED.toString()).creationDate(LocalDateTime.now()).build();

		when(passwordTokenRepository.findById(anyString())).thenReturn(Mono.just(passwordToken));
		when(passwordTokenRepository.deleteById(anyString())).thenReturn(Mono.empty());
		when(redisCache.savePasswordResetSession(anyString(), anyString())).thenReturn(Mono.just(Boolean.TRUE));

		Mono<PasswordResetTokenResponse> monoUser = passwdService.validatePasswordResetToken(token);

		StepVerifier.create(monoUser).verifyError(ResetPasswordTokenValidationException.class);
	}

	/**
	 * Re-test
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@DisplayName("grants access to validate password reset, session token exist for anonymous user")
	@Disabled
	void verifyValidatePasswordResetSessionExistAccessIsGrantedForUnauthenticated() {
		UUID userId = UUID.randomUUID();
		UserRepresentation userRepresentation = UserBuilder.user().userRepresentation(userId);
		String email = userRepresentation.getEmail();

		// String ipAddress = "166.197.174.199";

		final String token = RandomGenerator.generate6RandomDigits();
		PasswordResetToken passwordToken = PasswordResetToken.builder().username(email).token(token)
				.status(StatusType.VALID.toString()).creationDate(LocalDateTime.now()).build();

		when(passwordTokenRepository.findById(anyString())).thenReturn(Mono.just(passwordToken));
		when(passwordTokenRepository.deleteById(anyString())).thenReturn(Mono.empty());
		when(redisCache.savePasswordResetSession(anyString(), anyString())).thenReturn(Mono.just(Boolean.FALSE));

		Mono<PasswordResetTokenResponse> monoUser = passwdService.validatePasswordResetToken(token);

		StepVerifier.create(monoUser).verifyError(ServiceUnavailableException.class);
	}

	/**
	 * Re-test
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@DisplayName("grants access to reset password for anonymous user")
	@Disabled
	void verifyResetUserPasswordAccessIsGrantedForUnauthenticated() throws URISyntaxException, UnknownHostException {
		UUID userId = UUID.randomUUID();
		UserRepresentation userRepresentation = UserBuilder.user().userRepresentation(userId);
		// String email = userRepresentation.getEmail();
		String username = userRepresentation.getUsername();

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

		String sessionId = RandomGenerator.generateSessionId();
		String newPassword = Faker.instance().internet().password();

		ResetPasswordFinalRequest newPasswordResetReq = new ResetPasswordFinalRequest(username, newPassword, sessionId);

		when(redisCache.getPasswordResetSession(eq(sessionId))).thenReturn(Mono.just(username));
		when(keycloakClient.resetPassword(eq(username), eq(newPassword))).thenReturn(Mono.just("SUCCESS"));
		when(redisCache.deletePasswordResetSession(anyString())).thenReturn(Mono.just(Boolean.TRUE));

		Mono<String> monoUser = passwdService.resetUserPassword(newPasswordResetReq, serverHttpRequest);

		StepVerifier.create(monoUser).expectNextMatches(result -> result != null && StringUtils.isNotBlank(result))
				.verifyComplete();

		Assertions.assertEquals(1, applicationEvents.stream(CustomUserAuthActionEvent.class)
				.filter(event -> event.getUser().getEmail().equals(userRepresentation.getEmail())).count());
		Assertions.assertEquals(1, applicationEvents.stream(GenericSpringEvent.class)
				.filter(event -> StringUtil.isNotBlank(event.getMessage())).count());

		applicationEvents.stream().forEach(System.out::println);

		Predicate<? super ApplicationEvent> predicate = p -> p instanceof CustomUserAuthActionEvent
				|| p instanceof GenericSpringEvent;

		Assertions.assertTrue(applicationEvents.stream().anyMatch(predicate));
	}

	/**
	 * Re-test
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@DisplayName("grants access to reset password with expired session for anonymous user")
	@Disabled
	void verifyResetUserPasswordExpiredSessionAccessIsGrantedForUnauthenticated()
			throws URISyntaxException, UnknownHostException {
		UUID userId = UUID.randomUUID();
		UserRepresentation userRepresentation = UserBuilder.user().userRepresentation(userId);
		// String email = userRepresentation.getEmail();
		String username = userRepresentation.getUsername();

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

		String sessionId = RandomGenerator.generateSessionId();
		String newPassword = Faker.instance().internet().password();

		ResetPasswordFinalRequest newPasswordResetReq = new ResetPasswordFinalRequest(username, newPassword, sessionId);

		when(redisCache.getPasswordResetSession(eq(sessionId))).thenReturn(
				Mono.error(new SessionExpiredException("reset.password.session.notfound", new Object[] {})));

		when(keycloakClient.resetPassword(eq(username), eq(newPassword))).thenReturn(Mono.just("SUCCESS"));
		when(redisCache.deletePasswordResetSession(anyString())).thenReturn(Mono.just(Boolean.TRUE));

		Mono<String> monoUser = passwdService.resetUserPassword(newPasswordResetReq, serverHttpRequest);

		StepVerifier.create(monoUser).verifyError(SessionExpiredException.class);
	}

	/**
	 * Re-test
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@DisplayName("grants access to reset password but user not found for anonymous user")
	@Disabled
	void verifyResetUserPasswordUserNotFoundAccessIsGrantedForUnauthenticated()
			throws URISyntaxException, UnknownHostException {
		UUID userId = UUID.randomUUID();
		UserRepresentation userRepresentation = UserBuilder.user().userRepresentation(userId);
		// String email = userRepresentation.getEmail();
		String username = userRepresentation.getUsername();

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

		String sessionId = RandomGenerator.generateSessionId();
		String newPassword = Faker.instance().internet().password();

		ResetPasswordFinalRequest newPasswordResetReq = new ResetPasswordFinalRequest(username, newPassword, sessionId);

		when(redisCache.getPasswordResetSession(eq(sessionId))).thenReturn(Mono.just(username));

		when(keycloakClient.resetPassword(eq(username), eq(newPassword)))
				.thenReturn(Mono.error(new ResourceNotFoundException("user.notFound", new Object[] { username })));
		when(redisCache.deletePasswordResetSession(anyString())).thenReturn(Mono.just(Boolean.TRUE));

		Mono<String> monoUser = passwdService.resetUserPassword(newPasswordResetReq, serverHttpRequest);

		StepVerifier.create(monoUser).verifyError(ResourceNotFoundException.class);
	}

	/**
	 * Re-test
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@DisplayName("grants access to reset password but user not matched for anonymous user")
	@Disabled
	void verifyResetUserPasswordUserUnmatchedAccessIsGrantedForUnauthenticated()
			throws URISyntaxException, UnknownHostException {
		UUID userId = UUID.randomUUID();
		UserRepresentation userRepresentation = UserBuilder.user().userRepresentation(userId);
		// String email = userRepresentation.getEmail();
		String username = userRepresentation.getUsername();

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

		String sessionId = RandomGenerator.generateSessionId();
		String newPassword = Faker.instance().internet().password();

		ResetPasswordFinalRequest newPasswordResetReq = new ResetPasswordFinalRequest(username, newPassword, sessionId);

		String anotherUsername = "Unknown_user";
		when(redisCache.getPasswordResetSession(eq(sessionId))).thenReturn(Mono.just(anotherUsername));
		when(keycloakClient.resetPassword(eq(username), eq(newPassword))).thenReturn(Mono.just("SUCCESS"));
		when(redisCache.deletePasswordResetSession(anyString())).thenReturn(Mono.just(Boolean.TRUE));

		Mono<String> monoUser = passwdService.resetUserPassword(newPasswordResetReq, serverHttpRequest);

		StepVerifier.create(monoUser).verifyError(AuthenticationError.class);
	}

	/**
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 * 
	 */
	@DisplayName("test scheduler to expire user passwords")
	@Disabled
	void verifyExpireUserPasswordsAccessIsGrantedForAnonymous() throws URISyntaxException, UnknownHostException {

		UserBuilder user = UserBuilder.user();

		int listSize = 20;
		List<UserRepresentation> userRepresentationList = user.userRepresentationList(listSize);

		when(keycloakClient.findAllUsers()).thenReturn(Flux.fromIterable(userRepresentationList));
		// when(keycloakClient.conditionsForPasswordExpiration(any(UserRepresentation.class))).thenReturn(true);
		when(keycloakClient.expireUserPassword(any(UserRepresentation.class))).thenReturn(Mono.just("SUCCESS"));

		Flux<Flux<String>> fluxOfFluxResponse = passwdService.expireUserProfilePasswordRecords();

		Flux<String> fluxResp = fluxOfFluxResponse.flatMap(f -> f);
		StepVerifier.create(fluxResp).expectNextCount(listSize)
				/*
				 * .expectNextMatches(result -> { int count = 0; Iterator<UserRepresentation>
				 * iter = result.toIterable().iterator();
				 * 
				 * while (iter.hasNext()) { UserRepresentation userRep = iter.next();
				 * assertThat(StringUtils.isNotBlank(userRep.getUsername()));
				 * 
				 * ++count; }
				 * 
				 * return true; })
				 */
				.verifyComplete();
	}

	/**
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 * 
	 */
	@DisplayName("test scheduler to expire password reset tokens")
	@Test
	void verifyExpirePasswordResetTokenRecordsAccessIsGrantedForAnonymous()
			throws URISyntaxException, UnknownHostException {

		int matchedCount = 2;
		Long matchedUpdate = 2L;
		UpdateResult updateResult = UpdateResult.acknowledged(matchedCount, matchedUpdate, null);
		when(mongoTemplate.updateMulti(any(Query.class), any(UpdateDefinition.class), eq(PasswordResetToken.class)))
				.thenReturn(Mono.just(updateResult));

		Mono<Long> monoResponse = passwdService.expirePasswordResetTokenRecords();

		StepVerifier.create(monoResponse).expectNextMatches(result -> result == matchedUpdate).verifyComplete();
	}

	/**
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 * 
	 */
	@DisplayName("test scheduler to remove expired password reset tokens")
	@Test
	void verifyRemoveExpiredPasswordResetTokenRecordsAccessIsGrantedForAnonymous()
			throws URISyntaxException, UnknownHostException {

		int deletedCount = 2;
		DeleteResult deleteResult = DeleteResult.acknowledged(deletedCount);
		when(mongoTemplate.remove(any(Query.class), eq(PasswordResetToken.class))).thenReturn(Mono.just(deleteResult));

		Mono<Long> monoResponse = passwdService.removeExpiredPasswordResetTokenRecords();

		StepVerifier.create(monoResponse).expectNextMatches(result -> result == deletedCount).verifyComplete();
	}

}
