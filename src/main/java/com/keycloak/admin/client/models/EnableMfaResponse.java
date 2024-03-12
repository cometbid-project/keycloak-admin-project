/**
 * 
 */
package com.keycloak.admin.client.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.keycloak.admin.client.validators.qualifiers.ValidEmail;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * @author Gbenga
 *
 */
@Value
@Builder
@Schema(name = "Enable MFA", description = "Enable Mfa response model")
public class EnableMfaResponse {

	@JsonProperty("message")
	@Schema(name = "Response message", description = "Response message")
	private String message;

	@JsonProperty("qrCode")
	@Schema(name = "QrCode", description = "Qrcode to be presented to user who initiated the request")
	private String qrCodeImage;

	/**
	 * @param message
	 * @param qrCodeImage
	 */
	@JsonCreator
	public EnableMfaResponse(String message, String qrCodeImage) {
		this.message = message;
		this.qrCodeImage = qrCodeImage;
	}

	/**
	 * @param message
	 * @param qrCodeImage
	 */
	private EnableMfaResponse() {
		this(null, null);
	}

}
