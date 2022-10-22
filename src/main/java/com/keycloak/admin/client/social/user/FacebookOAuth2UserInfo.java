package com.keycloak.admin.client.social.user;

import java.util.Map;
import java.util.Objects;

/**
 * 
 * @author Gbenga
 *
 */
public class FacebookOAuth2UserInfo extends OAuth2UserInfo {
	
	public FacebookOAuth2UserInfo(Map<String, Object> attributes) {
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
	@SuppressWarnings("unchecked")
	public String getImageUrl() {
		if (attributes.containsKey("picture")) {
			Map<String, Object> pictureObj = (Map<String, Object>) attributes.get("picture");
			
			if (pictureObj.containsKey("data")) {
				Map<String, Object> dataObj = (Map<String, Object>) pictureObj.get("data");
				if (dataObj.containsKey("url")) {
					return (String) dataObj.get("url");
				}
			}
		}
		return null;
	}
}