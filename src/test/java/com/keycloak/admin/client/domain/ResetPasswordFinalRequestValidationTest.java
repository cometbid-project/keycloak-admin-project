/**
 * 
 */
package com.keycloak.admin.client.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.javafaker.Faker;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.models.ResetPasswordFinalRequest;

/**
 * @author Gbenga
 *
 */
class ResetPasswordFinalRequestValidationTest {

	private static Validator validator;

	@BeforeAll
	static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void whenAllFieldsCorrectThenValidationSucceeds() {
		var req = UserBuilder.user().buildFinalPasswordResetRequest(UUID.randomUUID().toString());

		Set<ConstraintViolation<ResetPasswordFinalRequest>> violations = validator.validate(req);
		assertThat(violations).isEmpty();
	}

	@Test
	void whenUsernameNotDefinedOrBlankThenValidationFails() {
		var user = UserBuilder.user().withUsername(null).buildFinalPasswordResetRequest(UUID.randomUUID().toString());

		Set<ConstraintViolation<ResetPasswordFinalRequest>> violations = validator.validate(user);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Username must be specified");

		// ==================================================================================================

		user = UserBuilder.user().withUsername("").buildFinalPasswordResetRequest(UUID.randomUUID().toString());
		violations = validator.validate(user);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Username must be specified");
	}

	@Test
	void whenUsernameDefinedButTooLongStringThenValidationFails() {
		var user = UserBuilder.user().withUsername(Faker.instance().random().hex(52))
				.buildFinalPasswordResetRequest(UUID.randomUUID().toString());
		Set<ConstraintViolation<ResetPasswordFinalRequest>> violations = validator.validate(user);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Username character length must not exceed 50");
	}

	@Test
	void whenPasswordIsNotDefinedValidationFails() {
		var userReg = UserBuilder.user().withPassword(null)
				.buildFinalPasswordResetRequest(UUID.randomUUID().toString());

		Set<ConstraintViolation<ResetPasswordFinalRequest>> violations = validator.validate(userReg);
		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Please specify your new password");

		// =======================================================================================================

		userReg = UserBuilder.user().withPassword("").buildFinalPasswordResetRequest(UUID.randomUUID().toString());
		violations = validator.validate(userReg);

		assertThat(violations).hasSize(2);

		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());
		assertThat(constraintViolationMessages)
				.contains("Password character length must be atleast 8 and not exceed 50")
				.contains("Please specify your new password");
	}

	@Test
	void whenPasswordDefinedButTooShortOrTooLongStringThenValidationFails() {
		var userReg = UserBuilder.user().withPassword(Faker.instance().random().hex(51))
				.buildFinalPasswordResetRequest(UUID.randomUUID().toString());

		Set<ConstraintViolation<ResetPasswordFinalRequest>> violations = validator.validate(userReg);
		assertThat(violations).hasSize(2);

		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());
		assertThat(constraintViolationMessages)
				.contains("Password character length must be atleast 8 and not exceed 50").contains(
						"Password specified does not meet requirement to be acceptable, must have atleast(1 lowercase char, 1 uppercase char, 1 special character in(@#$&_), and a digit)");

		// =======================================================================================================

		userReg = UserBuilder.user().withPassword(Faker.instance().random().hex(6))
				.buildFinalPasswordResetRequest(UUID.randomUUID().toString());
		violations = validator.validate(userReg);

		assertThat(violations).hasSize(2);

		constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());
		assertThat(constraintViolationMessages)
				.contains("Password character length must be atleast 8 and not exceed 50").contains(
						"Password specified does not meet requirement to be acceptable, must have atleast(1 lowercase char, 1 uppercase char, 1 special character in(@#$&_), and a digit)");
	}

	@Test
	void whenPasswordDefinedDoesnotMeetRequirementThenValidationFails() {
		var userReg = UserBuilder.user().withPassword(Faker.instance().internet().password())
				.buildFinalPasswordResetRequest(UUID.randomUUID().toString());

		Set<ConstraintViolation<ResetPasswordFinalRequest>> violations = validator.validate(userReg);
		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo(
				"Password specified does not meet requirement to be acceptable, must have atleast(1 lowercase char, 1 uppercase char, 1 special character in(@#$&_), and a digit)");
	}

	@Test
	void whenPasswordResetSessionNotDefinedOrBlankThenValidationFails() {
		var req = UserBuilder.user().buildFinalPasswordResetRequest(null);

		Set<ConstraintViolation<ResetPasswordFinalRequest>> violations = validator.validate(req);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Reset password session id must be specified");

		// ===============================================================================================================

		req = UserBuilder.user().buildFinalPasswordResetRequest("");

		violations = validator.validate(req);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Reset password session id must be specified");
	}

}
