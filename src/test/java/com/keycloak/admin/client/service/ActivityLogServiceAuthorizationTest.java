/**
 * 
 */
package com.keycloak.admin.client.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.keycloak.admin.client.common.activity.ActivityFactory;
import com.keycloak.admin.client.common.activity.ActivityLog;
import com.keycloak.admin.client.common.activity.enums.Action;
import com.keycloak.admin.client.common.activity.enums.ContentType;
import com.keycloak.admin.client.common.activity.enums.ObjectType;
import com.keycloak.admin.client.common.enums.StatusType;
import com.keycloak.admin.client.common.service.it.ActivityLogService;
import com.keycloak.admin.client.common.utils.DateUtil;
import com.keycloak.admin.client.common.utils.LocaleContextUtils;
import com.keycloak.admin.client.common.utils.RandomGenerator;
import com.keycloak.admin.client.components.AuthenticatedUserMgr;
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;
import com.keycloak.admin.client.config.AppConfiguration;
import com.keycloak.admin.client.config.AuthProfile;
import com.keycloak.admin.client.config.AuthProperties;
import com.keycloak.admin.client.config.MessageConfig;
import com.keycloak.admin.client.config.SecurityConfig;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.models.Username;
import com.keycloak.admin.client.models.mappers.ActivityLogMapper;
import com.keycloak.admin.client.models.mappers.UserActivationMapper;
import com.keycloak.admin.client.oauth.service.ActivityLogServiceImpl;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author Gbenga
 *
 */
@Log4j2
@WebFluxTest
@DisplayName("Verify Activity log service")
@ContextConfiguration(classes = { AuthProfile.class, AppConfiguration.class, SecurityConfig.class, MessageConfig.class,
		AuthProperties.class })
@Import({ UserActivationMapper.class, LocaleContextUtils.class, ActivityLogServiceImpl.class,
		CustomMessageSourceAccessor.class })
//@ExtendWith({ SpringExtension.class })
class ActivityLogServiceAuthorizationTest {

	@MockBean
	private ActivityLogMapper activityLogMapper;

	@MockBean
	private AuthenticatedUserMgr authUserMgr;
	@MockBean
	private ReactiveMongoTemplate mongoReactiveTemplate;

	@Autowired
	private ActivityLogService activityService;

	/**
	 * 
	 */
	@DisplayName("grants access to save activity log for users")
	@Test
	@WithMockUser(roles = { "USER", "APP_MANAGER", "MANAGER", "ADMIN" })
	void verifySaveActivityAccessIsGrantedForUser() {

		UserBuilder user = UserBuilder.user();

		UUID userId = UUID.randomUUID();
		UserVO userVO = user.userVo(userId);

		String joined = String.join(" ", userVO.getRoles());
		Username currentUser = new Username(userVO.getId(), joined);

		String receiver = "";
		String subject = userVO.getId() + "(" + userVO.getRoles() + ")";
		String message = "A new user was registered";
		String comment = "Credit Transaction";
		ObjectType objectType = ObjectType.MERCHANT;
		ContentType contentType = ContentType.ACCOUNT;
		Action actionType = Action.CREDIT;

		Instant timeInstant = Instant.now();
		Long timestamp = DateUtil.currentTimestamp();
		ZonedDateTime currentZonedDateTime = ZonedDateTime.ofInstant(timeInstant, ZoneOffset.UTC);

		String tranxId = RandomGenerator.generateTransactionId(String.valueOf(timestamp));

		String activityStmt = ActivityFactory.createActivityStmt(subject, actionType, objectType, receiver, contentType,
				tranxId);

		ActivityLog activityLog = ActivityLog.builder().id(UUID.randomUUID().toString())
				.status(StatusType.VALID.toString()).username(currentUser).activityStmt(activityStmt).comment(comment)
				.timestamp(currentZonedDateTime).build();

		when(activityLogMapper.create(anyString(), eq(currentUser), anyString(), any(ZonedDateTime.class)))
				.thenReturn(activityLog);
		when(authUserMgr.getCurrentUser()).thenReturn(Mono.just(currentUser));
		when(mongoReactiveTemplate.save(any(ActivityLog.class))).thenReturn(Mono.just(activityLog));

		@SuppressWarnings("unchecked")
		Mono<ActivityLog> monoResponse = activityService.saveActivity(message, objectType, contentType, actionType,
				timestamp);

		StepVerifier.create(monoResponse)
				.expectNextMatches(result -> result != null && StringUtils.isNotBlank(result.getStatus())
						&& StringUtils.isNotBlank(result.getActivityStmt()) && result.getCreationDate() != null
						&& result.getUsername().equals(currentUser))
				.verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("test find default fields")
	@Test
	@WithMockUser(roles = { "USER", "APP_MANAGER", "MANAGER", "ADMIN" })
	void verifyFindDefaultFieldsForUser() {

		Flux<String> fluxFields = activityService.findDefaultFields(ActivityLog.class);

		StepVerifier.create(fluxFields).expectNextCount(6)
				// .expectNextMatches(result -> StringUtils.isNotBlank(result))
				.verifyComplete();
	}

	/**
	 * 
	 */
	@DisplayName("test count default fields")
	@Test
	@WithMockUser(roles = { "USER", "APP_MANAGER", "MANAGER", "ADMIN" })
	void verifyCountDefaultFieldsForUser() {

		Mono<Integer> fluxFields = activityService.countDefaultFields(ActivityLog.class);

		StepVerifier.create(fluxFields).expectNext(6)
				// .expectNextMatches(result -> StringUtils.isNotBlank(result))
				.verifyComplete();
	}

}
