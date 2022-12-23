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
public enum StatusType {
	
	VALID("ACTIVE"), EXPIRED("EXPIRE"), LOCKED("LOCK"), DISABLED("DISABLE");    

	@Getter
	private final String name;

	StatusType(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	// Implementing a fromString method on an enum type
	private static final Map<String, StatusType> stringToEnum = new HashMap<String, StatusType>();

	static { // Initialize map from constant name to enum constant
		for (StatusType op : values()) {
			stringToEnum.put(op.toString(), op);
		}
	}

	// Returns Operation for string, or null if string is invalid
	public static StatusType fromString(String typeName) {
		return stringToEnum.get(typeName.toUpperCase());
	}

	public static Set<String> getAllTypes() {
		return stringToEnum.keySet();
	}

}

