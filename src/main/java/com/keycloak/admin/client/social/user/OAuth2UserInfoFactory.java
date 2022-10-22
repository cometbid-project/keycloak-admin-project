package com.keycloak.admin.client.social.user;

import java.util.Map;

import com.keycloak.admin.client.common.enums.SocialProvider;
import com.keycloak.admin.client.exceptions.OAuth2AuthenticationProcessingException;

/**
 * 
 * @author Gbenga
 *
 */
public class OAuth2UserInfoFactory {

	public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {

		if (registrationId.equalsIgnoreCase(SocialProvider.GOOGLE.getProviderType())) {
			return new GoogleOAuth2UserInfo(attributes);
		} else if (registrationId.equalsIgnoreCase(SocialProvider.FACEBOOK.getProviderType())) {
			return new FacebookOAuth2UserInfo(attributes);
		} else if (registrationId.equalsIgnoreCase(SocialProvider.GITHUB.getProviderType())) {
			return new GithubOAuth2UserInfo(attributes);
		} else if (registrationId.equalsIgnoreCase(SocialProvider.LINKEDIN.getProviderType())) {
			return new LinkedinOAuth2UserInfo(attributes);
		} else if (registrationId.equalsIgnoreCase(SocialProvider.TWITTER.getProviderType())) {
			return new GithubOAuth2UserInfo(attributes);
		} else {
			throw new OAuth2AuthenticationProcessingException(String.format(
					"Sorry! Login with %s is not supported yet. Supported Registration id are: %s, %s, %s, %s, %s",
					registrationId, SocialProvider.GOOGLE.getProviderType(), SocialProvider.FACEBOOK.getProviderType(),
					SocialProvider.GITHUB.getProviderType(), SocialProvider.LINKEDIN.getProviderType(),
					SocialProvider.TWITTER.getProviderType()));
		}
	}
}