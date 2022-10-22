package com.keycloak.admin.client.social.user;

import java.util.Map;
import java.util.Objects;

/**
 * 
 * @author Gbenga
 *
 */
public class LinkedinOAuth2UserInfo extends OAuth2UserInfo {

	public LinkedinOAuth2UserInfo(Map<String, Object> attributes) {
		super(attributes);
	}

	@Override
	public String getId() {
		Object result = attributes.get("id");
		return (String) (Objects.isNull(result) ? null: result);
	}

	@Override
	public String getName() {
		Object firstNameResult = attributes.get("localizedFirstName");			
		Object lastNameResult = attributes.get("localizedLastName");
		
		String firstName = (String) (Objects.isNull(lastNameResult) ? "": lastNameResult);		
		String lastName = (String) (Objects.isNull(firstNameResult) ? "": firstNameResult);
		
		return firstName + " " + lastName;
	}

	@Override
	public String getEmail() {
		Object result = attributes.get("emailAddress");
		return (String) (Objects.isNull(result) ? null: result);
	}

	@Override
	public String getImageUrl() {
		Object result = attributes.get("pictureUrl");
		return (String) (Objects.isNull(result) ? null: result);
	}
}

