/**
 * 
 */
package com.keycloak.admin.client.integration.messaging.factory;

import com.keycloak.admin.client.common.utils.RandomGenerator;
import com.keycloak.admin.client.integration.messaging.EmailMessage;
import com.keycloak.admin.client.integration.messaging.EmailValidationDto;

/**
 * @author Gbenga
 *
 */
public class EmailMessageFactory {

	/**
	 * 
	 * @param merchantCriteria
	 * @param username
	 * @param email
	 * @return
	 */
	public static EmailValidationDto buildUsernameRequestEmailMessage(String username, String email,
			String messageToSend) {
		// Process the Event source by sending a message to Messaging Broker
		EmailValidationDto message = EmailValidationDto.builder().name(null).emailAddr(email).content(messageToSend)
				.refNo(RandomGenerator.generateUnique64LengthReferenceNo()).build();

		return message;
	}

	/**
	 * 
	 * @param merchantCriteria
	 * @param username
	 * @param email
	 * @param firstName
	 * @param lastName
	 * @param userId
	 * @param password
	 * @param mgrId
	 * @return
	 */
	public static EmailMessage buildUserCreatedEmailMessage(String rcNo, String branchCode, String username,
			String email, String firstName, String lastName, String userId, String messageToSend, String url) {
		// Process the Event source by sending a message to Messaging Broker

		EmailMessage message = EmailMessage.builder().name(lastName).emailAddr(email).confirmationUrl(url).rcNo(rcNo)
				.branchCode(branchCode).content(messageToSend)
				.refNo(RandomGenerator.generateUnique64LengthReferenceNo()).mgrIdentityNo(null).userIdentityNo(userId)
				.build();

		return message;
	}

	/**
	 * 
	 * @param criteriaModel
	 * @param merchantName
	 * @param merchantEmail
	 * @param subscriptionType
	 * @return
	 */
	public static EmailMessage buildMerchantCreatedEmailMessage(String rcNo, String branchCode, String merchantName,
			String merchantEmail, String messageToSend) {
		// TODO Auto-generated method stub

		EmailMessage message = EmailMessage.builder().name(null).emailAddr(merchantEmail).businessName(merchantName)
				.rcNo(rcNo).branchCode(branchCode).content(messageToSend)
				.refNo(RandomGenerator.generateUnique64LengthReferenceNo()).mgrIdentityNo(null).userIdentityNo(null)
				.build();

		return message;
	}

	/**
	 * 
	 * @param rcNo
	 * @param branchCode
	 * @param email
	 * @return
	 */
	public static EmailMessage buildMemberRegEmailMessage(String rcNo, String branchCode, String email) {
		// TODO Auto-generated method stub
		EmailMessage message = EmailMessage.builder().emailAddr(email).rcNo(rcNo).branchCode(branchCode)
				// .content(messageToSend)
				.refNo(RandomGenerator.generateUnique64LengthReferenceNo()).mgrIdentityNo(null)
				// .userIdentityNo(userId)
				.build();
		return message;
	}

	/**
	 * 
	 * @param rcNo
	 * @param branchCode
	 * @param username
	 * @param email
	 * @param messageToSend
	 * @return
	 */
	public static EmailValidationDto buildUserPasswordResetEmailMessage(String email, String displayName,
			String messageToSend) {
		// TODO Auto-generated method stub
		EmailValidationDto message = EmailValidationDto.builder().name(displayName).emailAddr(email)
				.content(messageToSend).refNo(RandomGenerator.generateUnique64LengthReferenceNo()).build();

		return message;
	}

	/**
	 * 
	 * @param rcNo
	 * @param branchCode
	 * @param username
	 * @param email
	 * @param messageToSend
	 * @return
	 */
	public static EmailValidationDto buildUnfamiliarLocationNotificationEmailMessage(String displayName, String email,
			String messageToSend) {
		// TODO Auto-generated method stub
		EmailValidationDto message = EmailValidationDto.builder().name(displayName).emailAddr(email)
				.content(messageToSend).refNo(RandomGenerator.generateUnique64LengthReferenceNo()).build();

		return message;
	}

	/**
	 * 
	 * @param email
	 * @param messageToSend
	 * @param confirmationUrl
	 * @return
	 */
	public static EmailValidationDto buildEmailVerificationMessage(String email, String displayName,
			String messageToSend, String confirmationUrl) {
		// TODO Auto-generated method stub
		EmailValidationDto message = EmailValidationDto.builder().name(displayName).emailAddr(email)
				.confirmationUrl(confirmationUrl).content(messageToSend)
				.refNo(RandomGenerator.generateUnique64LengthReferenceNo()).build();

		return message;
	}

	/**
	 * 
	 * @param email
	 * @param secret
	 * @param messageToSend
	 * @return
	 */
	public static EmailMessage build2FactorActivationMessage(String email, String secret, String messageToSend) {
		// TODO Auto-generated method stub
		EmailMessage message = EmailMessage.builder().name(null).emailAddr(email).content(messageToSend)
				.refNo(RandomGenerator.generateUnique64LengthReferenceNo()).build();

		return message;
	}

	/**
	 * 
	 * @param email
	 * @param displayName
	 * @param messageToSend
	 * @return
	 */
	public static EmailValidationDto buildUserPasswordResetCompleteEmailMessage(String email, String displayName,
			String messageToSend) {
		// TODO Auto-generated method stub
		EmailValidationDto message = EmailValidationDto.builder().name(displayName).emailAddr(email)
				.content(messageToSend).refNo(RandomGenerator.generateUnique64LengthReferenceNo()).build();

		return message;
	}

}
