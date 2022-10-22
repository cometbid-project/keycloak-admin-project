/**
 * 
 */
package com.keycloak.admin.client.common.activity.enums;

/**
 * @author Gbenga
 *
 */
public enum ContentType {

	ADDRESS("address"), PERSONAL("personal details"), NEXT_OF_KIN("next of kins"), USERNAME("username"),
	PASSWORD("password"), PHONE("phone"), ACCOUNT("cash account"), AUTH("authentication details"),
	LOGIN_RECORD("login records"), TOKEN("activation token"), SECRETARIES("Secretaries"), MANAGERS("Managers"),
	TRANSACTION("transaction logs"), APP_MANAGERS("Application Managers"), STAFFS("staffs"), PHOTO("Photo"),
	USERS("Users"), MEMBERS("Members"), ACTIVITY_AUDIT("Audit trails"), BRANCHES("Branch"),
	ACCOUNT_TYPES("account types"), APP_SETTINGS("Application settings"), USER_GROUP("User (realm) group"),
	USER_ROLE("User (realm) role"), USER_LOCATION("User location");

	String content;

	ContentType(String content) {
		this.content = content;
	}

	public String getContent() {
		return this.content;
	}
}