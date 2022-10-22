/**
 * 
 */
package com.keycloak.admin.client.models;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Gbenga
 *
 */
@Data
@Schema(name="Role", description = "Role respresentation")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleVO {

	private String id;

	@Schema(name = "name", description = "role's name", required = true, example = "john_doe@yahoo.com")
	@JsonProperty("name")
	private String name;

	@Schema(name = "description", description = "roles' description", required = true, example = "john_doe@yahoo.com")
	@JsonProperty("description")
	private String description;
	
	@Builder.Default
	@JsonProperty("attributes")
	private Map<String, List<String>> attributes = new HashMap<>();
}
