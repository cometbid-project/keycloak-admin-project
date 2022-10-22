/**
 * 
 */
package com.keycloak.admin.client.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;

/**
 * @author Gbenga
 *
 */
@Value
public class EnableTwoFactorAuthResponse {

	@JsonProperty("message")
	private String message;

	@JsonProperty("qrCode_image")
	private String qrCodeImage;

	/**
	 * @param message
	 * @param qrCodeImage
	 */
	public EnableTwoFactorAuthResponse(String message, String qrCodeImage) {
		this.message = message;
		this.qrCodeImage = qrCodeImage;
	}

	/**
	 * @param message
	 * @param qrCodeImage
	 */
	public EnableTwoFactorAuthResponse() {
		this(null, null);
	}

}
