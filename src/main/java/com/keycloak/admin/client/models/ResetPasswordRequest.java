/**
 * 
 */
package com.keycloak.admin.client.models;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.keycloak.admin.client.validators.qualifiers.ValidEmail;

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
public class ResetPasswordRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6638402242122020624L;

	@JsonProperty("email")
	@ValidEmail(message = "{User.email.invalid}")
	@NotBlank(message = "{email.notBlank}")
	private String email;

}
