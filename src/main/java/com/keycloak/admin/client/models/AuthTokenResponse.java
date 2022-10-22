/**
 * 
 */
package com.keycloak.admin.client.models;

import lombok.Data;
import lombok.Value;

/**
 *
 * @author Gbenga
 */
@Value
public class AuthTokenResponse {

	private String body;
	private String sessionId;

	public AuthTokenResponse() {
		this.body = null;
		this.sessionId = null;
	}

	/**
	 * @param body
	 * @param sessionId
	 */
	public AuthTokenResponse(String body, String sessionId) {
		this.body = body;
		this.sessionId = sessionId;
	}

}
