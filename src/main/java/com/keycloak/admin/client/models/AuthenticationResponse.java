package com.keycloak.admin.client.models;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Setter;
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
public class AuthenticationResponse implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 9136201574728380851L;

	@JsonProperty("username")
	private String username;

	@JsonProperty("roles")
	private List<String> roles;

	@JsonProperty("secret")
	private String secret;

	@JsonProperty("access_token")
	private String accessToken;

	@JsonProperty("refresh_token")
	private String refreshToken;

	@JsonProperty("expires_in")
	private Long expiresIn;

	@JsonIgnore
	private String otpCode;

	// in Seconds
	@JsonProperty("refresh_expires_in")
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
