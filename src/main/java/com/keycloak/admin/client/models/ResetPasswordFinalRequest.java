/**
 * 
 */
package com.keycloak.admin.client.models;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.keycloak.admin.client.validators.qualifiers.ValidPassword;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

/**
 * @author Gbenga
 *
 */
@Value
//@AllArgsConstructor
//@NoArgsConstructor
@Builder
@Schema(name = "Reset Password final", description = "final Reset Password request model")
public class ResetPasswordFinalRequest implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -5105759840241421585L;

	@Schema(name = "username", description = "username (email)", required = true)
	@JsonProperty("username")
	@NotBlank(message = "{username.notBlank}")
	@Size(max = 50, message = "{User.username.size}")
	private String username;

	@JsonProperty("new_password")
	@ValidPassword
	@NotBlank(message = "{new.password.notBlank}")
	@Size(min = 8, max = 50, message = "{password.size}")
	@Schema(name = "Password", description = "New password to change to", required = true)
	private String newPassword;

	@JsonProperty("session_id")
	@NotBlank(message = "{password.session.notBlank}")
	@Schema(name = "Session id", description = "Reset password session id", required = true)
	private String sessionId;

	/**
	 * 
	 */
	private ResetPasswordFinalRequest() {
		this(null, null, null);
	}

	/**
	 * @param username
	 * @param newPassword
	 * @param sessionId
	 */
	@JsonCreator
	public ResetPasswordFinalRequest(
			@NotBlank(message = "{username.notBlank}") @Size(max = 50, message = "{User.username.size}") String username,
			@NotBlank(message = "{new.password.notBlank}") String newPassword,
			@NotBlank(message = "{password.session.notBlank}") String sessionId) {
		super();
		this.username = username;
		this.newPassword = newPassword;
		this.sessionId = sessionId;
	}

}
