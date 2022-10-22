/**
 * 
 */
package com.keycloak.admin.client.integration.messaging;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Gbenga
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMessage {

	@JsonProperty("user_id")
	private String userIdentityNo;

	@JsonProperty("mgr_id")
	private String mgrIdentityNo;

	@JsonProperty("branch_code")
	private String branchCode;

	@JsonProperty("rc_no")
	private String rcNo;

	@NotBlank(message = "{message.email.notBlank}")
	@Email(message = "{message.email.valid}")
	@JsonProperty("email_addr")
	private String emailAddr;

	@JsonProperty("username")
	private String username;

	@JsonProperty("acct_no")
	private String acctNo;

	@JsonProperty("content")
	private String content;

	@NotBlank(message = "{message.refNo.notBlank}")
	@JsonProperty("ref_no")
	private String refNo;

}
