/**
 * 
 */
package com.keycloak.admin.client.integration.messaging;

import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

/**
 * @author Gbenga
 *
 */
@Data
@Builder
public class SmsValidationDto {

	@JsonProperty("url")
	private String confirmationUrl;

	@JsonProperty("phone_no")
	private String msisdn;

	@JsonProperty("display_name")
	private String name;

	@JsonProperty("content")
	private String content;

	@NotBlank(message = "{message.refNo.notBlank}")
	@JsonProperty("ref_no")
	private String refNo;

}
