/**
 * 
 */
package com.keycloak.admin.client.models;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

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
//@AllArgsConstructor
//@NoArgsConstructor
@Builder
@Schema(name = "Reset Password", description = "Reset Password request model")
public class ResetPasswordRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6638402242122020624L;

	@JsonProperty("email")
	@ValidEmail
	@NotBlank(message = "{resetPassword.email.notBlank}")
	private String email;

	/**
	 * @param email
	 */
	@JsonCreator
	public ResetPasswordRequest(@ValidEmail @NotBlank(message = "{resetPassword.email.notBlank}") String email) {
		super();
		this.email = email;
	}

	/**
	 * 
	 */
	private ResetPasswordRequest() {
		this(null);
	}

}
