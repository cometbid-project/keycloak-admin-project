/**
 * 
 */
package com.keycloak.admin.client.common.activity.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Gbenga
 *
 */
public enum ObjectType {

	MEMBER("Member"), MERCHANT("Branch"), MANAGER("Manager"), APP_MANAGER("Application Manager"),
	SECRETARY("Secretary"), USER_AUTH("login credential profile"), SELF_AUTH("his/her own login credential profile"),
	STAFF("Staff"), STAFF_ADDRESS("Staff's Address"), STAFF_PHONE("Staff's Phone"), STAFF_PHOTO("Staff's Photo"),
	STAFF_NEXTOFKIN("Staff's Next-Of-Kin"), MEMBER_ADDRESS("Staff's Address"), MEMBER_PHONE("Staff's Phone"),
	MEMBER_PHOTO("Member's Photo"), MEMBER_NEXTOFKIN("Staff's Next-Of-Kin"), SELF("his/her own profile"),
	ACCOUNT_TYPES("Branch's Account types"), BRANCH_ADDRESS("Branch's Address"), BRANCH_PHONE("Branch's Phone"),
	MERCHANT_SETTINGS("Branch's Settings"), MERCHANT_STATUS("Branch's Status"), ACTIVITY_LOG("Activity log"),
	MERCHANT_LOGO("Merchant's (branch) logo"), MEMBER_EMAIL("Member's Email"), BRANCH_EMAIL("Branch's email"),
	MEMBER_ALERT_OPTIONS("Member's Communication/Alert options"),
	MEMBER_SOCIAL_PROFILE("Member's Social Media Profiles");

	String object;

	ObjectType(String object) {
		this.object = object;
	}

	public String getObject() {
		return this.object;
	}

	// Implementing a fromString method on an enum type
	private static final Map<String, ObjectType> stringToEnum = new HashMap<String, ObjectType>();

	static { // Initialize map from constant name to enum constant
		for (ObjectType op : values()) {
			stringToEnum.put(op.toString(), op);
		}
	}

	// Returns Operation for string, or null if string is invalid
	public static ObjectType fromString(String typeName) {
		return stringToEnum.get(typeName);
	}

}
