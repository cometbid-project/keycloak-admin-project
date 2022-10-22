/**
 * 
 */
package com.keycloak.admin.client.models;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.keycloak.admin.client.validators.qualifiers.ValidPassword;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Gbenga
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordFinalRequest implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -5105759840241421585L;
	
	@Schema(name = "username", description = "username (email)", required = true, example = "john_doe@yahoo.com")
	@JsonProperty("username")
	@NotBlank(message = "{req.new.username.notBlank}")
	@Size(max = 330, message = "{User.username.size}")
	protected String username;

	@JsonProperty("new_password")
	@ValidPassword(message = "{req.new.password.valid}")
	@NotBlank(message = "{req.new.password.notBlank}")
	private String newPassword;

	@JsonProperty("session_id")
	@NotBlank(message = "{req.new.password.session.notBlank}")
	private String sessionId;
}
