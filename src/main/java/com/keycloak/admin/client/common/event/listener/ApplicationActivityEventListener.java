/**
 * 
 */
package com.keycloak.admin.client.common.event.listener;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.keycloak.admin.client.common.activity.ActivityLog;
import com.keycloak.admin.client.common.activity.ActivityLogVO;
import com.keycloak.admin.client.common.activity.enums.Action;
import com.keycloak.admin.client.common.activity.enums.ContentType;
import com.keycloak.admin.client.common.activity.enums.ObjectType;
import com.keycloak.admin.client.common.events.GenericSpringEvent;
import com.keycloak.admin.client.common.service.it.ActivityLogService;
import com.keycloak.admin.client.config.SchedulerConfig;
import com.keycloak.admin.client.error.handlers.LamdaExceptionHandler;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Component
public class ApplicationActivityEventListener implements ApplicationEventListener {

	private final ActivityLogService<ActivityLog, ActivityLogVO> activityService;
	private final Executor executor;

	private BlockingQueue<GenericSpringEvent<String>> blockingQueue;

	public ApplicationActivityEventListener(@Qualifier("eventTaskExecutor") Executor executor,
			SchedulerConfig appConfig,
			@Qualifier("activityService") ActivityLogService<ActivityLog, ActivityLogVO> activityService) {
		this.executor = executor;
		this.activityService = activityService;

		int QUEUE_CAPACITY = appConfig.getEventThreadPoolSize();
		blockingQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
	}

	@Override
	@Async
	@EventListener
	public Mono<Void> handleGenericApplicationEvent(@NonNull GenericSpringEvent<String> event) {
		log.info("Received spring generic event - type: {}", event.getWhat());

		// Queue up the event for processing
		this.blockingQueue.offer(event);
		
		//starts the executor thread to process Queue		
		beginProcessing();
		
		return Mono.empty();
	}

	public void beginProcessing() {

		log.info("accept method executing...");

		this.executor.execute(executeTask());
	}

	private Runnable executeTask() {

		return LamdaExceptionHandler.handleCheckedExceptionRunnable(() -> {
			while (true) {

				log.info("Executing endless loop...");

				GenericSpringEvent<String> event = blockingQueue.take();

				persistNewActivity(event).subscribe(data -> log.info("A new Activity log: {}", data),
						e -> log.error("Activity log Error: {}", e.getMessage()),
						() -> log.info("Activity Stream finished"));
			}
		});
	}

	private Mono<ActivityLog> persistNewActivity(GenericSpringEvent<String> event) {
		String whatEvent = event.getWhat();
		String eventMessage = event.getMessage();
		Long eventTimestamp = event.getTimestamp();
		ContentType contentType = event.getContentActedOn();
		ObjectType objectType = event.getObjectType();

		Action actionType = null;
		if (contains(whatEvent, "CREATED") || contains(whatEvent, "SAVED")) {
			actionType = Action.CREATED;
		} else if (contains(whatEvent, "UPDATED") || contains(whatEvent, "CHANGED")) {
			actionType = Action.UPDATED;
		} else if (contains(whatEvent, "SEARCHED")) {
			actionType = Action.SEARCHED;
		} else if (contains(whatEvent, "DELETED") || contains(whatEvent, "REMOVED")) {
			actionType = Action.DELETED;
		}

		log.info("Activity-details: {}, {}, {}, {}", eventMessage, objectType, contentType, actionType);
		log.info("Activity- time: {}", eventTimestamp);

		return activityService.saveActivity(eventMessage, objectType, contentType, actionType, eventTimestamp);
	}

	private boolean contains(String source, String substr) {
		return Pattern.compile(Pattern.quote(substr), Pattern.CASE_INSENSITIVE).matcher(source).find();
	}
}
