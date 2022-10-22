/**
 * 
 */
package com.keycloak.admin.client.models;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Gbenga
 *
 */
@Data
@Builder
@NoArgsConstructor
public class SearchUserRequest {

	@Schema(name = "first name", description = "first name on profiles", example = "John")
	@Size(min = 1, max = 200, message = "{FirstName.size}")
	private String firstName;

	@Schema(name = "Last name", description = "last name on profiles", example = "Doe")
	@Size(min = 1, max = 200, message = "{LastName.size}")
	private String lastName;

	@Schema(name = "email", description = "email", example = "johndoe@yahoo.ca")
	private String email;

	@Schema(name = "Verified Emails?", description = "email verification status", required = true)
	@NotBlank(message = "{reg.email.notBlank}")
	private boolean emailVerified;

	/**
	 * @param firstName
	 * @param lastName
	 * @param email
	 */
	public SearchUserRequest(String firstName, String lastName, String email, boolean emailVerified) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.emailVerified = emailVerified;
	}

}
