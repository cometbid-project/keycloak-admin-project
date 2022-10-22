package com.keycloak.admin.client.social.user;

import java.util.Map;
import java.util.Objects;

/**
 * 
 * @author Gbenga
 *
 */
public class GoogleOAuth2UserInfo extends OAuth2UserInfo {

	public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
		super(attributes);
	}

	@Override
	public String getId() {
		Object result = attributes.get("sub");
		return (String) (Objects.isNull(result) ? null: result);
	}

	@Override
	public String getName() {
		Object result = attributes.get("name");
		return (String) (Objects.isNull(result) ? "": result);
	}

	@Override
	public String getEmail() {
		Object result = attributes.get("email");
		return (String) (Objects.isNull(result) ? null: result);
	}

	@Override
	public String getImageUrl() {
		Object result = attributes.get("picture");
		return (String) (Objects.isNull(result) ? null: result);
	}
}