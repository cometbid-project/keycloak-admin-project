/**
 * 
 */
package com.keycloak.admin.client.models;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

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
@NoArgsConstructor
@Schema(name = "User", description = "User details update model")
public class UserDetailsUpdateRequest {

	@Schema(name = "first name", description = "first name on profiles", required = true, example = "John")	
	@JsonProperty("first_name")
	@NotBlank(message = "{FirstName.notBlank}")
	@Size(min = 1, max = 200, message = "{FirstName.size}")
	private String firstName;

	@Schema(name = "Last name", description = "last name on profiles", required = true, example = "Doe")
	@JsonProperty("last_name")
	@NotBlank(message = "{LastName.notBlank}")
	@Size(min = 1, max = 200, message = "{LastName.size}")
	private String lastName;
	
	/**
	 * 
	 * @param firstName
	 * @param lastName
	 */
	public UserDetailsUpdateRequest(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}

}
