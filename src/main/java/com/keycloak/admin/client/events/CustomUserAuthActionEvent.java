/**
 * 
 */
package com.keycloak.admin.client.events;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationEvent;

import com.keycloak.admin.client.events.dto.UserDTO;

/**
 * @author Gbenga
 *
 */
@SuppressWarnings("serial")
public class CustomUserAuthActionEvent extends ApplicationEvent {

	private final List<String> appUrlList;
	private final UserDTO user;
	private final UserAuthEventTypes authEventTypes;

	public CustomUserAuthActionEvent(final UserDTO user, UserAuthEventTypes authEventTypes, List<String> appUrlList) {
		super(user);
		this.appUrlList = appUrlList;
		this.user = user;
		this.authEventTypes = authEventTypes;
	}

	public CustomUserAuthActionEvent(final UserDTO user, UserAuthEventTypes authEventTypes) {
		super(user);
		this.appUrlList = new ArrayList<>();
		this.user = user;
		this.authEventTypes = authEventTypes;
	}

	public UserDTO getUser() {
		return this.user;
	}

	public boolean addAppUrl(String appUrl) {
		return this.appUrlList.add(appUrl);
	}

	public List<String> getAppUrlList() {
		return this.appUrlList;
	}

	public UserAuthEventTypes getAuthEventTypes() {
		return this.authEventTypes;
	}

}
