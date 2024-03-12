/**
 * 
 */
package com.keycloak.admin.client.models;

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * @author Gbenga
 *
 */
@Value
@Builder
@Schema(name = "Password reset token", description = "Password reset token update request model")
public class PasswordResetTokenResponse {

	/**
	 * @param sessionId
	 */
	@JsonCreator
	public PasswordResetTokenResponse(@NotBlank(message = "{password.session.notBlank}") String sessionId) {
		super();
		this.sessionId = sessionId;
	}

	/**
	 * 
	 */
	private PasswordResetTokenResponse() {
		this(null);
	}

	@JsonProperty("session_id")
	@NotBlank(message = "{password.session.notBlank}")
	@Schema(name = "Session token", description = "Reset password session token", required = true)
	private String sessionId;

}
