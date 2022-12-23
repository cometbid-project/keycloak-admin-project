/**
 * 
 */
package com.keycloak.admin.client.dataacess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import com.github.javafaker.Faker;
import com.keycloak.admin.client.common.enums.Role;
import com.keycloak.admin.client.models.CreateRoleRequest;
import com.keycloak.admin.client.models.GroupVO;
import com.keycloak.admin.client.models.RoleVO;

import lombok.Data;

/**
 * @author Gbenga
 *
 */
@Data
public class RoleBuilder {

	private Faker faker;

	private UUID id;

	private String name;

	private String description = "New Role description";

	private Map<String, List<String>> attributes = new HashMap<>();

	private RoleBuilder() {

		faker = new Faker();

		this.id = UUID.randomUUID();
		this.name = getRandomRole().getName();
		this.description = faker.lorem().sentence(1);
	}

	public static RoleBuilder role() {
		return new RoleBuilder();
	}

	public RoleBuilder withId(UUID id) {
		this.id = id;
		return this;
	}

	public RoleBuilder withName(String name) {
		this.name = name;
		return this;
	}

	public RoleBuilder withDescription(String description) {
		this.description = description;
		return this;
	}

	public RoleBuilder withRealmRoles(Map<String, List<String>> attributes) {
		this.attributes = attributes;
		return this;
	}

	public RoleVO roleVo() {

		return RoleVO.builder().id(id == null ? null : id.toString()).name(this.name).description(this.description)
				.attributes(this.attributes).build();
	}

	public CreateRoleRequest build() {
		return new CreateRoleRequest(this.name, this.description);
	}

	public static Role getRandomRole() {
		Set<String> setOfRoles = Role.getAllTypes();
		int num = setOfRoles.size();

		List<String> arrayList = List.copyOf(setOfRoles);

		int i = ThreadLocalRandom.current().nextInt(0, num - 1);

		return Role.fromString(arrayList.get(i));
	}

	public static Role getRandomRoleExclude(String roleToExclude) {

		List<String> arrayList = Role.getAllTypes().stream().filter(r -> !r.equalsIgnoreCase(roleToExclude))
				.collect(Collectors.toList());

		int num = arrayList.size();

		int i = ThreadLocalRandom.current().nextInt(0, num);

		return Role.fromString(arrayList.get(i));
	}

	public RoleRepresentation roleRepresentation(UUID id, Boolean clientRole) {

		RoleRepresentation representation = new RoleRepresentation();
		representation.setId(id.toString());
		representation.setName(this.name);
		representation.setClientRole(clientRole);
		representation.setDescription(this.description);
		representation.setComposite(false);
		representation.setAttributes(Collections.emptyMap());

		return representation;
	}

	public static ClientRepresentation clientRepresentation(UUID id) {

		ClientRepresentation representation = new ClientRepresentation();
		representation.setId(id.toString());
		representation.setName("client_name");
		representation.setClientId(id.toString());
		representation.setSecret("clientSecret");
		representation.setDescription("client_description");
		representation.setEnabled(true);

		return representation;
	}

	public static List<RoleRepresentation> rolesRepresentationList() {
		Set<String> setOfRoles = Role.getAllTypes();
		List<String> arrayList = List.copyOf(setOfRoles);

		return arrayList.stream()
				.map(roleName -> RoleBuilder.role().withName(roleName).roleRepresentation(UUID.randomUUID(), false))
				.collect(Collectors.toList());
	}

	public static List<ClientRepresentation> clientRepresentationList() {
		List<ClientRepresentation> arrayList = new ArrayList<>();

		arrayList.add(RoleBuilder.clientRepresentation(UUID.randomUUID()));
		// arrayList.add(RoleBuilder.clientRepresentation(UUID.randomUUID()));

		return arrayList;
	}
}
