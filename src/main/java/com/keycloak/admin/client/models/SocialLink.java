/**
 * 
 */
package com.keycloak.admin.client.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.keycloak.admin.client.common.enums.SocialProvider;
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
