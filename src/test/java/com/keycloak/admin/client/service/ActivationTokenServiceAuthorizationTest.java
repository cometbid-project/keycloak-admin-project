/**
 * 
 */
package com.keycloak.admin.client.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.keycloak.admin.client.common.enums.StatusType;
import com.keycloak.admin.client.common.utils.LocaleContextUtils;
import com.keycloak.admin.client.common.utils.RandomGenerator;
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;
import com.keycloak.admin.client.config.AppConfiguration;
import com.keycloak.admin.client.config.AuthProfile;
import com.keycloak.admin.client.config.AuthProperties;
import com.keycloak.admin.client.config.MessageConfig;
import com.keycloak.admin.client.config.SecurityConfig;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.entities.ActivationToken;
import com.keycloak.admin.client.events.UserAuthEventTypes;
import com.keycloak.admin.client.exceptions.ActivationTokenValidationException;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.models.mappers.UserActivationMapper;
import com.keycloak.admin.client.oauth.service.ActivationTokenServiceImpl;
import com.keycloak.admin.client.oauth.service.CommonUtil;
import com.keycloak.admin.client.oauth.service.KeycloakOauthClient;
import com.keycloak.admin.client.oauth.service.it.ActivationTokenService;
import com.keycloak.admin.client.oauth.service.it.KeycloakOauthClientService;
import com.keycloak.admin.client.repository.UserActivationTokenRepository;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author Gbenga
 *
 */
@Log4j2
@WebFluxTest
@DisplayName("Verify Activation token service")
@ContextConfiguration(classes = { AuthProfile.class, AppConfiguration.class, SecurityConfig.class, MessageConfig.class,
		AuthProperties.class })
@Import({ UserActivationMapper.class, LocaleContextUtils.class, KeycloakOauthClient.class,
		ActivationTokenServiceImpl.class, CustomMessageSourceAccessor.class, CommonUtil.class })
//@ExtendWith({ SpringExtension.class })
class ActivationTokenServiceAuthorizationTest {

	@MockBean
	private UserActivationTokenRepository tokenRepository;
	@MockBean
	private CommonUtil commonUtil;
	@MockBean
	private ReactiveMongoTemplate mongoTemplate;

	@Autowired
	private ActivationTokenService tokenService;

	@MockBean(name = "keycloak-client")
	private KeycloakOauthClientService keycloakClient;

