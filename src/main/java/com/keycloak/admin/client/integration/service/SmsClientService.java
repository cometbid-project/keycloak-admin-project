/**
 * 
 */
package com.keycloak.admin.client.integration.service;


import com.keycloak.admin.client.integration.enums.SMSType;
import com.keycloak.admin.client.integration.messaging.SmsValidationDto;

import reactor.core.publisher.Mono;
// import reactor.kafka.sender.SenderResult;

/**
 * @author Gbenga
 *
 */
public interface SmsClientService {

	/**
	 * 
	 */
	void publishSignUpEvent(SmsValidationDto smsValidationRequestDto, SMSType smsType);

}
