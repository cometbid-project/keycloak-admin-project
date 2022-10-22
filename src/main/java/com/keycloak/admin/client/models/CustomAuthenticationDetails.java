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
public class CustomAuthenticationDetails {

	private String subscriptionType;
	
	public CustomAuthenticationDetails() {
		this.subscriptionType = null;
	}

}
