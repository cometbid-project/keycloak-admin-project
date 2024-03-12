/**
 * 
 */
package com.keycloak.admin.client.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.models.PasswordResetTokenResponse;

/**
 * @author Gbenga
 *
 */
class PasswordResetTokenValidationTest {

	private static Validator validator;

	@BeforeAll
	static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void whenAllFieldsCorrectThenValidationSucceeds() {
		var req = UserBuilder.user().buildPasswordResetToken(UUID.randomUUID().toString());

		Set<ConstraintViolation<PasswordResetTokenResponse>> violations = validator.validate(req);
		assertThat(violations).isEmpty();
	}

	@Test
	void whenPasswordResetSessionNotDefinedOrBlankThenValidationFails() {
		var req = UserBuilder.user().buildPasswordResetToken(null);

		Set<ConstraintViolation<PasswordResetTokenResponse>> violations = validator.validate(req);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Reset password session id must be specified");

		// ===============================================================================================================

		req = UserBuilder.user().buildPasswordResetToken("");
		violations = validator.validate(req);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Reset password session id must be specified");
	}

}
