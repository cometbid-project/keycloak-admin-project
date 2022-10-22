/**
 * 
 */
package com.keycloak.admin.client.integration.service.impl;

import com.keycloak.admin.client.integration.service.SES;

import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Gbenga
 *
 */
@Setter
@NoArgsConstructor
public class SESImpl implements SES {

	private String email;

	private String name;

	@Override
	public String getEmail() {
		// TODO Auto-generated method stub
		return this.email;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return this.name;
	}

}
