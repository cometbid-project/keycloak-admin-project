/**
 * 
 */
package com.keycloak.admin.client.models;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * @author Gbenga
 *
 */
@Data
@Accessors(chain = true)
public class LogoutRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2286729821859033431L;

	@Schema(name = "refreshToken", description = "Refresh token", required = true)
	@NotBlank(message = "{refreshToken.notBlank}")
	@JsonProperty("refresh_token")
	private String refreshToken;
}
