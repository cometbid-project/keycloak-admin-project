/**
 * 
 */
package com.keycloak.admin.client.integration.enums;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.keycloak.admin.client.integration.events.MessageEventType;

/**
 * @author Gbenga
 *
 */
public enum PaymentType implements MessageEventType {
	
	DEPOSIT("DEPOSIT"), TRANZ_STATUS("TRANZ_STATUS"), APPROVAL("APPROVAL");

	private final String name;

	PaymentType(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	// Implementing a fromString method on an enum type
	private static final Map<String, PaymentType> stringToEnum = new HashMap<String, PaymentType>();

	static { // Initialize map from constant name to enum constant
		for (PaymentType op : values()) {
			stringToEnum.put(op.toString(), op);
		}
	}

	// Returns Operation for string, or null if string is invalid
	public static PaymentType fromString(String typeName) {
		return stringToEnum.get(typeName);
	}

	public static Set<String> getAllTypes() {
		return stringToEnum.keySet();
	}

}
