/**
 * 
 */
package com.keycloak.admin.client.models;

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

/**
 * @author Gbenga
 *
 */
@Value
//@AllArgsConstructor
//@NoArgsConstructor
@Schema(name = "Totp", description = "Totp request model")
public class TotpRequest {

	@JsonProperty("totp_code")
	@Schema(name = "totp_code", description = "Totp code to validate")
	@NotBlank(message = "{totpCode.notBlank}")
	private String totpCode;

	@JsonProperty("totp_session_id")
	@NotBlank(message = "{totpSession.notBlank}")
	@Schema(name = "Totp Session", description = "Totp session token assigned to manage mfa authentication")
	private String totpSessionId;

	/**
	 * 
	 */
	public TotpRequest() {
		this(null, null);
	}

	/**
	 * @param totpCode
	 * @param totpSessionId
	 */
	//@JsonCreator
	public TotpRequest(@NotBlank(message = "{totpCode.notBlank}") String totpCode,
			@NotBlank(message = "{totpSession.notBlank}") String totpSessionId) {
		super();
		this.totpCode = totpCode;
		this.totpSessionId = totpSessionId;
	}

}
