/**
 * 
 */
package com.keycloak.admin.client.models;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonCreator;
//import com.cometbid.oauth2.demo.validators.qualifiers.ValidEmail;
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
//@AllArgsConstructor
//@NoArgsConstructor
@Builder
@Schema(name = "Profile activation", description = "Profile activation update request model")
public class ProfileActivationUpdateRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7906083395063594306L;

	@Schema(name = "user", description = "user identifier", required = true)
	private String userId;

	@JsonProperty("enable")
	private boolean enable;

	/**
	 * @param username
	 * @param enable
	 */
	@JsonCreator
	public ProfileActivationUpdateRequest(String userId, boolean enable) {
		super();
		this.userId = userId;
		this.enable = enable;
	}

	/**
	 * 
	 */
	private ProfileActivationUpdateRequest() {
		this(null, false);
	}

}
