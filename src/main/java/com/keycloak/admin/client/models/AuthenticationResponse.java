package com.keycloak.admin.client.models;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 *
 * @author Gbenga
 *
 */
@Value
//@Builder
@Accessors(chain = true)
@Schema(name = "Login response", description = "Authentication response which includes the JWT access token")
public class AuthenticationResponse implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 9136201574728380851L;

	@JsonProperty("username")
	@Schema(name = "Username(Email)", description = "Username of the authenticated user")
	private String username;

	@JsonProperty("roles")
	@Schema(name = "Roles", description = "Role(s) of the authenticated user")
	private List<String> roles;

	@JsonProperty("secret")
	@Schema(name = "Secret key", description = "Access token secret key")
	private String secret;

	@JsonProperty("access_token")
	@Schema(name = "Access token", description = "login password")
	private String accessToken;

	@JsonProperty("refresh_token")
	@Schema(name = "Refresh token", description = "Refresh token")
	private String refreshToken;

	@JsonProperty("expires_in")
	@Schema(name = "Access token expiry", description = "Access token expiration period")
	private Long expiresIn;

	@JsonIgnore
	@Schema(name = "OTP Code", description = "Otp code, a substitute for Totp code")
	private String otpCode;

	// in Seconds
	@JsonProperty("refresh_expires_in")
	@Schema(name = "Refresh token expiry", description = "Refresh token expiration period")
	private Long refreshExpiresIn;

	public AuthenticationResponse() {

		this(null, null, null, null, null, null, null, null);
	}

	public AuthenticationResponse(String username, List<String> roles, String accessToken, String refreshToken) {

		this(username, roles, accessToken, refreshToken, null, null, null, null);
	}

	public AuthenticationResponse(String username, String totpSessionId) {

		this(username, null, null, null, null, null, null, totpSessionId);
	}

	@Builder
	@JsonCreator
	public AuthenticationResponse(String username, List<String> roles, String accessToken, String refreshToken,
			Long expiresIn, Long refreshExpiresIn, String secret, String otpCode) {

		this.roles = roles;
		this.otpCode = otpCode;
		this.expiresIn = expiresIn;
		this.refreshExpiresIn = refreshExpiresIn;
		this.secret = secret;
		this.username = username;
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
	}

	public static AuthenticationResponse cloneWithOtpCode(AuthenticationResponse authResponse, String otpCode) {

		return new AuthenticationResponse(authResponse.getUsername(), authResponse.getRoles(),
				authResponse.getAccessToken(), authResponse.getRefreshToken(), authResponse.getExpiresIn(),
				authResponse.getRefreshExpiresIn(), authResponse.getSecret(), otpCode);
	}

}
