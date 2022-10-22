/**
 * 
 */
package com.keycloak.admin.client.common.events;

import org.springframework.context.ApplicationEvent;

import com.keycloak.admin.client.common.activity.enums.ContentType;
import com.keycloak.admin.client.common.activity.enums.ObjectType;

import lombok.Getter;

/**
 * @author Gbenga
 *
 */
@Getter
public class GenericSpringEvent<T> extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1166071707676230386L;

	private T what;
	protected Object source;
	protected String message;
	protected boolean success;
	protected ObjectType objectType;
	private ContentType contentActedOn;
	private Object payload;

	public GenericSpringEvent(T what, Object source, String message, ObjectType objectType, ContentType content) {
		super(source);
		this.what = what;
		this.source = source;
		this.message = message;
		this.objectType = objectType;
		this.contentActedOn = content;
		this.success = true;
		this.payload = null;
	}

	public GenericSpringEvent(T what, Object source, String message, ObjectType objectType, ContentType content,
			Object payload) {
		super(source);
		this.what = what;
		this.source = source;
		this.message = message;
		this.objectType = objectType;
		this.contentActedOn = content;
		this.success = true;
		this.payload = payload;
	}

	public T getWhat() {
		return this.what;
	}

	public String getMessage() {
		return this.message;
	}

	@Override
	public String toString() {
		return "GenericSpringEvent [what=" + what + ", source=" + source + ", message=" + message + ", success="
				+ success + ", objectType=" + objectType + ", contentActedOn=" + contentActedOn + ", payload=" + payload
				+ "]";
	}
	
}
