/**
 * 
 */
package com.keycloak.admin.client.integration.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keycloak.admin.client.integration.enums.EmailType;
import com.keycloak.admin.client.integration.events.EmailEvent;
import com.keycloak.admin.client.integration.messaging.EmailValidationDto;
import com.keycloak.admin.client.integration.service.EmailClientService;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Sinks;

/**
 * @author Gbenga
 * 
 */
@Log4j2
@Service("emailClient")
public class EmailClientServiceImpl implements EmailClientService {

	@Autowired
	private Sinks.Many<EmailEvent> signUpSinks;

	/**
	 * 
	 */
	@Override
	public void publishSignUpEvent(EmailValidationDto emailValidationRequestDto, EmailType emailType) {
		EmailEvent signupEvent = new EmailEvent(emailValidationRequestDto, emailValidationRequestDto.getRefNo(),
				EmailType.EMAIL_VERIFICATION);
		signUpSinks.tryEmitNext(signupEvent);
	}

}
