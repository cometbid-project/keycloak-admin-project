/**
 * 
 */
package com.keycloak.admin.client.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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
//@NoArgsConstructor
@Schema(name = "User", description = "User details update model")
public class UserDetailsUpdateRequest {

	@Schema(name = "first name", description = "first name on profiles", required = true, example = "John")
	@JsonProperty("first_name")
	@NotBlank(message = "{FirstName.notBlank}")
	@Size(min = 1, max = 50, message = "{FirstName.size}")
	private String firstName;

	@Schema(name = "Last name", description = "last name on profiles", required = true, example = "Doe")
	@JsonProperty("last_name")
	@NotBlank(message = "{LastName.notBlank}")
	@Size(min = 1, max = 50, message = "{LastName.size}")
	private String lastName;

	/**
	 * 
	 */
	private UserDetailsUpdateRequest() {
		this(null, null);
	}

	/**
	 * @param firstName
	 * @param lastName
	 */
	@JsonCreator
	public UserDetailsUpdateRequest(
			@NotBlank(message = "{FirstName.notBlank}") @Size(min = 1, max = 50, message = "{FirstName.size}") String firstName,
			@NotBlank(message = "{LastName.notBlank}") @Size(min = 1, max = 50, message = "{LastName.size}") String lastName) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
	}

}
