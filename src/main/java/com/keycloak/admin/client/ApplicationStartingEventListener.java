/**
 * 
 */
package com.keycloak.admin.client;

import java.time.LocalDateTime;

import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;

import com.keycloak.admin.client.common.utils.DateUtil;

import lombok.extern.log4j.Log4j2;

/**
 * @author Gbenga
 *
 */
@Log4j2
public class ApplicationStartingEventListener implements ApplicationListener<ApplicationStartingEvent> {

	@Override
	public void onApplicationEvent(ApplicationStartingEvent applicationStartingEvent) {
		LocalDateTime timeOfDay = DateUtil.getLocalDateTimeFromLongMillisecs(applicationStartingEvent.getTimestamp());

		log.info("Application Starting Event logged at {}", timeOfDay);
	}
}