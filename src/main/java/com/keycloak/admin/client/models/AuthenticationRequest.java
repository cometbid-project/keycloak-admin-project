/**
 * 
 */
package com.keycloak.admin.client.models;

import java.io.Serializable;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

/**
 * @author Gbenga
 *
 */
@Value
//@NoArgsConstructor
public class AuthenticationRequest implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = -6172203911260603601L;

	@Schema(name = "username", description = "username (email)", required = true, example = "john_doe@yahoo.com")
	@JsonProperty("username")
	@Size(max = 330, message = "{User.username.size}")
	protected String username;

	private String password;

	// private UsrLoginLoc location;
	public AuthenticationRequest(String username, String password) {
		this.username  = username;
		this.password = password;
	}
	
	/**
	 * 
	 */
	public AuthenticationRequest() {
		this(null, null);
	}

}
