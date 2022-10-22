/**
 * 
 */
package com.keycloak.admin.client.integration.events;

import com.keycloak.admin.client.integration.enums.EmailType;
import com.keycloak.admin.client.integration.messaging.EmailValidationDto;

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
public class EmailEvent extends Event<String, EmailValidationDto, EmailType> {

	public EmailEvent(EmailValidationDto emailValidationRequestDto, String eventId, EmailType emailType) {
		super(emailType, eventId, emailValidationRequestDto);
	}

	@Override
	public String getEventId() {
		return super.getKey();
	}

}
