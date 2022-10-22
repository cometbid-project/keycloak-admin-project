/**
 * 
 */
package com.keycloak.admin.client.models;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.keycloak.admin.client.validators.qualifiers.ValidEmail;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Gbenga
 *
 */
@Data
@Builder
//@AllArgsConstructor
//@NoArgsConstructor
@Schema(name = "User", description = "User details update model")
public class EmailStatusUpdateRequest {

	@JsonProperty("email")
	@NotBlank(message = "{User.email.notBlank}")
	@Size(max = 330, message = "{User.email.size}")
	@ValidEmail(message = "{User.email.invalid}")
	private String email;

	@JsonProperty("verified")
	private boolean verified;

	/**
	 * @param email
	 * @param verified
	 */
	public EmailStatusUpdateRequest(
			@NotBlank(message = "{User.email.notBlank}") @Size(max = 330, message = "{User.email.size}") @ValidEmail(message = "{User.email.invalid}") String email,
			boolean verified) {
		this.email = email;
		this.verified = verified;
	}

	/**
	 * @param email
	 * @param verified
	 */
	public EmailStatusUpdateRequest() {
		this(null, false);
	}

}
