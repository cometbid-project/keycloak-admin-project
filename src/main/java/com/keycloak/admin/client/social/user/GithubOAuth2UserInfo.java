package com.keycloak.admin.client.social.user;

import java.util.Map;
import java.util.Objects;

/**
 * 
 * @author Gbenga
 *
 */
public class GithubOAuth2UserInfo extends OAuth2UserInfo {

	public GithubOAuth2UserInfo(Map<String, Object> attributes) {
		super(attributes);
	}

	@Override
	public String getId() {
		Object result = attributes.get("id");
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
		Object result = attributes.get("avatar_url");
		return (String) (Objects.isNull(result) ? null: result);
	}
}