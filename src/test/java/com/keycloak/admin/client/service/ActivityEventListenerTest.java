/**
 * 
 */
package com.keycloak.admin.client.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.keycloak.admin.client.common.activity.ActivityFactory;
import com.keycloak.admin.client.common.activity.ActivityLog;
import com.keycloak.admin.client.common.activity.enums.Action;
import com.keycloak.admin.client.common.activity.enums.ContentType;
import com.keycloak.admin.client.common.activity.enums.ObjectType;
import com.keycloak.admin.client.common.enums.StatusType;
import com.keycloak.admin.client.common.event.listener.ApplicationActivityEventListener;
import com.keycloak.admin.client.common.events.ActivityEventTypes;
import com.keycloak.admin.client.common.events.GenericSpringEvent;
import com.keycloak.admin.client.common.service.it.ActivityLogService;
import com.keycloak.admin.client.common.utils.BlockingThreadPoolExecutor;
import com.keycloak.admin.client.common.utils.DateUtil;
import com.keycloak.admin.client.common.utils.RandomGenerator;
import com.keycloak.admin.client.config.SchedulerConfig;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.models.Username;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Log4j2
//@RecordApplicationEvents
@DisplayName("Verify Activity event listener service")
@ExtendWith({ SpringExtension.class })
class ActivityEventListenerTest {

	@MockBean
	private ActivityLogService activityService;
	@MockBean
	private SchedulerConfig appConfig;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

	}

	@BeforeAll
	static void initializeData() throws FileNotFoundException, IOException, URISyntaxException {

		log.info("Creates a blockingConnectionScheduler with connectionPoolSize = {}", 10);

		// BlockHound.install(new ReactorBlockHoundIntegration(), new MyIntegration());
		// =============================================================================
	}

	/**
	 * 
	 */
	@DisplayName("event listener to save activity log for users")
	@Test
	// @WithMockUser(roles = { "USER", "APP_MANAGER", "MANAGER", "ADMIN" })
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

		Mono<ActivityLog> monoActivityLog = Mono.just(activityLog);

		when(activityService.saveActivity(anyString(), any(ObjectType.class), any(ContentType.class), any(Action.class),
				anyLong())).thenReturn(monoActivityLog);		
		when(appConfig.getEventThreadPoolSize()).thenReturn(10);
		
		Executor executor = threadPoolTaskExecutor();		
		ApplicationActivityEventListener eventListener = new ApplicationActivityEventListener(executor, appConfig, activityService);
		
		GenericSpringEvent appEvent = new GenericSpringEvent<>(ActivityEventTypes.AUTH_PROFILE_CREATED_EVENT,
				StringUtils.EMPTY, message, ObjectType.USER_AUTH, ContentType.AUTH, null);
		
		eventListener.handleGenericApplicationEvent(appEvent);
	}
	
	
	 Executor threadPoolTaskExecutor() {

		final int QUEUE_CAPACITY = 10;
		final int QUEUE_POOL_SIZE = 1;
		final int MAX_QUEUE_POOL_SIZE = 1;
		final int KEEP_ALIVE_TIME = 1000;
		final int MAX_SLEEP_TIME = 1000;
		
		BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<Runnable>(QUEUE_CAPACITY);

		BlockingThreadPoolExecutor executor = new BlockingThreadPoolExecutor(QUEUE_POOL_SIZE, MAX_QUEUE_POOL_SIZE,
				KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS, blockingQueue);

		executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				log.info("Event Worker Task Rejected : {} time: {}", Thread.currentThread().getName(), DateUtil.now());

				log.info("Waiting for a second !!");
				try {

					Thread.sleep(MAX_SLEEP_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				log.info("Lets add another time : " + r);
				executor.execute(r);
			}
		});

		// Let start all core threads initially
		executor.prestartAllCoreThreads();

		return executor;
	}


}
