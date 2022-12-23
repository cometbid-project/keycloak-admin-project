/**
 * 
 */
package com.keycloak.admin.client.models;

import com.fasterxml.jackson.annotation.JsonCreator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Value;

/**
 *
 * @author Gbenga
 */
@Value
@Schema(name = "Authentication token", description = "Authentication token response model")
public class AuthTokenResponse {

	@Schema(name = "Response body", description = "Authentication token response body")
	private String body;

	@Schema(name = "Session token", description = "Session token assigned to manage oauth2 request")
	private String sessionId;

	/**
	 * 
	 */
	private AuthTokenResponse() {
		this(null, null);
	}

	/**
	 * @param body
	 * @param sessionId
	 */
	@JsonCreator
	public AuthTokenResponse(String body, String sessionId) {
		this.body = body;
		this.sessionId = sessionId;
	}

}
