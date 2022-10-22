/**
 * 
 */
package com.keycloak.admin.client.models;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

//import com.cometbid.oauth2.demo.validators.qualifiers.ValidEmail;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.keycloak.admin.client.validators.qualifiers.ValidEmail;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Gbenga
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileStatusUpdateRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7906083395063594306L;

	@JsonProperty("email")
	@Schema(name = "email", description = "user's unique email", required = true, example = "john_doe@yahoo.com")
	@NotBlank(message = "{User.email.notBlank}")
	@Size(max = 330, message = "{User.email.size}")
	@ValidEmail(message = "{User.email.invalid}")
	private String email;

	@JsonProperty("enable")
	private boolean enable;
}
