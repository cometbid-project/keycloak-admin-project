/**
 * 
 */
package com.keycloak.admin.client.models;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.keycloak.admin.client.validators.qualifiers.ValidEmail;

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
public class ForgotUsernameRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1399820146702381230L;

	@JsonProperty("email")
	@NotBlank(message = "{User.email.notBlank}")
	@Size(max = 330, message = "{User.email.size}")
	@ValidEmail(message = "{User.email.invalid}")
	private String email;

	/**
	 * @param email
	 */
	public ForgotUsernameRequest(
			@NotBlank(message = "{User.email.notBlank}") @Size(max = 330, message = "{User.email.size}") @ValidEmail(message = "{User.email.invalid}") String email) {
		this.email = email;
	}

	/**
	 * @param email
	 */
	public ForgotUsernameRequest() {
		this(null);
	}

}
