/**
 * 
 */
package com.keycloak.admin.client.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.keycloak.admin.client.common.enums.SocialProvider;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * @author Gbenga
 *
 */
@Value
//@NoArgsConstructor
@Accessors(chain = true)
public class SocialLink {

	private String providerUserId;
	private SocialProvider socialProvider;

	/**
	 * 
	 * @param providerUserId
	 * @param socialProvider
	 */
	@JsonCreator
	public SocialLink(String providerUserId, SocialProvider socialProvider) {
		// TODO Auto-generated constructor stub
		super();
		this.providerUserId = providerUserId;
		this.socialProvider = socialProvider;
	}

	/**
	 * 
	 */
	public SocialLink() {
		this(null, null);
	}
}
