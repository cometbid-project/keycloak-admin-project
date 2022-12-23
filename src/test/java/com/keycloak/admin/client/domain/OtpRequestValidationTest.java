/**
 * 
 */
package com.keycloak.admin.client.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.keycloak.admin.client.dataacess.AuthBuilder;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.models.SendOtpRequest;
import com.keycloak.admin.client.models.TotpRequest;

/**
 * @author Gbenga
 *
 */
class OtpRequestValidationTest {

	private static Validator validator;

	@BeforeAll
	static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void whenAllFieldsCorrectThenValidationSucceeds() {
		var user = UserBuilder.user().userVo();
		var totp = AuthBuilder.auth(user).buildOtpRequest(false, false);

		Set<ConstraintViolation<SendOtpRequest>> violations = validator.validate(totp);

		assertThat(violations).isEmpty();
	}

	@Test
	void whenOtpSessionNotDefinedOrBlankThenValidationFails() {
		var user = UserBuilder.user().userVo();
		var totp = AuthBuilder.auth(user).buildOtpRequest(true, false);

		Set<ConstraintViolation<SendOtpRequest>> violations = validator.validate(totp);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Otp session id must be specified");
	}

	@Test
	void whenOtpCodeNotDefinedOrBlankThenValidationFails() {
		var user = UserBuilder.user().userVo();
		var totp = AuthBuilder.auth(user).buildOtpRequest(false, true);

		Set<ConstraintViolation<SendOtpRequest>> violations = validator.validate(totp);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage())
				.isEqualTo("Otp mode value specified is not valid(acceptable values are Email, Sms)");
	}

}
