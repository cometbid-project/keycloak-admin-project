/**
 * 
 */
package com.keycloak.admin.client.models;

import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.keycloak.admin.client.validators.qualifiers.ValidPassword;

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
public class PasswordUpdateRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7151706463042473364L;

	@JsonProperty("old_password")
	@ValidPassword(message = "{req.old.password.valid}")
	@NotBlank(message = "{req.old.password.notBlank}")
	private String oldPassword;

	@JsonProperty("new_password")
	@ValidPassword(message = "{req.new.password.valid}")
	@NotBlank(message = "{req.new.password.notBlank}")
	private String newPassword;

}
