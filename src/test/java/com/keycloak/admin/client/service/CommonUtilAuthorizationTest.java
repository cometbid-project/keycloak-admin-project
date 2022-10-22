/**
 * 
 */
package com.keycloak.admin.client.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Predicate;
import org.springframework.context.ApplicationEvent;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ResourceUtils;

import com.keycloak.admin.client.common.enums.StatusType;
import com.keycloak.admin.client.common.geo.GeolocationUtils;
import com.keycloak.admin.client.common.utils.DateUtil;
import com.keycloak.admin.client.common.utils.LocaleContextUtils;
import com.keycloak.admin.client.common.utils.RandomGenerator;
import com.keycloak.admin.client.config.AppConfiguration;
import com.keycloak.admin.client.config.AuthProperties;
import com.keycloak.admin.client.config.MessageConfig;
import com.keycloak.admin.client.config.SecurityConfig;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.entities.ActivationToken;
import com.keycloak.admin.client.entities.UserloginLocation;
import com.keycloak.admin.client.events.CustomUserAuthActionEvent;
import com.keycloak.admin.client.events.UserAuthEventTypes;
import com.keycloak.admin.client.exceptions.ServiceUnavailableException;
import com.keycloak.admin.client.models.LoginLocation;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.oauth.service.CommonUtil;
import com.keycloak.admin.client.oauth.service.UserLocationServiceImpl;
import com.keycloak.admin.client.oauth.service.it.UserLocationService;
import com.keycloak.admin.client.repository.UserActivationTokenRepository;
import com.maxmind.geoip2.DatabaseReader;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ua_parser.Parser;

/**
 * @author Gbenga
 *
 */
@Log4j2
@WebFluxTest
@RecordApplicationEvents
@DisplayName("Verify Common util service")
@ContextConfiguration(classes = { AppConfiguration.class, SecurityConfig.class, MessageConfig.class,
		AuthProperties.class })
@Import({ CommonUtil.class, LocaleContextUtils.class, UserLocationServiceImpl.class })
//@ExtendWith({ SpringExtension.class })
class CommonUtilAuthorizationTest {

	@MockBean
	private UserLocationService userLocationService;

	@Autowired
	private ApplicationEvents applicationEvents;

	@Captor
	private ArgumentCaptor<CustomUserAuthActionEvent> eventArgumentCaptor;

	@MockBean
	private UserActivationTokenRepository tokenRepository;
	private static ServerHttpRequest serverHttpRequest;
	private static DatabaseReader databaseReader;
	private static Parser parser;

	@Autowired
	private CommonUtil commonUtil;

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

	@SuppressWarnings("unchecked")
	private void createPostConditions(String username) throws URISyntaxException {
		// UUID userId = UUID.randomUUID();
		String ipAddress = "166.197.174.199";
		// UserRegistrationRequest userRequest = UserBuilder.user().build();
		// String username = userRequest.getEmail();

		HttpHeaders headers = new HttpHeaders();
		headers.add("Forwarded", "host=84.198.58.199;proto=https");
		// headers.add("X-Forwarded-For", "103.0.513.125, 80.91.3.17, 120.192.335.629");
		headers.add("User-Agent", USER_AGENT);

		InetSocketAddress mockInetSocketAddress = Mockito.mock(InetSocketAddress.class);

		serverHttpRequest = MockServerHttpRequest.method(HttpMethod.GET, new URI("http://example.com/a%20b?q=a%2Bb"))
				.contentType(MediaType.APPLICATION_JSON).headers(headers).remoteAddress(mockInetSocketAddress).build();

		UserloginLocation usrLoginLoc = UserloginLocation.builder().id(UUID.randomUUID().toString()).ipAddr(ipAddress)
				.loginLocHis(Collections.emptyList()).username(username).build();

		String deviceDetails = GeolocationUtils.getDeviceDetails(USER_AGENT, parser);
		log.info("Device details {}", deviceDetails);

		LoginLocation loginLoc = GeolocationUtils.getUserRelativeLocation(ipAddress, deviceDetails, databaseReader);

		Mono<LoginLocation> monoLoginLoc = Mono.just(loginLoc);
		when((Mono<LoginLocation>) userLocationService.decodeUserLocation(serverHttpRequest)).thenReturn(monoLoginLoc);

	}

