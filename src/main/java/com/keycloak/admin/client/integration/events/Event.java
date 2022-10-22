/**
 * 
 */
package com.keycloak.admin.client.integration.events;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.keycloak.admin.client.common.utils.DateUtil;

/**
 * @author Gbenga
 *
 */
public abstract class Event<K, T, E extends MessageEventType> {

	private E eventType;
	private K key;
	private T data;
	private ZonedDateTime eventCreationTime;

	public Event() {
		this.eventType = null;
		this.key = null;
		this.data = null;
		this.eventCreationTime = null;
	}

	public Event(E eventType, K key, T data) {
		this.eventType = eventType;
		this.key = key;
		this.data = data;
		this.eventCreationTime = DateUtil.toZonedDateTime(LocalDateTime.now(), ZoneId.systemDefault(), ZoneOffset.UTC);
	}

	public E getEventType() {
		return eventType;
	}

	public K getKey() {
		return key;
	}

	public T getData() {
		return data;
	}

	public ZonedDateTime getEventCreationDate() {
		return eventCreationTime;
	}

	public abstract String getEventId();

}
