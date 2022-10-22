/**
 * 
 */
package com.keycloak.admin.client.models;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Gbenga
 *
 */
@Data
@Accessors(chain = true)
public class CreateRoleRequest implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -53466764341987983L;

	@NotBlank(message = "{role_name.notBlank}")
	@Schema(name = "roleName", description = "User roles", required = true)
	@Size(min = 1, max = 30)
	@JsonProperty("role_name")
	private String roleName;

	/**
	 * @param roleName
	 */
	public CreateRoleRequest(@NotBlank(message = "{role_name.notBlank}") @Size(min = 1, max = 30) String roleName) {
		super();
		this.roleName = roleName;
	}

	/**
	 * @param groupName
	 */
	public CreateRoleRequest() {
		this(null);
	}

}
