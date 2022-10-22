/**
 * 
 */
package com.keycloak.admin.client.integration.enums;

import com.keycloak.admin.client.integration.events.MessageEventType;

/**
 * @author Gbenga
 *
 */
public enum CommonEventType implements MessageEventType {

	CREATE_MERCHANT, DELETE_MERCHANT, UPDATE_MERCHANT, 
	CREATE_STAFF, UPDATE_STAFF, DELETE_STAFF, 
	CREATE_MERCHANT_SETTINGS, UPDATE_MERCHANT_SETTINGS, DELETE_MERCHANT_SETTINGS;

}
