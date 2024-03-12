/**
 * 
 */
package com.keycloak.admin.client.models;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

/**
 * @author Gbenga
 *
 */
@Value
@Builder
//@NoArgsConstructor
@Schema(name = "User login credential", description = "Authentication request with password-based user credentials")
public class AuthenticationRequest implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = -6172203911260603601L;

	@Schema(name = "username", description = "username (email)", required = true, example = "john_doe@yahoo.com")
	@JsonProperty("username")
	@NotBlank(message = "{username.notBlank}")
	@Size(max = 50, message = "{User.username.size}")
	protected String username;

	@Schema(name = "password", description = "login password", required = true, example = "Makintoch@2013")
	@JsonProperty("password")
	@NotBlank(message = "{password.notBlank}")
	private String password;

	/**
	 * 
	 */
	private AuthenticationRequest() {
		this(null, null);
	}

	/**
	 * @param username
	 * @param password
	 */
	@JsonCreator
	public AuthenticationRequest(
			@NotBlank(message = "{username.notBlank}") @Size(max = 50, message = "{User.username.size}") String username,
			@NotBlank(message = "{password.notBlank}") String password) {
		super();
		this.username = username;
		this.password = password;
	}

}
