/**
 * 
 */
package com.keycloak.admin.client.models;

import com.keycloak.admin.client.common.enums.SocialProvider;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Gbenga
 *
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class SocialLink {

	private String providerUserId;
	private SocialProvider socialProvider;

	public SocialLink(String providerUserId, SocialProvider socialProvider) {
		// TODO Auto-generated constructor stub
		this.providerUserId = providerUserId;
		this.socialProvider = socialProvider;
	}

}
