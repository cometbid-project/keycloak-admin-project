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
public enum SMSType implements MessageEventType {

	ACCT_BALANCE("ACCT_BALANCE"), ACCT_STATUS("ACCT_STATUS"), PASSWORD_RESET_NOTIFICATION("PASSWORD_RESET_REQUEST"),
	PASSWORD_CHANGED("PASSWORD_CHANGED");

	private final String name;

	SMSType(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	// Implementing a fromString method on an enum type
	private static final Map<String, SMSType> stringToEnum = new HashMap<String, SMSType>();

	static { // Initialize map from constant name to enum constant
		for (SMSType op : values()) {
			stringToEnum.put(op.toString(), op);
		}
	}

	// Returns Operation for string, or null if string is invalid
	public static SMSType fromString(String typeName) {
		return stringToEnum.get(typeName);
	}

	public static Set<String> getAllTypes() {
		return stringToEnum.keySet();
	}
}
