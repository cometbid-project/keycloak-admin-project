/**
 * 
 */
package com.keycloak.admin.client.models;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.keycloak.admin.client.validators.qualifiers.ValidEmail;

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
@Builder
//@AllArgsConstructor
//@NoArgsConstructor
@Schema(name = "Forgot Username", description = "Forgot Username request model")
public class ForgotUsernameRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1399820146702381230L;

	@Schema(name = "email", description = "email", required = true, example = "johndoe@yahoo.ca")
	@JsonProperty("email")
	@Size(max = 50, message = "{User.email.size}")
	@ValidEmail
	@NotBlank(message = "{User.email.notBlank}")
	private String email;

	/**
	 * @param email
	 */
	public ForgotUsernameRequest() {
		this(null);
	}

	/**
	 * @param email
	 */
	@JsonCreator
	public ForgotUsernameRequest(
			@Size(max = 50, message = "{User.email.size}") @ValidEmail @NotBlank(message = "{User.email.notBlank}") String email) {
		super();
		this.email = email;
	}

}
