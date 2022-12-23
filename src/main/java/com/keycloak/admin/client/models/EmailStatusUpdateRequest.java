/**
 * 
 */
package com.keycloak.admin.client.models;

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
@Schema(name = "Email verification", description = "Email verification update model")
public class EmailStatusUpdateRequest {

	@Schema(name = "email", description = "email", required = true, example = "johndoe@yahoo.ca")
	@JsonProperty("email")
	@Size(max = 50, message = "{User.email.size}")
	@ValidEmail
	@NotBlank(message = "{User.email.notBlank}")
	private String email;

	@JsonProperty("verified")
	@Schema(name = "Verification status", description = "To update Email verification status as Verified or Unverified", required = true)
	private boolean verified;

	/**
	 * @param email
	 * @param verified
	 */
	public EmailStatusUpdateRequest() {
		this(null, false);
	}

	/**
	 * @param email
	 * @param verified
	 */
	@JsonCreator
	public EmailStatusUpdateRequest(
			@Size(max = 50, message = "{User.email.size}") @ValidEmail @NotBlank(message = "{reg.email.notBlank}") String email,
			boolean verified) {
		super();
		this.email = email;
		this.verified = verified;
	}

}
