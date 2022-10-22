/**
 * 
 */
package com.keycloak.admin.client.integration.messaging.factory;

import com.keycloak.admin.client.common.utils.RandomGenerator;
import com.keycloak.admin.client.integration.messaging.SMSMessage;

/**
 * @author Gbenga
 *
 */
public class SmsMessageFactory {

	public static SMSMessage createSmsMessage(String rcNo, String branchCode, String username, String msisdn) {
		// Process the Event source by sending a message to Messaging Broker
		SMSMessage message = SMSMessage.builder().msisdn(msisdn).content(username)
				.refNo(RandomGenerator.generateUniqueRefId()).build();

		return message;
	}

	public static SMSMessage createSmsMessage(String rcNo, String branchCode, String username, String msisdn,
			String firstName, String lastName, String userId, String password, String mgrId) {
		// Process the Event source by sending a message to Messaging Broker
		SMSMessage message = SMSMessage.builder().msisdn(msisdn).username(username).rcNo(rcNo).branchCode(branchCode)
				.content(password).refNo(RandomGenerator.generateUniqueRefId()).mgrIdentityNo(mgrId)
				.userIdentityNo(userId).build();

		return message;
	}

}
