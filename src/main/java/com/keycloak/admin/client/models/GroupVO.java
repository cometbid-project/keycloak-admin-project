/**
 * 
 */
package com.keycloak.admin.client.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

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
@Schema(name = "Group", description = "Group respresentation")
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupVO {

	private String id;

	@Schema(name = "name", description = "group's name", required = true, example = "john_doe@yahoo.com")
	@JsonProperty("name")
	private String name;

	@Schema(name = "path", description = "group's unique path", required = true, example = "john_doe@yahoo.com")
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

}
