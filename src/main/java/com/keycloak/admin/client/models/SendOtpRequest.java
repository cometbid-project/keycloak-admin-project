/**
 * 
 */
package com.keycloak.admin.client.models;

import jakarta.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.keycloak.admin.client.common.enums.SendModeType;
import com.keycloak.admin.client.validators.qualifiers.VerifyValue;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Gbenga
 *
 */
@Data
//@AllArgsConstructor
//@NoArgsConstructor
@Schema(name = "Otp", description = "Otp request model")
public class SendOtpRequest {

	@JsonProperty("otp_session_id")
	@NotBlank(message = "{otpSession.notBlank}")
	@Schema(name = "Otp Session", description = "Otp session token assigned to manage mfa authentication")
	private String otpSessionId;

	@JsonProperty("mode")
	@Schema(name = "mode", description = "Preferred mode of sending/receiving Otp code")
	@NotBlank(message = "{otp.mode.notBlank}")
	@VerifyValue(message = "{otp.mode.verifyValue}", value = SendModeType.class)
	private String mode;

	/**
	 * @param otpSessionId
	 * @param mode
	 */
	//@JsonCreator
	public SendOtpRequest(String otpSessionId, String mode) {
		super();
		this.otpSessionId = otpSessionId;
		this.mode = StringUtils.isBlank(mode) ? SendModeType.EMAIL.toString() : mode;
	}

	/**
	 * @param totpSessionId
	 * @param mode
	 */
	public SendOtpRequest(String otpSessionId) {
		super();
		this.otpSessionId = otpSessionId;
		this.mode = SendModeType.EMAIL.toString();
	}
	
	/**
	 * @param totpSessionId
	 * @param mode
	 */
	public SendOtpRequest() {
		this(null);
	}


}
