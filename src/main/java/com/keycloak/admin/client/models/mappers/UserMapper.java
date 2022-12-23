/**
 * 
 */
package com.keycloak.admin.client.models.mappers;

import static com.keycloak.admin.client.config.AuthProperties.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.keycloak.representations.idm.SocialLinkRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import com.keycloak.admin.client.common.enums.Role;
import com.keycloak.admin.client.common.utils.DateUtil;
import com.keycloak.admin.client.models.SocialLink;
import com.keycloak.admin.client.models.UserRegistrationRequest;
import com.keycloak.admin.client.models.UserVO;
import lombok.extern.log4j.Log4j2;

/**
 * @author Gbenga
 *
 */
@Log4j2
public class UserMapper {

	private UserMapper() {
	}

	public static UserVO toViewObject(UserRepresentation user) {

		log.info("User Representation Timestamp {}", user.getCreatedTimestamp());

		Long creationDate = user.getCreatedTimestamp();
		LocalDateTime creationDateTime = creationDate != null ? DateUtil.getLocalDateTimeFromLongMillisecs(creationDate)
				: null;

		List<SocialLinkRepresentation> socialRepresentationLinks = user.getSocialLinks();
		SocialLinkRepresentation socialLinkRep = null;
		if (socialRepresentationLinks != null && !socialRepresentationLinks.isEmpty()) {
			socialLinkRep = socialRepresentationLinks.get(0);
			//userVo.setSocialProvider(socialLinkRep.getSocialProvider());
			//userVo.setProviderUserId(socialLinkRep.getSocialUserId());
		}

		Long modifiedDate = null;
		boolean isAccountLocked = false;
		boolean isAccountExpired = false;
		boolean isMfaEnabled = false;
		
		if (user.getAttributes() != null) {

			// Extract Last Modified Date from Attributes
			List<String> profileLastModified = user.getAttributes().getOrDefault(LAST_MODIFIED_DATE,
					Collections.emptyList());
			if (profileLastModified != null && !profileLastModified.isEmpty()) {
				modifiedDate = Long.valueOf(profileLastModified.get(0));
				//userVo.setLastModifiedDate(DateUtil.getLocalDateTimeFromLongMillisecs(modifiedDate));
			}

			List<String> profileLocked = user.getAttributes().getOrDefault(PROFILE_LOCKED, Collections.emptyList());
			List<String> profileExpired = user.getAttributes().getOrDefault(PROFILE_EXPIRED, Collections.emptyList());
			List<String> totpEnabled = user.getAttributes().getOrDefault(TOTP_ENABLED, Collections.emptyList());

			if (profileLocked != null && !profileLocked.isEmpty()) {
				isAccountLocked = Boolean.getBoolean(profileLocked.get(0));
			}

			if (profileExpired != null && !profileExpired.isEmpty()) {
				isAccountExpired = Boolean.getBoolean(profileExpired.get(0));
			}

			if (totpEnabled != null && !totpEnabled.isEmpty()) {
				isMfaEnabled = Boolean.getBoolean(totpEnabled.get(0));
			}
		}

		return UserVO.builder().id(user.getId())
				.username(user.getUsername())
				.roles(new HashSet<>(user.getRealmRoles()))
				.email(user.getEmail())
				.firstName(user.getFirstName()) 
				.lastName(user.getLastName()) 
				.displayName(UserVO.displayName(user.getFirstName(), user.getLastName()))
				.emailVerified(Boolean.TRUE.equals(user.isEmailVerified()))
				.disabled(!Boolean.TRUE.equals(user.isEnabled()))
				// Set empty to avoid leakage
				.password(null)
				.createdDate(creationDateTime)
				.socialProvider(socialLinkRep != null ? socialLinkRep.getSocialProvider(): null)
				.providerUserId(socialLinkRep != null ? socialLinkRep.getSocialUserId() : null)
				.lastModifiedDate(modifiedDate != null ? DateUtil.getLocalDateTimeFromLongMillisecs(modifiedDate) : null) 
				.accountLocked(isAccountLocked)
				.expired(isAccountExpired)
				.enableMFA(isMfaEnabled) 
				.build();
	}

	public static UserRepresentation createUserRepresentation(UserRegistrationRequest regRequest, Role role) {

		UserRepresentation newUser = new UserRepresentation();
		newUser.setUsername(regRequest.getEmail());
		newUser.setEmail(regRequest.getEmail());
		newUser.setFirstName(regRequest.getFirstName());
		newUser.setLastName(regRequest.getLastName());
		newUser.setRealmRoles(Arrays.asList(role.toString()));
		newUser.setCreatedTimestamp(System.currentTimeMillis());

		return newUser;
	}

}
