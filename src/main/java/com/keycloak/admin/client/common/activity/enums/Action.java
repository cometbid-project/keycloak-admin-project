/**
 * 
 */
package com.keycloak.admin.client.common.activity.enums;

/**
 * @author Gbenga
 *
 */
public enum Action {
	CREATED("created a new"), UPDATED("updated"), SEARCHED("searched for"), DEACTIVATED("deactivated"),
	LOGIN("logged in"), LOGOUT("logged out"), CREDIT("credit"), DEBIT("debit"), TRANSFER("transfer"),
	GENERIC("Unknown"), DELETED("marked for deletion");

	String stmt;

	Action(String stmt) {
		this.stmt = stmt;
	}

	public String getStmt() {
		return this.stmt;
	}
}
