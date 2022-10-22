/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.keycloak.admin.client.models;

import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.keycloak.admin.client.validators.qualifiers.ValidEmail;
import com.keycloak.admin.client.validators.qualifiers.ValidPassword;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 *
 * @author Gbenga
 */
@Value
@Builder
//@AllArgsConstructor
//@NoArgsConstructor
@Schema(name = "User", description = "User registration model")
public class UserRegistrationRequest implements Serializable {

	/**  
	 *
	 */
	private static final long serialVersionUID = 8163431475910655299L;

	/**
	 * @param firstName
	 * @param lastName
	 * @param email
	 * @param password
	 */
	public UserRegistrationRequest() {
		this.firstName = null;
		this.lastName = null;
		this.email = null;
		this.password = null;
	}

	/**
	 * @param firstName
	 * @param lastName
	 * @param email
	 * @param password
	 */
	@Builder
	public UserRegistrationRequest(
			@NotBlank(message = "{FirstName.notBlank}") @Size(min = 1, max = 200, message = "{FirstName.size}") String firstName,
			@NotBlank(message = "{LastName.notBlank}") @Size(min = 1, max = 200, message = "{LastName.size}") String lastName,
			@ValidEmail(message = "{User.email.invalid}") @NotBlank(message = "{reg.email.notBlank}") String email,
			@Size(min = 6, message = "{Size.userDto.password}") @NotBlank(message = "{reg.password.notBlank}") String password) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.password = password;
	}

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

	@Schema(name = "email", description = "email", required = true, example = "johndoe@yahoo.ca")
	@JsonProperty("email")
	@ValidEmail(message = "{User.email.invalid}")
	@NotBlank(message = "{reg.email.notBlank}")
	private String email;

	@Schema(name = "password", description = "chosen password", required = true, example = "makintoch@2013")
	@JsonProperty("password")
	@ValidPassword(message = "{req.new.password.valid}")
	@Size(min = 6, message = "{Size.userDto.password}")
	@NotBlank(message = "{reg.password.notBlank}")
	private String password;

}
