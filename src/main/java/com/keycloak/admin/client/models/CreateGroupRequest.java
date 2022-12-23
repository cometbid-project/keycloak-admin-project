/**
 * 
 */
package com.keycloak.admin.client.models;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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
@Builder
@Value
@Schema(name = "Create Group", description = "Create Group request model")
@Accessors(chain = true)
public class CreateGroupRequest implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4804102702577089856L;

	@Schema(name = "name", description = "group name", required = true)
	@Size(min = 1, max = 30, message = "{group.name.size}")
	@NotBlank(message = "{group.name.notBlank}")
	@JsonProperty("name")
	private String groupName;

	@Schema(name = "description", description = "group description")
	@Size(max = 70, message = "{group.desc.size}")
	@JsonProperty("description")
	private String description;

	/**
	 * @param groupName
	 * @param description
	 */
	@JsonCreator
	public CreateGroupRequest(
			@Size(min = 1, max = 30, message = "{group.name.size}") @NotBlank(message = "{group.name.notBlank}") String groupName,
			@Size(max = 70, message = "{group.desc.size}") String description) {
		super();
		this.groupName = groupName;
		this.description = description;
	}

	/**
	 * 
	 */
	public CreateGroupRequest(
			@Size(min = 1, max = 30, message = "{group.name.size}") @NotBlank(message = "{group.name.notBlank}") String groupName) {
		this(groupName, null);
	}

	/**
	 * 
	 */
	private CreateGroupRequest() {
		this(null, null);
	}

}
