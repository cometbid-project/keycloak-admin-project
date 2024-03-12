/**
 * 
 */
package com.keycloak.admin.client.models;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * @author Gbenga
 *
 */
@Value
@Builder
@XmlRootElement
@JsonInclude(Include.ALWAYS)
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