	/**
	 * Re-test
	 * 
	 * @throws URISyntaxException
	 * @throws UnknownHostException
	 */
	@DisplayName("email verification event granted to all roles")
	@Disabled
	@WithMockUser(roles = { "USER", "APP_MANAGER", "MANAGER", "ADMIN" })
	void verifysendEmailVerificationEventIsGrantedForAllRoles() throws URISyntaxException, UnknownHostException {

		UserBuilder user = UserBuilder.user();
		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);

		createPostConditions(userVO.getUsername());

		String token = RandomGenerator.generateNewToken();

		Mono<Void> monoResponse = commonUtil.sendEmailVerificationEvent(userVO, token,
				UserAuthEventTypes.ON_SOCIAL_USER_SIGNUP_COMPLETE, serverHttpRequest);

		StepVerifier.create(monoResponse).verifyComplete();
	}

	/**
	 * 
	 * @throws URISyntaxException
	 */
	@DisplayName("email activation event is published")
	@Disabled
	@WithMockUser(roles = { "USER", "APP_MANAGER", "MANAGER", "ADMIN" })
	void sendEmailVerificationPublishEvent() throws URISyntaxException {
		UserBuilder user = UserBuilder.user();

		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);

		createPostConditions(userVO.getUsername());

		String token = RandomGenerator.generateNewToken();

		Mono<Void> monoResponse = commonUtil.sendEmailVerificationEvent(userVO, token,
				UserAuthEventTypes.ON_SOCIAL_USER_SIGNUP_COMPLETE, serverHttpRequest);

		StepVerifier.create(monoResponse).verifyComplete();

		Assertions.assertEquals(1, applicationEvents.stream(CustomUserAuthActionEvent.class)
				.filter(event -> event.getUser().getUsername().equals(userVO.getUsername())).count());

		applicationEvents.stream().forEach(System.out::println);

		Predicate<? super ApplicationEvent> predicate = p -> p instanceof CustomUserAuthActionEvent;

		Assertions.assertTrue(applicationEvents.stream().anyMatch(predicate));
	}

	/**
	 * 
	 * @throws URISyntaxException
	 */
	@DisplayName("get new activation token while token not existing")
	@Disabled
	@WithMockUser(roles = { "USER", "APP_MANAGER", "MANAGER", "ADMIN" })
	void verifyGetNewActivationNonExistingToken() {
		UserBuilder user = UserBuilder.user();

		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);
		String username = userVO.getUsername();

		String token = RandomGenerator.generateNewToken();

		ActivationToken tokenModel = ActivationToken.builder().status(StatusType.VALID.toString()).username(username)
				.token(token).creationDate(DateUtil.now()).build();

		when(tokenRepository.existsById(anyString())).thenReturn(Mono.just(Boolean.FALSE));
		when(tokenRepository.save(any(ActivationToken.class))).thenReturn(Mono.just(tokenModel));

		Mono<ActivationToken> monoResponse = commonUtil.getNewActivationToken(userVO.getUsername());

		StepVerifier.create(monoResponse)
				.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result.getStatus())
						&& StringUtils.isNotBlank(result.getUsername()) && result.getToken().equals(token)
						&& result.getUsername().equals(username))
				.verifyComplete();

	}

	/**
	 * 
	 * @throws URISyntaxException
	 */
	@DisplayName("get new activation token while token already exist")
	@Test
	@WithMockUser(roles = { "USER", "APP_MANAGER", "MANAGER", "ADMIN" })
	void verifyGetNewActivationExistingToken() {
		UserBuilder user = UserBuilder.user();

		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);
		String username = userVO.getUsername();

		String token = RandomGenerator.generateNewToken();

		ActivationToken tokenModel = ActivationToken.builder().status(StatusType.VALID.toString()).username(username)
				.token(token).creationDate(DateUtil.now()).build();

		when(tokenRepository.existsById(anyString())).thenReturn(Mono.just(Boolean.TRUE));
		when(tokenRepository.save(any(ActivationToken.class))).thenReturn(Mono.just(tokenModel));

		Mono<ActivationToken> monoResponse = commonUtil.getNewActivationToken(userVO.getUsername());

		StepVerifier.create(monoResponse).expectError(ServiceUnavailableException.class).verify();
	}

}
