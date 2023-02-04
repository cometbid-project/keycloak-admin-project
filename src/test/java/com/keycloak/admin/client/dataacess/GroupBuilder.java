/**
 * 
 */
package com.keycloak.admin.client.dataacess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.github.javafaker.Faker;
import org.keycloak.representations.idm.GroupRepresentation;

import com.keycloak.admin.client.models.CreateGroupRequest;
import com.keycloak.admin.client.models.GroupVO;

import lombok.Data;

/**
 * @author Gbenga
 *
 */
@Data
public class GroupBuilder {

	private Faker faker;

	private UUID id = UUID.randomUUID();

	private String path = "/";

	private String name = "new_group";
	
	private String description;

	private Map<String, List<String>> attributes = new HashMap<>();

	private List<String> realmRoles = new ArrayList<>();

	private Map<String, List<String>> clientRoles = new HashMap<>();

	private GroupBuilder() {
		faker = new Faker();

		this.id = UUID.randomUUID();
		this.name = faker.team().name().toUpperCase();
		this.description = faker.lorem().sentence(1);
		this.path = faker.internet().url();
		this.realmRoles.add("ADMIN");
	}

	public static GroupBuilder group() {
		return new GroupBuilder();
	}

	public GroupBuilder withId(UUID id) {
		this.id = id;
		return this;
	}

	public GroupBuilder withName(String name) {
		this.name = name;
		return this;
	}

	public GroupBuilder withPath(String path) {
		this.path = path;
		return this;
	}
	
	public GroupBuilder withDescription(String description) {
		this.description = description;
		return this;
	}

	public GroupBuilder withAttributes(Map<String, List<String>> attributes) {
		this.attributes = attributes;
		return this;
	}

	public GroupBuilder withClientRoles(Map<String, List<String>> clientRoles) {
		this.clientRoles = clientRoles;
		return this;
	}

	public GroupBuilder withRealmRoles(List<String> realmRoles) {
		this.realmRoles = realmRoles;
		return this;
	}

	public GroupVO build() {

		return GroupVO.builder().id(this.id == null ? null : this.id.toString()).name(this.name).path(this.path)
				.attributes(this.attributes).realmRoles(this.realmRoles).clientRoles(this.clientRoles).build();
	}
	
	public CreateGroupRequest buildCreateGroupRequest( ) {
		
		return CreateGroupRequest.builder().groupName(this.name).description(this.description).build();
	}

	public static GroupRepresentation groupRepresentation(UUID id) {
		Faker lFaker = Faker.instance();

		GroupRepresentation representation = new GroupRepresentation();
		representation.setId(id.toString());
		representation.setName(lFaker.team().name());
		representation.setClientRoles(Collections.emptyMap());
		representation.setRealmRoles(Collections.emptyList());

		return representation;
	}

	public static List<GroupRepresentation> groupRepresentationList() {
		List<GroupRepresentation> arrayList = new ArrayList<>();

		arrayList.add(GroupBuilder.groupRepresentation(UUID.randomUUID()));
		arrayList.add(GroupBuilder.groupRepresentation(UUID.randomUUID()));

		return arrayList;
	}
	
	public static List<GroupVO> groupList() {
		List<GroupVO> arrayList = new ArrayList<>();

		arrayList.add(GroupBuilder.group().build());
		arrayList.add(GroupBuilder.group().build());
		arrayList.add(GroupBuilder.group().build());
		arrayList.add(GroupBuilder.group().build());

		return arrayList;
	}
}
