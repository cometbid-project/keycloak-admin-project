/**
 * 
 */
package com.keycloak.admin.client.common.event.listener;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;
import com.keycloak.admin.client.events.CustomUserAuthActionEvent;
import com.keycloak.admin.client.events.UserAuthEventTypes;
import com.keycloak.admin.client.events.dto.UserDTO;
import com.keycloak.admin.client.integration.enums.EmailType;
import com.keycloak.admin.client.integration.messaging.EmailValidationDto;
import com.keycloak.admin.client.integration.messaging.factory.EmailMessageFactory;
import com.keycloak.admin.client.integration.service.EmailClientService;
import com.keycloak.admin.client.integration.service.SmsClientService;
import com.maxmind.geoip2.DatabaseReader;

import lombok.extern.log4j.Log4j2;
import reactor.core.scheduler.Scheduler;
import ua_parser.Parser;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Component
public class ApplicationEmailActionEventListener {

	private final EmailClientService emailClient;
	private final SmsClientService smsClient;
	private final CustomMessageSourceAccessor messageSource;
	private final Parser parser;
	private final DatabaseReader databaseReader;

	@Autowired
	private Environment env;

	/**
	 * @param service
	 * @param emailClient
	 * @param messages
	 * @param mailSender
	 * @param env
	 */
	public ApplicationEmailActionEventListener(EmailClientService emailClient, SmsClientService smsClient,
			MessageSource messages, Scheduler scheduler, DatabaseReader databaseReader, Parser parser,
			CustomMessageSourceAccessor messageSource) {
		super();
		this.emailClient = emailClient;
		this.smsClient = smsClient;
		this.messageSource = messageSource;
		this.parser = parser;
		this.databaseReader = databaseReader;
	}

	

	private void handleDifferentLocationLoginEvent(final CustomUserAuthActionEvent event) {
		final UserDTO user = event.getUser();

		List<String> requiredUrls = event.getAppUrlList();

		final String changePassUri = requiredUrls.get(0);
		final String enableLocUri = requiredUrls.get(1);

		final String message = messageSource.getLocalizedMessage("message.differentLocation",
				new Object[] { new Date().toString(), user.getLocation(), user.getIp(), enableLocUri, changePassUri });

		EmailValidationDto emailMessage = EmailMessageFactory
				.buildUnfamiliarLocationNotificationEmailMessage(user.getUsername(), user.getEmail(), message);

		emailClient.publishSignUpEvent(emailMessage, EmailType.UNKNOWN_LOGIN_LOCATION);
	}

	@Async
	@EventListener
	public void handleUserRegisteredEvent(final CustomUserAuthActionEvent event) {
		UserAuthEventTypes authEventTypes = event.getAuthEventTypes();

		switch (authEventTypes) {
		case ON_MEMBER_REGISTRATION_COMPLETE:
			//this.confirmMemberRegistration(event);
			break;
		case ON_MEMBER_REGISTRATION_REQUEST:
			//this.confirmMemberRegistrationRequest(event);
			break;
		case ON_USER_SIGNUP_COMPLETE:
			this.sendEmailVerificationNotification(event);
			break;
		case ON_SOCIAL_USER_SIGNUP_COMPLETE:
			this.sendTemporaryPasswordEmail(event);
			break;
		case ON_PASSWORD_RESET_REQUEST:
			this.confirmPasswordResetRequest(event);
			break;
		case ON_USERNAME_REQUEST_COMPLETE:
			this.sendUsernameRequestConfirmation(event);
			break;
		case ON_USER_REGISTRATION_COMPLETE:
			//this.confirmUserRegistration(event);
			break;
		case ON_PASSWORD_RESET_COMPLETE:
			this.sendUserPasswordResetCompletedNotification(event);
			break;
		case ON_DIFFERENT_LOGIN_LOCATION:
			this.handleDifferentLocationLoginEvent(event);
			break;
		default:
			log.error("Invalid User Auth event type specified....");
			break;
		}
	}

	private void sendTemporaryPasswordEmail(CustomUserAuthActionEvent event) {

		// TODO Auto-generated method stub
		final UserDTO user = event.getUser();
		String displayName = user.getName();
		String email = user.getEmail();
		String socialProvider = user.getSocialProvider();
		final String password = user.getPassword();

		String message = String
				.format("You recently signed up using %s, and your profile has been setup with a temporary password. "
						+ "/nTo gain access to your profile you will need to change it to your preferred password. "
						+ "Use this password %s temporarily.", socialProvider, password);

		EmailValidationDto emailMessage = EmailMessageFactory.buildEmailVerificationMessage(email, displayName, message,
				null);

		emailClient.publishSignUpEvent(emailMessage, EmailType.TEMPORARY_PASSWORD);
	}

