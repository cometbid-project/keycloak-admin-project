/**
 * 
 */
package com.keycloak.admin.client.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * @author Gbenga
 *
 */
@Data
public class PasswordResetTokenResponse {

	public PasswordResetTokenResponse(String id) { 
		// TODO Auto-generated constructor stub
		this.sessionId = id;
	}

	@JsonProperty("session_id")
	private String sessionId;

}
