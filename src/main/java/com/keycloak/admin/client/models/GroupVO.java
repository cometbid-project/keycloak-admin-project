/**
 * 
 */
package com.keycloak.admin.client.models;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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
@Schema(name = "Group", description = "Group representation")
@Builder
//@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupVO {

	private String id;

	@Schema(name = "name", description = "group name", required = true)
	@Size(min = 1, max = 30, message = "{group.name.size}")
	@NotBlank(message = "{group.name.notBlank}")
	@JsonProperty("name")
	private String name;

	@Schema(name = "path", description = "group unique path")
	@Size(max = 50, message = "{group.path.size}")
	//@NotBlank(message = "{group.path.notBlank}")
	@JsonProperty("path")
	private String path;

	@Builder.Default
	@JsonProperty("attributes")
	private Map<String, List<String>> attributes = new HashMap<>();

	@Builder.Default
	@JsonProperty("realm_roles")
	private List<String> realmRoles = new ArrayList<>();

	@Builder.Default
	@JsonProperty("client_roles")
	private Map<String, List<String>> clientRoles = new HashMap<>();

	/**
	 * @param id
	 * @param name
	 * @param path
	 * @param attributes
	 * @param realmRoles
	 * @param clientRoles
	 */
	@JsonCreator
	public GroupVO(String id, String name, String path, Map<String, List<String>> attributes, List<String> realmRoles,
			Map<String, List<String>> clientRoles) {
		super();
		this.id = id;
		this.name = name;
		this.path = path;
		this.attributes = attributes;
		this.realmRoles = realmRoles;
		this.clientRoles = clientRoles;
	}

	/**
	 * @param id
	 * @param name
	 * @param path
	 */
	private GroupVO(String id, String name, String path) {
		
		this(id, name, path, new HashMap<>(), new ArrayList<>(), new HashMap<>());
	}

}