	private void sendEmailVerificationNotification(CustomUserAuthActionEvent event) {
		// TODO Auto-generated method stub
		final UserDTO user = event.getUser();
		String displayName = user.getName();
		String email = user.getEmail();

		List<String> requiredUrls = event.getAppUrlList();
		final String confirmationUrl = requiredUrls.get(0);

		String message = String.format(
				"You recently signed up with Cometbid. To confirm to us that the email address you provided is genuine,"
						+ "/n Please click on %s or copy it to a Browsers Address bar and press Return key.",
				confirmationUrl);

		EmailValidationDto emailMessage = EmailMessageFactory.buildEmailVerificationMessage(email, displayName, message,
				confirmationUrl);

		emailClient.publishSignUpEvent(emailMessage, EmailType.EMAIL_VERIFICATION);
	}

	private void confirmPasswordResetRequest(final CustomUserAuthActionEvent event) {
		final UserDTO user = event.getUser();

		String displayName = user.getName();
		String email = user.getEmail();
		String message = String.format("You requested to reset your password from this location %s."
				+ "/n Please copy or type this code into the Auth code prompt %s to proceed and reset your password. "
				+ "If it's not you, this may have reached your email in error, please ignore this message.",
				user.getLocation(), user.getToken());

		EmailValidationDto emailMessage = EmailMessageFactory.buildUserPasswordResetEmailMessage(email, displayName,
				message);

		emailClient.publishSignUpEvent(emailMessage, EmailType.RESET_PASSWORD_REQUEST);
	}

	private void sendUserPasswordResetCompletedNotification(final CustomUserAuthActionEvent event) {
		final UserDTO user = event.getUser();

		String displayName = user.getName();
		String email = user.getEmail();
		String message = String.format("Your password has been changed from this location %s."
				+ "/n You can proceed to login with the new password. " + "If this action was initiated by you, "
				+ "please change your email and profile password immediately to avoid security breach of your account.");

		EmailValidationDto emailMessage = EmailMessageFactory.buildUserPasswordResetCompleteEmailMessage(email,
				displayName, message);

		emailClient.publishSignUpEvent(emailMessage, EmailType.RESET_PASSWORD_COMPLETED);
	}

	private void sendUsernameRequestConfirmation(final CustomUserAuthActionEvent event) {
		final UserDTO user = event.getUser();
		String email = user.getEmail();
		String username = user.getUsername();

		String message = String.format("Your username is %s", user.getUsername());

		EmailValidationDto emailMessage = EmailMessageFactory.buildUsernameRequestEmailMessage(username, email,
				message);

		emailClient.publishSignUpEvent(emailMessage, EmailType.USERNAME_REQUEST);
	}

	/**
	private void confirmUserRegistration(final CustomUserAuthActionEvent event) {
		final UserDTO user = event.getUser();

		String branchCode = user.getBranchCode();
		String rcNo = user.getRcNo();

		emailClient.sendEmailMessage(
				EmailMessageFactory.buildUserCreatedEmailMessage(rcNo, branchCode, user.getUsername(), user.getEmail(),
						user.getFirstName(), user.getLastName(), user.getUserId(), "", null),
				EmailType.USER_PROFILE_CREATION);
	}

	private void confirmMemberRegistration(final CustomUserAuthActionEvent event) {
		final UserDTO user = event.getUser();

		String branchCode = user.getBranchCode();
		String rcNo = user.getRcNo();

		emailClient.sendEmailMessage(
				EmailMessageFactory.buildUserCreatedEmailMessage(rcNo, branchCode, user.getUsername(), user.getEmail(),
						user.getFirstName(), user.getLastName(), user.getUserId(), "", null),
				EmailType.STAFF_PROFILE_CREATION);
	}

	private void confirmMemberRegistrationRequest(final CustomUserAuthActionEvent event) {
		final UserDTO user = event.getUser();

		String branchCode = user.getBranchCode();
		String rcNo = user.getRcNo();

		emailClient.sendEmailMessage(EmailMessageFactory.buildMemberRegEmailMessage(rcNo, branchCode, user.getEmail()),
				EmailType.MEMBER_REGISTRATION_REQUEST);
	}
	
	@Async
	@EventListener
	public void handleMerchantRegisteredEvent(final OnMerchantRegistrationCompleteEvent event) {
		this.confirmMerchantRegistration(event);
	}

	private void confirmMerchantRegistration(final OnMerchantRegistrationCompleteEvent event) {
		final BranchDTO branch = event.getBranch();

		String message = String.format("Your Subscription %s", branch.getSubscription());

		emailClient.sendEmailMessage(EmailMessageFactory.buildMerchantCreatedEmailMessage(branch.getRcNo(),
				branch.getBranchCode(), branch.getName(), branch.getEmail(), message),
				EmailType.MERCHANT_PROFILE_CREATION);
	}
	**/
}
