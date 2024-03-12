/**
 * 
 */
package com.keycloak.admin.client.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.keycloak.admin.client.dataacess.AuthBuilder;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.models.TotpRequest;

/**
 * @author Gbenga
 *
 */
class TotpRequestValidationTest {

	private static Validator validator;

	@BeforeAll
	static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void whenAllFieldsCorrectThenValidationSucceeds() {
		var user = UserBuilder.user().userVo();
		var totp = AuthBuilder.auth(user).buildTotpRequest(false, false);
		
		Set<ConstraintViolation<TotpRequest>> violations = validator.validate(totp);

		assertThat(violations).isEmpty();
	}

	@Test
	void whenTotpSessionNotDefinedOrBlankThenValidationFails() {
		var user = UserBuilder.user().userVo();
		var totp = AuthBuilder.auth(user).buildTotpRequest(false, true);
		
		Set<ConstraintViolation<TotpRequest>> violations = validator.validate(totp);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Totp session id must be specified");
	}

	@Test
	void whenTotpCodeNotDefinedOrBlankThenValidationFails() {
		var user = UserBuilder.user().userVo();
		var totp = AuthBuilder.auth(user).buildTotpRequest(true, false);
		
		Set<ConstraintViolation<TotpRequest>> violations = validator.validate(totp);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Totp code must be specified");
	}

}
