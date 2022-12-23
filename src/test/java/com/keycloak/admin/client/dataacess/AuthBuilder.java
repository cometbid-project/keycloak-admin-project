/**
 * 
 */
package com.keycloak.admin.client.dataacess;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;

import com.github.javafaker.Faker;
import com.keycloak.admin.client.common.enums.SendModeType;
import com.keycloak.admin.client.common.enums.StatusType;
import com.keycloak.admin.client.common.utils.RandomGenerator;
import com.keycloak.admin.client.entities.ActivationToken;
import com.keycloak.admin.client.models.ActivationTokenModel;
import com.keycloak.admin.client.models.AuthenticationRequest;
import com.keycloak.admin.client.models.AuthenticationResponse;
import com.keycloak.admin.client.models.LogoutRequest;
import com.keycloak.admin.client.models.SendOtpRequest;
import com.keycloak.admin.client.models.TotpRequest;
import com.keycloak.admin.client.models.UserVO;

import lombok.Data;

/**
 * @author Gbenga
 *
 */
@Data
public class AuthBuilder {

	private String token = "{}";

	private String username;

	private Faker faker;

	private List<String> roles;

	private String secret;

	private String accessToken;

	private String refreshToken;

	private Long expiresIn;

	private String totpSessionId;

	private Long refreshExpiresIn;

	private AuthBuilder(UserVO user) {
		faker = new Faker();

		this.secret = faker.internet().password();
		this.totpSessionId = faker.internet().uuid();

		Calendar nowPlusMins = Calendar.getInstance();
		nowPlusMins.add(Calendar.MINUTE, 30);
		this.expiresIn = nowPlusMins.getTimeInMillis();

		Calendar nowPlusHrs = Calendar.getInstance();
		nowPlusHrs.add(Calendar.HOUR, 24);
		this.refreshExpiresIn = nowPlusHrs.getTimeInMillis();

		this.accessToken = JwtUtil.instance().generateToken(user);
		this.refreshToken = JwtUtil.instance().generateToken(user);

		this.username = user.getUsername();
		this.roles = List.copyOf(user.getRoles());
	}

	public static AuthBuilder auth(UserVO user) {
		return new AuthBuilder(user);
	}

	public AuthBuilder withUsername(String username) {
		this.username = username;
		return this;
	}

	public AuthBuilder withAccessToken(String token) {
		this.accessToken = token;
		return this;
	}

	public AuthBuilder withRefreshToken(String token) {
		this.refreshToken = token;
		return this;
	}

	public AuthBuilder withSecret(String secret) {
		this.secret = secret;
		return this;
	}

	public AuthBuilder withTotpSessionId(String totpSessionId) {
		this.totpSessionId = totpSessionId;
		return this;
	}

	public AuthBuilder withExpiresIn(Long expiresIn) {
		this.expiresIn = expiresIn;
		return this;
	}

	public AuthBuilder withRefreshExpiresIn(Long expiresIn) {
		this.refreshExpiresIn = expiresIn;
		return this;
	}

	public AuthBuilder withRoles(List<String> roles) {
		this.roles = roles;
		return this;
	}

	public String token() {
		return token;
	}

	public AuthenticationResponse authResponse() {

		return AuthenticationResponse.builder().username(this.username).roles(this.roles).accessToken(this.accessToken)
				.refreshToken(this.refreshToken).expiresIn(this.expiresIn).refreshExpiresIn(this.refreshExpiresIn)
				.secret(this.secret).build();
	}

	public AuthenticationRequest build() {
		String password = faker.internet().password();
		return new AuthenticationRequest(this.username, password);
	}

	public TotpRequest buildTotpRequest(boolean nullTotp, boolean nullTotpSession) {
		final String totpSessionId = nullTotpSession ? null : RandomGenerator.generateSessionId();
		String totpCode = nullTotp ? null : Faker.instance().number().digits(6); // 6 digit code
		return new TotpRequest(totpCode, totpSessionId);
	}

	public SendOtpRequest buildOtpRequest(boolean nullOtpSession, boolean nullMode) {
		final String totpSessionId = nullOtpSession ? null : RandomGenerator.generateSessionId();
		String otpMode = nullMode ? "UNKNOWN" : SendModeType.EMAIL.toString(); // 6 digit code
		return new SendOtpRequest(totpSessionId, otpMode);
	}

	public LogoutRequest logoutRequest() {
		return new LogoutRequest(this.refreshToken);
	}

	public ActivationTokenModel buildActivationToken() {
		return ActivationTokenModel.builder().username(this.username).token(this.accessToken).build();
	}

}
