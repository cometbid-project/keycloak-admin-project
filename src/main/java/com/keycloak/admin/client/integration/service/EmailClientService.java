/**
 * 
 */
package com.keycloak.admin.client.integration.service;

import com.keycloak.admin.client.integration.enums.EmailType;
import com.keycloak.admin.client.integration.messaging.EmailValidationDto;

/**
 * @author Gbenga
 *
 */
public interface EmailClientService {

	/**
	 * 
	 */
	void publishSignUpEvent(EmailValidationDto emailValidationRequestDto, EmailType emailType);

}
