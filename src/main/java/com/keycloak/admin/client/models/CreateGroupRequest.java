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
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * @author Gbenga
 *
 */
@Value
@Accessors(chain = true)
public class CreateGroupRequest implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4804102702577089856L;

	@Size(min = 1, max = 20)
	@Schema(name = "groupName", description = "User groups", required = true)
	@NotBlank(message = "{group_name.notBlank}")
	@JsonProperty("group_name")
	private String groupName;

	/**
	 * @param roleName
	 */
	public CreateGroupRequest(@NotBlank(message = "{group_name.notBlank}") @Size(min = 1, max = 20) String groupName) {
		super();
		this.groupName = groupName;
	}

	/**
	 * @param groupName
	 */
	public CreateGroupRequest() {
		this.groupName = null;
	}

}
