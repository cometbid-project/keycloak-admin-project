/**
 * 
 */
package com.keycloak.admin.client.integration.service;

import com.keycloak.admin.client.integration.enums.SMSType;
import com.keycloak.admin.client.integration.events.Event;
import com.keycloak.admin.client.integration.messaging.SMSMessage;

/**
 * @author Gbenga
 *
 */
public interface SmsConsumerService {

	void receiveMessage(Event<String, SMSMessage, SMSType> event);

}
