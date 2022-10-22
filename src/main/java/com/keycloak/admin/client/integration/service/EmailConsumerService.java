/**
 * 
 */
package com.keycloak.admin.client.integration.service;

import com.keycloak.admin.client.integration.enums.EmailType;
import com.keycloak.admin.client.integration.events.Event;
import com.keycloak.admin.client.integration.messaging.EmailMessage;

/**
 * @author Gbenga
 *
 */
public interface EmailConsumerService {

	void receiveMessage(Event<String, EmailMessage, EmailType> event);
}
