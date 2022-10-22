/**
 * 
 */
package com.keycloak.admin.client.integration.service.impl;

import org.springframework.stereotype.Service;

import com.keycloak.admin.client.integration.enums.SMSType;
import com.keycloak.admin.client.integration.events.SmsEvent;
import com.keycloak.admin.client.integration.messaging.SmsValidationDto;
import com.keycloak.admin.client.integration.service.SmsClientService;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Sinks;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Service("smsClient")
public class SmsClientServiceImpl implements SmsClientService {

	@Autowired
	private Sinks.Many<SmsEvent> signUpSinks;

	/**
	 * 
	 */
	@Override
	public void publishSignUpEvent(SmsValidationDto smsValidationRequestDto, SMSType smsType) {
		
		SmsEvent signupEvent = new SmsEvent(smsValidationRequestDto, smsValidationRequestDto.getRefNo(), smsType);
		signUpSinks.tryEmitNext(signupEvent);
	}

}
