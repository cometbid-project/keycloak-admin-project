/**
 * 
 */
package com.keycloak.admin.client.integration.messaging;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.keycloak.admin.client.common.utils.LocaleContextUtils;

import lombok.Builder;
import lombok.Data;

/**
 * @author Gbenga
 *
 */
@Data
@Builder
public class EmailValidationDto {

	@JsonProperty("url")
	private String confirmationUrl;

	@NotBlank(message = "{message.email.notBlank}")
	@Email(message = "{message.email.valid}")
	@JsonProperty("email_addr")
	private String emailAddr;

	@JsonProperty("display_name")
	private String name;

	@JsonProperty("content")
	private String content;
	
	@Builder.Default
	@JsonProperty("locale")
	private String preferredLocale = LocaleContextUtils.getContextLocaleAsString();

	@NotBlank(message = "{message.refNo.notBlank}")
	@JsonProperty("ref_no")
	private String refNo;

}
