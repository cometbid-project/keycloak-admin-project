package com.keycloak.admin.client.common.utils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.keycloak.admin.client.common.enums.Role;
import com.keycloak.admin.client.common.enums.SocialProvider;
import com.keycloak.admin.client.models.UserVO;

import lombok.extern.log4j.Log4j2;

/**
 * 
 * @author Chinna
 *
 */
@Log4j2
public class GeneralUtils {

	public static List<SimpleGrantedAuthority> buildSimpleGrantedAuthorities(final Set<Role> roles) {
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		for (Role role : roles) {
			authorities.add(new SimpleGrantedAuthority(role.getName()));
		}
		return authorities;
	}

	public static SocialProvider toSocialProvider(String providerId) {
		for (SocialProvider socialProvider : SocialProvider.values()) {
			if (socialProvider.getProviderType().equals(providerId)) {
				return socialProvider;
			}
		}
		return SocialProvider.LOCAL;
	}

	public static UserVO buildUserInfo(UserVO localUser) {

		return UserVO.builder().id(localUser.getId()).displayName(localUser.getDisplayName())
				.email(localUser.getEmail()).roles(new HashSet<>(localUser.getRoles())).build(); 
	}

	public static String getAppUrl(URI uri, String path, Map<String, String> parameters) {
		URIBuilder builder = new URIBuilder();
		builder.setScheme(uri.getScheme());
		builder.setHost(uri.getHost());
		builder.setPort(uri.getPort());

		builder.setPath(path != null ? path : StringUtils.EMPTY);

		List<NameValuePair> list = new ArrayList<>();
		if (parameters != null) {
			for (var entry : parameters.entrySet()) {
				BasicNameValuePair nameValuePair = new BasicNameValuePair(entry.getKey(), entry.getValue());
				list.add(nameValuePair);
			}
		}

		builder.setParameters(list);
		try {
			return builder.build().toURL().toString();
		} catch (URISyntaxException | MalformedURLException e) {
			// TODO Auto-generated catch block   
			// e.printStackTrace();
			String url = uri.toString() + path + "?" + buildParameters(parameters);
			log.error("Could not build URL from parameters...defaulting to {}", url);
			return url;
		}
	}

	private static String buildParameters(Map<String, String> parameters) {
		StringBuilder sb = new StringBuilder();
		if (parameters != null) {
			for (var entry : parameters.entrySet()) {
				sb.append(entry.getKey() + "=" + entry.getValue());
			}
		}

		return sb.toString();
	}
}
