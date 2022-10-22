/**
 * 
 */
package com.keycloak.admin.client.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Gbenga
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "Totp", description = "Totp request model")
public class TotpRequest {

	@JsonProperty("totp_code")
	@Schema(name = "totp_code", description = "Totp code to validate")
	private String totpCode;
	
	@JsonProperty("totp_session_id")
	private String totpSessionId;
}
