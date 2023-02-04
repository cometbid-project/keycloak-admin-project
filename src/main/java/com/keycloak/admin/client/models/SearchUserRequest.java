/**
 * 
 */
package com.keycloak.admin.client.models;

import javax.validation.constraints.Size;

import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "Search Criteria", description = "Search user profiles criteria model")
public class SearchUserRequest {
	
	@Schema(name = "username", description = "username (email)", required = true, example = "john_doe@yahoo.com")
	@JsonProperty("username")
	@Size(max = 50, message = "{User.username.size}")
	protected String username;
	
	@Schema(name = "first name", description = "first name on profiles", required = true, example = "John")
	@JsonProperty("first_name")
	@Size(max = 50, message = "{FirstName.size}")
	private String firstName;

	@Schema(name = "Last name", description = "last name on profiles", required = true, example = "Doe")
	@JsonProperty("last_name")
	@Size(max = 50, message = "{LastName.size}")
	private String lastName;

	@JsonProperty("email")
	@Schema(name = "email", description = "email", example = "johndoe@yahoo.ca")
	@Size(max = 50, message = "{User.email.size}")
	private String email;

	@JsonProperty("verified_emails")
	@Schema(name = "Verified Emails?", description = "email verification status", required = true)
	private boolean emailVerified;

	/**
	 * @param firstName
	 * @param lastName
	 * @param email
	 */
	@JsonCreator
	public SearchUserRequest(String username, String firstName, String lastName, String email, boolean emailVerified) {
		super();
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.emailVerified = emailVerified;
	}

	/**
	 * 
	 */
	private SearchUserRequest() {
		this(null, null, null, null, false);
	}

}
