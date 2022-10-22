/**
 * 
 */
package com.keycloak.admin.client.models;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;

/**
 * @author Gbenga
 *
 */
@Value
@Builder
//@NoArgsConstructor
//@AllArgsConstructor
public class ActivationTokenModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4763960068359968140L;

	@JsonProperty("username")
	@NotBlank(message = "{Activation.username.notBlank}")
	private String username;

	@NotBlank(message = "{Activation.token.notBlank}")
	private String token;

	/**
	 * @param username
	 * @param token
	 */
	public ActivationTokenModel(@NotBlank(message = "{Activation.username.notBlank}") String username,
			@NotBlank(message = "{Activation.token.notBlank}") String token) {
		this.username = username;
		this.token = token;
	}

	/**
	 * @param username
	 * @param token
	 */
	public ActivationTokenModel() {
		this(null, null);
	}

}
