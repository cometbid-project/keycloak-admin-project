/**
 * 
 */
package com.keycloak.admin.client.integration.messaging;

import javax.validation.constraints.NotBlank;

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
public class SMSMessage {

	@JsonProperty("user_id")
	private String userIdentityNo;

	@JsonProperty("mgr_id")
	private String mgrIdentityNo;

	@JsonProperty("branch_code")
	private String branchCode;

	@JsonProperty("rc_no")
	private String rcNo;

	@JsonProperty("phone_no")
	private String msisdn;

	@JsonProperty("username")
	private String username;

	@JsonProperty("display_name")
	private String name;

	@JsonProperty("content")
	private String content;

	@NotBlank(message = "{message.refNo.notBlank}")
	@JsonProperty("ref_no")
	private String refNo;
}
