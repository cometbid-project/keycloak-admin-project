/**
 * 
 */
package com.keycloak.admin.client.models;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * @author Gbenga
 *
 */
@Value
@Accessors(chain = true)
public class LogoutRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2286729821859033431L;

	@Schema(name = "Refresh token", description = "Refresh token", required = true)
	@NotBlank(message = "{refreshToken.notBlank}")
	@JsonProperty("refresh_token")
	private String refreshToken;

	/**
	 * @param refreshToken
	 */
	@JsonCreator
	public LogoutRequest(@NotBlank(message = "{refreshToken.notBlank}") String refreshToken) {
		super();
		this.refreshToken = refreshToken;
	}

	/**
	 * 
	 */
	private LogoutRequest() {
		this(null);
	}

}
