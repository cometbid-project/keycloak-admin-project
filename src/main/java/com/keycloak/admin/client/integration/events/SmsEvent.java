/**
 * 
 */
package com.keycloak.admin.client.integration.events;

import com.keycloak.admin.client.integration.enums.SMSType;
import com.keycloak.admin.client.integration.messaging.SmsValidationDto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Gbenga
 *
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SmsEvent extends Event<String, SmsValidationDto, SMSType> {

	public SmsEvent(SmsValidationDto smsValidationRequestDto, String eventId, SMSType smsType) {
		super(smsType, eventId, smsValidationRequestDto);
	}

	@Override
	public String getEventId() {
		return super.getKey();
	}
}