	private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel MacOS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.114 Safari/537.36";

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

	}

	@BeforeAll
	static void initializeData() throws FileNotFoundException, IOException, URISyntaxException {

		// BlockHound.install(new ReactorBlockHoundIntegration(), new MyIntegration());
		// =============================================================================
	}

	@TestConfiguration
	static class MockitoPublisherConfiguration {

		@Bean
		@Primary
		ApplicationEventPublisher publisher() {
			return mock(ApplicationEventPublisher.class);
		}

		@Bean
		@Primary
		GenericApplicationContext genericApplicationContext(final GenericApplicationContext gac) {
			return Mockito.spy(gac);
		}
	}

	/**
	  * 
	  */
	@DisplayName("grants access to generate email verification token for Anonymous users")
	@Test
	void verifyGenerateEmailVerificationTokenWithExistingRecordAccessIsGrantedForAnonymous() {

		UserBuilder user = UserBuilder.user();

		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);

		UserRepresentation userRepresentation = user.userRepresentation(userId);
		String username = userRepresentation.getUsername();
		String token = RandomGenerator.generateNewToken();

		String statusType = StatusType.VALID.name();
		ActivationToken activationToken = ActivationToken.builder().username(username).token(token)
				.creationDate(LocalDateTime.now()).status(statusType).build();

		when(commonUtil.getNewActivationToken(anyString())).thenReturn(Mono.just(activationToken));
		when(tokenRepository.findByUsernameAndStatusAllIgnoreCase(anyString(), eq(statusType)))
				.thenReturn(Mono.just(activationToken));

		Mono<ActivationToken> monoResponse = tokenService.generateEmailVerificationToken(username);

		StepVerifier.create(monoResponse)
				.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result.getStatus())
						&& StringUtils.isNotBlank(result.getUsername()) && result.getToken().equals(token)
						&& result.getUsername().equals(username))
				.verifyComplete();
	}

	/**
	  * 
	  */
	@DisplayName("grants access to generate email verification token with no existing record for Anonymous users")
	@Test
	void verifyGenerateEmailVerificationTokenNoExistingRecordAccessIsGrantedForAnonymous() {

		UserBuilder user = UserBuilder.user();

		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);

		UserRepresentation userRepresentation = user.userRepresentation(userId);
		String username = userRepresentation.getUsername();
		String token = RandomGenerator.generateNewToken();

		String statusType = StatusType.VALID.name();
		ActivationToken activationToken = ActivationToken.builder().username(username).token(token)
				.creationDate(LocalDateTime.now()).status(statusType).build();

		when(commonUtil.getNewActivationToken(anyString())).thenReturn(Mono.just(activationToken));
		when(tokenRepository.findByUsernameAndStatusAllIgnoreCase(anyString(), eq(statusType)))
				.thenReturn(Mono.empty());

		Mono<ActivationToken> monoResponse = tokenService.generateEmailVerificationToken(username);

		StepVerifier.create(monoResponse)
				.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result.getStatus())
						&& StringUtils.isNotBlank(result.getUsername()) && result.getToken().equals(token)
						&& result.getUsername().equals(username))
				.verifyComplete();
	}

	/**
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 * 
	 */
	@DisplayName("grants access to renew email verification token with existing record for Anonymous users")
	@Test
	void verifyRenewActivationTokenWithExistingRecordAccessIsGrantedForAnonymous()
			throws URISyntaxException, UnknownHostException {
		String ipAddress = "166.197.174.199";
		UserBuilder user = UserBuilder.user();

		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);

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

		UserRepresentation userRepresentation = user.userRepresentation(userId);
		String username = userRepresentation.getUsername();
		String token = RandomGenerator.generateNewToken();

		String statusType = StatusType.VALID.name();
		ActivationToken activationToken = ActivationToken.builder().username(username).token(token)
				.creationDate(LocalDateTime.now()).status(statusType).build();

		when(keycloakClient.findUserByUsername(any(String.class))).thenReturn(Mono.just(userRepresentation));
		when(tokenRepository.findById(eq(token))).thenReturn(Mono.just(activationToken));
		when(commonUtil.getNewActivationToken(anyString())).thenReturn(Mono.just(activationToken));
		when(commonUtil.sendEmailVerificationEvent(any(UserVO.class), anyString(), any(UserAuthEventTypes.class),
				eq(serverHttpRequest))).thenReturn(Mono.empty());
		when(tokenRepository.findByUsernameAndStatusAllIgnoreCase(anyString(), eq(statusType)))
				.thenReturn(Mono.empty());

		Mono<String> monoResponse = tokenService.renewActivationToken(token, serverHttpRequest);

		StepVerifier.create(monoResponse).expectNextMatches(result -> result != null && StringUtils.isNotBlank(result))
				.verifyComplete();
	}

	/**
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 * 
	 */
	@DisplayName("grants access to renew email verification token with no existing record for Anonymous users")
	@Test
	void verifyRenewActivationTokenWithNoExistingRecordAccessIsGrantedForAnonymous()
			throws URISyntaxException, UnknownHostException {
		String ipAddress = "166.197.174.199";
		UserBuilder user = UserBuilder.user();

		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);

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

		UserRepresentation userRepresentation = user.userRepresentation(userId);
		String username = userRepresentation.getUsername();
		String token = RandomGenerator.generateNewToken();

		String statusType = StatusType.VALID.name();
		ActivationToken activationToken = ActivationToken.builder().username(username).token(token)
				.creationDate(LocalDateTime.now()).status(statusType).build();

		when(keycloakClient.findUserByUsername(any(String.class))).thenReturn(Mono.just(userRepresentation));
		when(tokenRepository.findById(eq(token))).thenReturn(Mono.empty());
		when(commonUtil.getNewActivationToken(anyString())).thenReturn(Mono.just(activationToken));
		when(commonUtil.sendEmailVerificationEvent(any(UserVO.class), anyString(), any(UserAuthEventTypes.class),
				eq(serverHttpRequest))).thenReturn(Mono.empty());
		when(tokenRepository.findByUsernameAndStatusAllIgnoreCase(anyString(), eq(statusType)))
				.thenReturn(Mono.empty());

		Mono<String> monoResponse = tokenService.renewActivationToken(token, serverHttpRequest);

		StepVerifier.create(monoResponse).expectError(ActivationTokenValidationException.class).verify();
	}

	/**
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 * 
	 */
	@DisplayName("grants access to validate email activation token for Anonymous users")
	@Test
	void verifyValidateEmailActivationTokenAccessIsGrantedForAnonymous()
			throws URISyntaxException, UnknownHostException {
		String ipAddress = "166.197.174.199";
		UserBuilder user = UserBuilder.user();

		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);

		UserRepresentation userRepresentation = user.userRepresentation(userId);
		String username = userRepresentation.getUsername();
		String token = RandomGenerator.generateNewToken();

		String statusType = StatusType.VALID.name();
		ActivationToken activationToken = ActivationToken.builder().username(username).token(token)
				.creationDate(LocalDateTime.now()).status(statusType).build();

		when(tokenRepository.findById(eq(token))).thenReturn(Mono.just(activationToken));
		when(keycloakClient.updateEmailStatus(anyString(), anyBoolean())).thenReturn(Mono.just("SUCCESS"));

		Mono<String> monoResponse = tokenService.validateEmailActivationToken(token);

		StepVerifier.create(monoResponse).expectNextMatches(result -> result != null && StringUtils.isNotBlank(result))
				.verifyComplete();
	}

	/**
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 * 
	 */
	@DisplayName("grants access to validate email activation token but not found for Anonymous users")
	@Test
	void verifyValidateEmailActivationTokenNotFoundAccessIsGrantedForAnonymous()
			throws URISyntaxException, UnknownHostException {
		String ipAddress = "166.197.174.199";
		UserBuilder user = UserBuilder.user();

		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);

		UserRepresentation userRepresentation = user.userRepresentation(userId);
		String username = userRepresentation.getUsername();
		String token = RandomGenerator.generateNewToken();

		String statusType = StatusType.VALID.name();
		ActivationToken activationToken = ActivationToken.builder().username(username).token(token)
				.creationDate(LocalDateTime.now()).status(statusType).build();

		when(tokenRepository.findById(eq(token))).thenReturn(Mono.empty());
		when(keycloakClient.updateEmailStatus(anyString(), anyBoolean())).thenReturn(Mono.just("SUCCESS"));

		Mono<String> monoResponse = tokenService.validateEmailActivationToken(token);

		StepVerifier.create(monoResponse).expectNextMatches(result -> result != null && StringUtils.isNotBlank(result))
				.verifyComplete();
	}

	/**
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 * 
	 */
	@DisplayName("grants access to validate email activation token but expired for Anonymous users")
	@Test
	void verifyValidateEmailActivationTokenExpiredAccessIsGrantedForAnonymous()
			throws URISyntaxException, UnknownHostException {
		String ipAddress = "166.197.174.199";
		UserBuilder user = UserBuilder.user();

		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);

		UserRepresentation userRepresentation = user.userRepresentation(userId);
		String username = userRepresentation.getUsername();
		String token = RandomGenerator.generateNewToken();

		String statusType = StatusType.EXPIRED.name();
		ActivationToken activationToken = ActivationToken.builder().username(username).token(token)
				.creationDate(LocalDateTime.now()).status(statusType).build();

		when(tokenRepository.findById(eq(token))).thenReturn(Mono.just(activationToken));
		when(keycloakClient.updateEmailStatus(anyString(), anyBoolean())).thenReturn(Mono.just("SUCCESS"));

		Mono<String> monoResponse = tokenService.validateEmailActivationToken(token);

		StepVerifier.create(monoResponse).expectNextMatches(result -> result != null && StringUtils.isNotBlank(result))
				.verifyComplete();
	}

	/**
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 * 
	 */
	@DisplayName("grants access to expire email activation tokens for Anonymous users")
	@Test
	void verifyExpireActivationTokenRecordsAccessIsGrantedForAnonymous()
			throws URISyntaxException, UnknownHostException {

		int matchedCount = 2;
		Long matchedUpdate = 2L;
		UpdateResult updateResult = UpdateResult.acknowledged(matchedCount, matchedUpdate, null);
		when(mongoTemplate.updateMulti(any(Query.class), any(UpdateDefinition.class), eq(ActivationToken.class)))
				.thenReturn(Mono.just(updateResult));

		Mono<Long> monoResponse = tokenService.expireActivationTokenRecords();

		StepVerifier.create(monoResponse).expectNextMatches(result -> result == matchedUpdate).verifyComplete();
	}

	/**
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 * 
	 */
	@DisplayName("grants access to remove expired email activation tokens for Anonymous users")
	@Test
	void verifyRemoveExpiredActivationTokenRecordsAccessIsGrantedForAnonymous()
			throws URISyntaxException, UnknownHostException {

		int deletedCount = 2;
		DeleteResult deleteResult = DeleteResult.acknowledged(deletedCount);
		when(mongoTemplate.remove(any(Query.class), eq(ActivationToken.class))).thenReturn(Mono.just(deleteResult));

		Mono<Long> monoResponse = tokenService.removeExpiredActivationTokenRecords();

		StepVerifier.create(monoResponse).expectNextMatches(result -> result == deletedCount).verifyComplete();
	}

}
