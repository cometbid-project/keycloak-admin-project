/**
 * 
 */
package com.keycloak.admin.client.models;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import java.util.Collections;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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
@Builder
//@AllArgsConstructor
//@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "Role", description = "Role respresentation")
public class RoleVO {

	private String id;

	@Schema(name = "name", description = "role name", required = true)
	@Size(min = 1, max = 30, message = "{role.name.size}")
	@NotBlank(message = "{role.name.notBlank}")
	@JsonProperty("name")
	private String name;

	@Size(max = 70, message = "{role.desc.size}")
	@Schema(name = "description", description = "role description")
	@JsonProperty("description")
	private String description;

	@Builder.Default
	@JsonProperty("attributes")
	private Map<String, List<String>> attributes = new HashMap<>();

	/**
	 * @param id
	 * @param name
	 * @param description
	 * @param attributes
	 */
	@JsonCreator
	public RoleVO(String id, String name, String description, Map<String, List<String>> attributes) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.attributes = attributes;
	}

	/**
	 * 
	 */
	private RoleVO() {
		this(null, null, null, Collections.emptyMap());
	}

}
