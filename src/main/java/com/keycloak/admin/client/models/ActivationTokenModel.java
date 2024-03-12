/**
 * 
 */
package com.keycloak.admin.client.models;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;

/**
 * @author Gbenga
 *
 */
@Value
@Builder
//@NoArgsConstructor
//@AllArgsConstructor
@Schema(name = "Activation token", description = "Activation token model")
public class ActivationTokenModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4763960068359968140L;

	@JsonProperty("username")
	@NotBlank(message = "{activation.username.notBlank}")
	@Schema(name = "Username(Email)", description = "Username of a user")
	private String username;

	@NotBlank(message = "{activation.token.notBlank}")
	@Schema(name = "Activation token", description = "User profile unique activation token")
	private String token;

	/**
	 * @param username
	 * @param token
	 */
	@JsonCreator
	public ActivationTokenModel(@NotBlank(message = "{activation.username.notBlank}") String username,
			@NotBlank(message = "{activation.token.notBlank}") String token) {
		this.username = username;
		this.token = token;
	}

	/**
	 * @param username
	 * @param token
	 */
	private ActivationTokenModel() {
		this(null, null);
	}

}
