/**
 * 
 */
package com.keycloak.admin.client.models;

import lombok.Value;

/**
 * @author Gbenga
 *
 */
@Value
public class AuthenticationDetails {

	private String subscriptionType;
	
	public AuthenticationDetails() {
		this.subscriptionType = null;
	}

}
