/**
 * 
 */
package com.keycloak.admin.client.oauth.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.keycloak.admin.client.models.Username;
import com.keycloak.admin.client.common.activity.ActivityFactory;
import com.keycloak.admin.client.common.activity.ActivityLog;
import com.keycloak.admin.client.common.activity.ActivityLogVO;
import com.keycloak.admin.client.common.activity.enums.Action;
import com.keycloak.admin.client.common.activity.enums.ContentType;
import com.keycloak.admin.client.common.activity.enums.ObjectType;
import com.keycloak.admin.client.common.service.it.ActivityLogService;
import com.keycloak.admin.client.common.utils.DateUtil;
import com.keycloak.admin.client.common.utils.RandomGenerator;
import com.keycloak.admin.client.components.AuthenticatedUserMgr;
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;
import com.keycloak.admin.client.models.mappers.ActivityLogMapper;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Validated
@Transactional(readOnly = true)
@Service("activityService")
@PreAuthorize("isAuthenticated()")
public class ActivityLogServiceImpl implements ActivityLogService<ActivityLog, ActivityLogVO> {

	private final ActivityLogMapper activityLogMapper;
	private final AuthenticatedUserMgr authUserMgr;
	private final ReactiveMongoOperations mongoReactiveTemplate;

	/**
	 * 
	 * @param authUserMgr
	 * @param mongoReactiveTemplate
	 * @param activityLogMapper
	 */
	public ActivityLogServiceImpl(AuthenticatedUserMgr authUserMgr, ReactiveMongoOperations mongoReactiveTemplate,
			ActivityLogMapper activityLogMapper) {

		this.activityLogMapper = activityLogMapper;
		this.authUserMgr = authUserMgr;
		this.mongoReactiveTemplate = mongoReactiveTemplate;
	}

	/**
	 * 
	 * @param message
	 * @param contentType
	 * @return
	 */
	@Override
	@Transactional(readOnly = false)
	public Mono<ActivityLog> saveActivity(final String message, final ObjectType objectType,
			final ContentType contentType, final Action actionType, final Long timestamp) {

		log.info("Executing ActivityLogServiceImpl#saveActivity Service...");

		// TODO Auto-generated method stub
		return authUserMgr.getCurrentUser().flatMap(user -> Mono.just(Optional.of(user)))
				.defaultIfEmpty(Optional.empty())
				.flatMap(userOptional -> saveActivity(message, userOptional, actionType, objectType, contentType,
						timestamp))
				.doOnSuccess(c -> log.info("Activity log saved successfully"))
				.doOnError(e -> log.error("Activity log failed to save", e)).onErrorResume(e -> Mono.empty());
	}

	private Mono<ActivityLog> saveActivity(String message, Optional<Username> userOptional, Action actionType,
			ObjectType objectType, ContentType contentType, Long timestamp) {
		String subject = null;
		Username currentUser = null;

		log.info("Activity: Saving user audited activities...");

		if (!userOptional.isPresent()) {
			subject = "-";
			log.info("Activity: No authenticated user found");
		} else {
			currentUser = userOptional.get();

			subject = String.join("/", currentUser.getRoles(), currentUser.getUsername());
			log.info("Authenticated user found {}", currentUser);
		}

		LocalDateTime timeOfActivity = DateUtil.getLocalDateTimeFromLongMillisecs(timestamp);
		ZonedDateTime currentZonedDateTime = ZonedDateTime.of(timeOfActivity, ZoneOffset.UTC);

		String receiver = "";
		String tranxId = RandomGenerator.generateTransactionId(String.valueOf(timestamp));

		String activityStmt = ActivityFactory.createActivityStmt(subject, actionType, objectType, receiver, contentType,
				tranxId);

		log.info("Activity Statement: {}", activityStmt);
		log.info("Current User: {}", currentUser);
		log.info("Actual message: {}", message);
		log.info("Time of Activity: {}", currentZonedDateTime);

		ActivityLog activityLog = activityLogMapper.create(activityStmt, currentUser, message, currentZonedDateTime);

		log.info("Activity: {}", activityLog);

		// Save the activity
		return mongoReactiveTemplate.save(activityLog);
	}

	/**
	 * 
	 */
	@Override
	public Flux<String> findDefaultFields(Class<ActivityLog> domainClass) {
		// TODO Auto-generated method stub

		log.info("Executing ActivityLogServiceImpl#findDefaultFields Service...");

		return Flux.fromStream(ActivityLog.getMappedDefaultFields().stream());
	}

	/**
	 * 
	 */
	@Override
	public Mono<Integer> countDefaultFields(Class<ActivityLog> domainClass) {
		// TODO Auto-generated method stub

		log.info("Executing ActivityLogServiceImpl#countDefaultFields Service...");

		return Mono.justOrEmpty(ActivityLog.getMappedDefaultFields().size());
	}

}
