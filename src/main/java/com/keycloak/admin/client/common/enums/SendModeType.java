/**
 * 
 */
package com.keycloak.admin.client.common.enums;

/**
 * @author Gbenga
 *
 */
public enum SendModeType {

	EMAIL("Email"), SMS("Sms");

	private String modeType;

	public String getModeType() {
		return modeType;
	}

	SendModeType(final String modeType) {
		this.modeType = modeType;
	}
	
	public String toString() {
		return modeType;
	}
}
