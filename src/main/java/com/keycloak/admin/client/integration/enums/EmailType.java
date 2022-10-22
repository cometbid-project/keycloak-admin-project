/**
 * 
 */
package com.keycloak.admin.client.integration.enums;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import com.keycloak.admin.client.integration.events.MessageEventType;

/**
 * @author Gbenga
 *
 */
public enum EmailType implements MessageEventType {

	EMAIL_VERIFICATION("VERIFICATION", "Email Verification process", "verification-template.ftl"), // Used
	AGENT_REGISTRATION("AGENT REGISTRATION", "Welcome message", "agent-welcome-template.ftl"),
	ENQIURIES("CUSTOMER ENQIURIES", "Customer Enquiry", ""),
	SUPPORT_CONFIRMATION("SUPPORT CONFIRMATION EMAIL", "Support team acknowledgement", "auto-support-template.ftl"),
	DOMAIN_OFFER("DOMAIN OFFER", "Special & Dedicated Domain offer", "special-domain-offer-template.ftl"),
	RESET_PASSWORD_REQUEST("RESET PASSWORD", "Reset password Notification", "reset-password-template.ftl"),
	RESET_PASSWORD_COMPLETED("RESET PASSWORD COMPLETE", "Reset password complete Notification",
			"reset-password-template.ftl"),
	NEWSLETTER("WEEKLY NEWSLETTER", "Our weekly Newsletter", "weekly-newsletter-template.ftl"),
	MARKETING("MARKETING", "Marketing Information", "marketing-template.ftl"),
	NOTIFICATION("NOTIFICATION", "General Information", "notification-template.ftl"),
	REGISTRATION("REGISTRATION", "Marketing Information", "marketing-template.ftl"),
	VERIFICATION("VERIFICATION", "Marketing Information", "marketing-template.ftl"),
	PASSWORD_CHANGED("PASSWORD_CHANGED", "Marketing Information", "marketing-template.ftl"),
	USERNAME_REQUEST("USERNAME_REQUEST", "Marketing Information", "marketing-template.ftl"),
	STAFF_PROFILE_CREATION("STAFF_CREATED", "Marketing Information", "marketing-template.ftl"),
	USER_PROFILE_CREATION("USER_CREATED", "Marketing Information", "marketing-template.ftl"),
	MERCHANT_PROFILE_CREATION("MERCHANT_CREATED", "Marketing Information", "marketing-template.ftl"),
	MEMBER_REGISTRATION_REQUEST("MEMBER_PREREGISTRATION", "Member Registration Request", "marketing-template.ftl"),
	UNKNOWN_LOGIN_LOCATION("UNUSUAL_LOGIN_LOCATION", "Login attempt from different location",
			"unusual-login-location.ftl"),
	UNKNOWN_DEVICE_DETECTED("UNKNOWN_DEVICE", "Unknown Device Login Notification", "unknown-device-login.ftl"),
	LOGIN_NOTIFICATION("SUCCESSFUL LOGIN", "Login Notification", "login-notification.ftl"),
	TEMPORARY_PASSWORD("TEMPORARY PASSWORD", "Social Signup Notification", "signup-notification.ftl");

	@Getter
	private final String name;
	@Getter
	private final String subject;
	@Getter
	private final String emailTemplate;

	EmailType(String name, String subject, String filename) {
		this.name = name;
		this.subject = subject;
		this.emailTemplate = filename;
	}

	@Override
	public String toString() {
		return name;
	}

	// Implementing a fromString method on an enum type
	private static final Map<String, EmailType> stringToEnum = new HashMap<String, EmailType>();

	static { // Initialize map from constant name to enum constant
		for (EmailType op : values()) {
			stringToEnum.put(op.toString(), op);
		}
	}

	// Returns Operation for string, or null if string is invalid
	public static EmailType fromString(String typeName) {
		return stringToEnum.get(typeName);
	}

	public static Set<String> getAllTypes() {
		return stringToEnum.keySet();
	}

}
