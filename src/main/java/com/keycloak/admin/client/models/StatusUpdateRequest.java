/**
 * 
 */
package com.keycloak.admin.client.models;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.keycloak.admin.client.common.enums.StatusType;
import com.keycloak.admin.client.validators.qualifiers.VerifyValue;

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
@Schema(name = "Status update", description = "User profile status update model")
public class StatusUpdateRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3812598934962451724L;

	/*
	@Schema(name = "username", description = "username (email)", required = true, example = "john_doe@yahoo.com")
	@JsonProperty("username")
	@NotBlank(message = "{username.notBlank}")
	@Size(max = 50, message = "{User.username.size}")
	private String username;
	*/

	@NotBlank(message = "{status.notBlank}")
	@VerifyValue(message = "{status.verifyValue}", value = StatusType.class)
	private String status;

	/**
	 * 
	 */
	private StatusUpdateRequest() {
		this(null);
	}

	
	/**
	 * @param username
	 * @param status
	 */
	@JsonCreator
	public StatusUpdateRequest(String status) {
		super();
		this.status = status;
		//this.username = username;
	}

}
