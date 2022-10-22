/**
 * 
 */
package com.keycloak.admin.client.common.enums;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import lombok.Getter;

/**
 * @author Gbenga
 *
 */
public enum Role {

	ROLE_SECRETARY("SECRETARY", "ROLE_SECRETARY", 1), ROLE_MANAGER("MANAGER", "ROLE_MANAGER", 2),
	ROLE_APP_MANAGER("APPLICATION MANAGER", "ROLE_APP_MANAGER", 3), ROLE_ADMIN("ADMIN", "ROLE_ADMIN", 4),
	ROLE_STAFF("STAFF", "ROLE_STAFF", 5), ROLE_DEVELOPER("DEVELOPER", "ROLE_DEVELOPER", 6),
	ROLE_ACTUATOR("ACTUATOR MANAGER", "ROLE_ACTUATOR", 7), ROLE_MEMBER("MEMBER", "ROLE_MEMBER", 8),
	ROLE_USER("USER", "ROLE_USER", 9);

	@Getter
	private final String name;

	@Getter
	private final String value;

	@Getter
	private final int id;

	Role(String name, String value, Integer id) {
		this.name = name;
		this.id = id;
		this.value = value;
	}

	@Override
	public String toString() {
		return name;
	}

	// Implementing a fromString method on an enum type
	private static final Map<String, Role> stringToEnum = new HashMap<String, Role>();

	static { // Initialize map from constant name to enum constant
		for (Role op : values()) {
			stringToEnum.put(op.toString(), op);
		}
	}

	// Returns Operation for string, or null if string is invalid
	public static Role fromString(String typeName) {
		return stringToEnum.get(typeName.toUpperCase());
	}

	public static Set<String> getAllTypes() {
		return stringToEnum.keySet();
	}
}
