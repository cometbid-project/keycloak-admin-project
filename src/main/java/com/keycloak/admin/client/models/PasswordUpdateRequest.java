/**
 * 
 */
package com.keycloak.admin.client.models;

import java.io.Serializable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.keycloak.admin.client.validators.qualifiers.ValidPassword;

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
@Schema(name = "Password update", description = "Password update request model")
public class PasswordUpdateRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7151706463042473364L;

	@JsonProperty("old_password")
	@ValidPassword
	@Schema(name = "Current password", description = "Current password being used", required = true)
	@Size(min = 8, max = 50, message = "{password.size}")
	@NotBlank(message = "{old.password.notBlank}")
	private String oldPassword;

	@JsonProperty("new_password")
	@ValidPassword
	@Schema(name = "New password", description = "New chosen password that will replace the current password", required = true, example = "Makintoch@2013")
	@Size(min = 8, max = 50, message = "{password.size}")
	@NotBlank(message = "{new.password.notBlank}")
	private String newPassword;

	/**
	 * @param oldPassword
	 * @param newPassword
	 */
	@JsonCreator
	public PasswordUpdateRequest(
			@Size(min = 8, max = 50, message = "{password.size}") @NotBlank(message = "{old.password.notBlank}") String oldPassword,
			@Size(min = 8, max = 50, message = "{password.size}") @NotBlank(message = "{new.password.notBlank}") String newPassword) {
		super();
		this.oldPassword = oldPassword;
		this.newPassword = newPassword;
	}

	/**
	 * 
	 */
	private PasswordUpdateRequest() {
		this(null, null);
	}

}
