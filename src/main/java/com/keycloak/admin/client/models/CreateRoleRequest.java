/**
 * 
 */
package com.keycloak.admin.client.models;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * @author Gbenga
 *
 */
@Value
@Builder
@Schema(name = "Create Role", description = "Create Role request model")
@Accessors(chain = true)
public class CreateRoleRequest implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -53466764341987983L;

	@Schema(name = "name", description = "role name", required = true)
	@Size(min = 1, max = 30, message = "{role.name.size}")
	@NotBlank(message = "{role.name.notBlank}")
	@JsonProperty("name")
	private String roleName;

	@Size(max = 70, message = "{role.desc.size}")
	@Schema(name = "description", description = "role description")
	@JsonProperty("description")
	private String description;

	/**
	 * @param roleName
	 * @param description
	 */
	@JsonCreator
	public CreateRoleRequest(
			@Size(min = 1, max = 30, message = "{role.name.size}") @NotBlank(message = "{role.name.notBlank}") String roleName,
			@Size(max = 70, message = "{role.desc.size}") String description) {
		super();
		this.roleName = roleName;
		this.description = description;
	}

	/**
	 * 
	 */
	public CreateRoleRequest(
			@Size(min = 1, max = 30, message = "{role.name.size}") @NotBlank(message = "{role.name.notBlank}") String roleName) {
		this(roleName, null);
	}

	/**
	 * 
	 */
	private CreateRoleRequest() {
		this(null, null);
	}

}
