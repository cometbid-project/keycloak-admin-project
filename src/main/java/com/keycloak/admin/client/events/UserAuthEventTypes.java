/**
 * 
 */
package com.keycloak.admin.client.events;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import lombok.Getter;

/**
 * @author Gbenga
 *
 */
public enum UserAuthEventTypes {

	ON_USER_REGISTRATION_COMPLETE("OnUserRegistrationCompleteEvent"),
	ON_USER_SIGNUP_COMPLETE("OnUserSignUpCompleteEvent"),
	ON_PASSWORD_CHANGED_COMPLETE("OnPasswordChangeCompleteEvent"),
	ON_USERNAME_REQUEST_COMPLETE("OnUsernameRequestCompleteEvent"),
	ON_PASSWORD_RESET_REQUEST("OnPasswordResetRequestEvent"),
	ON_MEMBER_REGISTRATION_REQUEST("OnMemberRegistrationRequestEvent"),
	ON_MEMBER_REGISTRATION_COMPLETE("OnMemberRegistrationCompleteEvent"),
	ON_PASSWORD_RESET_COMPLETE("OnPasswordUpdateCompleteEvent"), ON_SUCCESS_LOGIN("OnSuccessfulLoginEvent"),
	ON_FAILED_LOGIN("OnFailedLoginEvent"), ON_UNKNOWN_DEVICE_LOGIN("OnUnknownDeviceLoginEvent"),
	ON_NEW_LOGIN_LOCATION("OnNewLoginLocationEvent"), ON_DIFFERENT_LOGIN_LOCATION("OnDifferentLocationLoginEvent"),
	ON_ACTIVATION_TOKEN_RENEWAL_REQUEST("OnEmailActivationRenewalRequestEvent"),
	ON_SOCIAL_USER_SIGNUP_COMPLETE("OnSocialUserSignUpCompleteEvent"), 
	ON_OTPCODE_REQUEST("OnOtpRequestEvent");

	@Getter
	private final String name;

	UserAuthEventTypes(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	// Implementing a fromString method on an enum type
	private static final Map<String, UserAuthEventTypes> stringToEnum = new HashMap<String, UserAuthEventTypes>();

	static { // Initialize map from constant name to enum constant
		for (UserAuthEventTypes op : values()) {
			stringToEnum.put(op.toString(), op);
		}
	}

	// Returns Operation for string, or null if string is invalid
	public static UserAuthEventTypes fromString(String typeName) {
		return stringToEnum.get(typeName);
	}

	public static Set<String> getAllTypes() {
		return stringToEnum.keySet();
	}

}
