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
public enum ResponseType {
	
	ERROR("Error", "503"), SUCCESS("Success", "200"), PARTIAL_SUCCESS("Partial_Success", "206");

	@Getter
	private final String name;
	
	@Getter  
	private final String status;

	ResponseType(String name, String status) {
		this.name = name;
		this.status = status;  
	}

	@Override
	public String toString() {
		return name;
	}

	// Implementing a fromString method on an enum type
	private static final Map<String, ResponseType> stringToEnum = new HashMap<>();

	static { // Initialize map from constant name to enum constant
		for (ResponseType op : values()) {
			stringToEnum.put(op.toString().toLowerCase(), op);
		}
	}

	// Returns Operation for string, or null if string is invalid
	public static ResponseType fromString(String typeName) {
		return stringToEnum.get(typeName);
	}

	public static Set<String> getAllTypes() {
		return stringToEnum.keySet();
	}


}

