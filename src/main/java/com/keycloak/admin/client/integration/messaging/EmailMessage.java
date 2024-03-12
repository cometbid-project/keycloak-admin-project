/**
 * 
 */
package com.keycloak.admin.client.integration.messaging;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Gbenga
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage {

	@JsonProperty("user_id")
	private String userIdentityNo;

	@JsonProperty("mgr_id")
	private String mgrIdentityNo;

	@JsonProperty("url")
	private String confirmationUrl;

	@JsonProperty("branch_code")
	private String branchCode;

	@JsonProperty("rc_no")
	private String rcNo;

	@NotBlank(message = "{message.email.notBlank}")
	@Email(message = "{message.email.valid}")
	@JsonProperty("email_addr")
	private String emailAddr;

	@JsonProperty("display_name")
	private String name;

	@NotBlank(message = "{message.businessName.notBlank}")
	@JsonProperty("business_name")
	private String businessName;

	@JsonProperty("content")
	private String content;

	@NotBlank(message = "{message.refNo.notBlank}")
	@JsonProperty("ref_no")
	private String refNo;

	private String title;

}
