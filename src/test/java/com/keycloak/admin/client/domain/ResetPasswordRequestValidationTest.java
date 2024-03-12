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

import com.github.javafaker.Faker;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.models.ResetPasswordRequest;
import com.keycloak.admin.client.models.StatusUpdateRequest;
import com.keycloak.admin.client.models.UserVO;

/**
 * @author Gbenga
 *
 */
class ResetPasswordRequestValidationTest {

	private static Validator validator;

	@BeforeAll
	static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void whenAllFieldsCorrectThenValidationSucceeds() {
		var req = UserBuilder.user().buildPasswordResetRequest();

		Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(req);
		assertThat(violations).isEmpty();
	}

	@Test
	void whenEmailIsNotDefinedValidationFails() {
		var req = UserBuilder.user().withEmail(null).buildPasswordResetRequest();

		Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(req);
		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Your email is required to reset password");

		// ==================================================================================================

		req = UserBuilder.user().withEmail("").buildPasswordResetRequest();
		violations = validator.validate(req);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Your email is required to reset password");
	}

	@Test
	void whenEmailDefinedButTooLongStringThenValidationFails() {
		var req = UserBuilder.user().withEmail(Faker.instance().random().hex(51)).buildPasswordResetRequest();

		Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(req);
		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage())
				.isEqualTo("Email specified is not a properly formatted email address");
	}

	@Test
	void whenEmailDefinedWithInvalidFormatThenValidationFails() {
		var req = UserBuilder.user().withEmail(Faker.instance().internet().domainName()).buildPasswordResetRequest();

		Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(req);
		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage())
				.isEqualTo("Email specified is not a properly formatted email address");
	}
}
