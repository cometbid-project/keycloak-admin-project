/**
 * 
 */
package com.keycloak.admin.client.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.javafaker.Faker;
import com.keycloak.admin.client.common.utils.RandomGenerator;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.models.PasswordUpdateRequest;

/**
 * @author Gbenga
 *
 */
class PasswordUpdateRequestValidationTest {

	private static Validator validator;

	@BeforeAll
	static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void whenAllFieldsCorrectThenValidationSucceeds() {
		var req = UserBuilder.user().buildPasswordUpdateRequest(true, RandomGenerator.generateRandomPassword());

		Set<ConstraintViolation<PasswordUpdateRequest>> violations = validator.validate(req);
		assertThat(violations).isEmpty();
	}

	@Test
	void whenOldPasswordIsNotDefinedValidationFails() {
		var req = UserBuilder.user().withPassword(null).buildPasswordUpdateRequest(true,
				RandomGenerator.generateRandomPassword());

		Set<ConstraintViolation<PasswordUpdateRequest>> violations = validator.validate(req);
		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Your current password is required");

		// =======================================================================================================

		req = UserBuilder.user().withPassword("").buildPasswordUpdateRequest(true,
				RandomGenerator.generateRandomPassword());
		violations = validator.validate(req);

		assertThat(violations).hasSize(2);

		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());
		assertThat(constraintViolationMessages)
				.contains("Password character length must be atleast 8 and not exceed 50")
				.contains("Your current password is required");
	}

	@Test
	void whenOldPasswordDefinedButTooShortOrTooLongStringThenValidationFails() {
		var req = UserBuilder.user().withPassword(Faker.instance().random().hex(51)).buildPasswordUpdateRequest(true,
				RandomGenerator.generateRandomPassword());

		Set<ConstraintViolation<PasswordUpdateRequest>> violations = validator.validate(req);
		assertThat(violations).hasSize(2);

		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());
		assertThat(constraintViolationMessages)
				.contains("Password character length must be atleast 8 and not exceed 50").contains(
						"Password specified does not meet requirement to be acceptable, must have atleast(1 lowercase char, 1 uppercase char, 1 special character in(@#$&_), and a digit)");

		// =======================================================================================================

		req = UserBuilder.user().withPassword(Faker.instance().random().hex(6)).buildPasswordUpdateRequest(true,
				RandomGenerator.generateRandomPassword());
		violations = validator.validate(req);

		assertThat(violations).hasSize(2);

		constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());
		assertThat(constraintViolationMessages)
				.contains("Password character length must be atleast 8 and not exceed 50").contains(
						"Password specified does not meet requirement to be acceptable, must have atleast(1 lowercase char, 1 uppercase char, 1 special character in(@#$&_), and a digit)");
	}

	@Test
	void whenOldPasswordDefinedDoesnotMeetRequirementThenValidationFails() {
		var req = UserBuilder.user().withPassword(Faker.instance().internet().password())
				.buildPasswordUpdateRequest(true, RandomGenerator.generateRandomPassword());

		Set<ConstraintViolation<PasswordUpdateRequest>> violations = validator.validate(req);
		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo(
				"Password specified does not meet requirement to be acceptable, must have atleast(1 lowercase char, 1 uppercase char, 1 special character in(@#$&_), and a digit)");
	}

	@Test
	void whenNewPasswordIsNotDefinedValidationFails() {
		var req = UserBuilder.user().buildPasswordUpdateRequest(false, null);

		Set<ConstraintViolation<PasswordUpdateRequest>> violations = validator.validate(req);
		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Please specify your new password");

		// =======================================================================================================

		req = UserBuilder.user().buildPasswordUpdateRequest(true, "");
		violations = validator.validate(req);

		assertThat(violations).hasSize(2);

		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());
		assertThat(constraintViolationMessages)
				.contains("Password character length must be atleast 8 and not exceed 50")
				.contains("Please specify your new password");
	}

	@Test
	void whenNewPasswordDefinedButTooShortOrTooLongStringThenValidationFails() {
		var req = UserBuilder.user().buildPasswordUpdateRequest(true, Faker.instance().random().hex(51));

		Set<ConstraintViolation<PasswordUpdateRequest>> violations = validator.validate(req);
		assertThat(violations).hasSize(2);

		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());
		assertThat(constraintViolationMessages)
				.contains("Password character length must be atleast 8 and not exceed 50").contains(
						"Password specified does not meet requirement to be acceptable, must have atleast(1 lowercase char, 1 uppercase char, 1 special character in(@#$&_), and a digit)");

		// =======================================================================================================

		req = UserBuilder.user().buildPasswordUpdateRequest(true, Faker.instance().random().hex(6));
		violations = validator.validate(req);

		assertThat(violations).hasSize(2);

		constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());
		assertThat(constraintViolationMessages)
				.contains("Password character length must be atleast 8 and not exceed 50").contains(
						"Password specified does not meet requirement to be acceptable, must have atleast(1 lowercase char, 1 uppercase char, 1 special character in(@#$&_), and a digit)");
	}

	@Test
	void whenNewPasswordDefinedDoesnotMeetRequirementThenValidationFails() {
		var req = UserBuilder.user().buildPasswordUpdateRequest(true, Faker.instance().internet().password());

		Set<ConstraintViolation<PasswordUpdateRequest>> violations = validator.validate(req);
		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo(
				"Password specified does not meet requirement to be acceptable, must have atleast(1 lowercase char, 1 uppercase char, 1 special character in(@#$&_), and a digit)");
	}

}
