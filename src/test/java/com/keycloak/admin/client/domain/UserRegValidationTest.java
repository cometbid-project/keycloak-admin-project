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
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.models.UserRegistrationRequest;
import com.keycloak.admin.client.models.UserVO;

/**
 * @author Gbenga
 *
 */
class UserRegValidationTest {

	private static Validator validator;

	@BeforeAll
	static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void whenAllFieldsCorrectThenValidationSucceeds() {
		var userReg = UserBuilder.user().build();
		Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(userReg);

		assertThat(violations).isEmpty();
	}

	@Test
	void whenFirstNameNotDefinedOrBlankThenValidationFails() {
		var userReg = UserBuilder.user().withFirstName("").build();
		Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(userReg);

		assertThat(violations).hasSize(2);

		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());
		assertThat(constraintViolationMessages).contains("Firstname character length must not exceed 50")
				.contains("Firstname must be specified");

		// ==================================================================================================

		userReg = UserBuilder.user().withFirstName(null).build();
		violations = validator.validate(userReg);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Firstname must be specified");
	}

	@Test
	void whenFirstNameDefinedButTooLongStringThenValidationFails() {
		var userReg = UserBuilder.user().withFirstName(Faker.instance().random().hex(52)).build();
		Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(userReg);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage())
				.isEqualTo("Firstname character length must not exceed 50");
	}

	@Test
	void whenLastNameNotDefinedOrBlankThenValidationFails() {
		var userReg = UserBuilder.user().withLastName("").build();
		Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(userReg);

		assertThat(violations).hasSize(2);

		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());
		assertThat(constraintViolationMessages).contains("Lastname character length must not exceed 50")
				.contains("Lastname must be specified");

		// ==================================================================================================

		userReg = UserBuilder.user().withLastName(null).build();
		violations = validator.validate(userReg);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Lastname must be specified");
	}

	@Test
	void whenLastNameDefinedButTooLongStringThenValidationFails() {
		var userReg = UserBuilder.user().withLastName(Faker.instance().random().hex(52)).build();
		Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(userReg);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Lastname character length must not exceed 50");
	}

	@Test
	void whenEmailIsNotDefinedValidationSucceeds() {
		var userReg = UserBuilder.user().withEmail(null).build();

		Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(userReg);
		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Email must be specified to register");

		// =======================================================================================

		userReg = UserBuilder.user().withEmail("").build();
		violations = validator.validate(userReg);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Email must be specified to register");
	}

	@Test
	void whenEmailDefinedButTooLongStringThenValidationFails() {
		var userReg = UserBuilder.user().withEmail(Faker.instance().random().hex(331)).build();

		Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(userReg);
		assertThat(violations).hasSize(2);

		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());
		assertThat(constraintViolationMessages).contains("Email character length must not exceed 50")
				.contains("Email specified is not a properly formatted email address");
	}

	@Test
	void whenEmailDefinedWithInvalidFormatThenValidationFails() {
		var userReg = UserBuilder.user().withEmail(Faker.instance().internet().domainName()).build();

		Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(userReg);
		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage())
				.isEqualTo("Email specified is not a properly formatted email address");
	}

	@Test
	void whenPasswordIsNotDefinedValidationSucceeds() {
		var userReg = UserBuilder.user().withPassword(null).build();

		Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(userReg);
		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Password must be specified");

		// =======================================================================================================

		userReg = UserBuilder.user().withPassword("").build();
		violations = validator.validate(userReg);

		assertThat(violations).hasSize(2);

		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());
		assertThat(constraintViolationMessages).contains("Password character length must be atleast 8 and not exceed 50")
				.contains("Password must be specified");
	}

	@Test
	void whenPasswordDefinedButTooShortOrTooLongStringThenValidationFails() {
		var userReg = UserBuilder.user().withPassword(Faker.instance().random().hex(51)).build();

		Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(userReg);
		assertThat(violations).hasSize(2);

		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());
		assertThat(constraintViolationMessages)
				.contains("Password character length must be atleast 8 and not exceed 50")
				.contains("Password specified does not meet requirement to be acceptable, must have atleast(1 lowercase char, 1 uppercase char, 1 special character in(@#$&_), and a digit)");

		// =======================================================================================================

		userReg = UserBuilder.user().withPassword(Faker.instance().random().hex(6)).build();
		violations = validator.validate(userReg);

		assertThat(violations).hasSize(2);

		constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());
		assertThat(constraintViolationMessages)
				.contains("Password character length must be atleast 8 and not exceed 50")
				.contains("Password specified does not meet requirement to be acceptable, must have atleast(1 lowercase char, 1 uppercase char, 1 special character in(@#$&_), and a digit)");
	}

	@Test
	void whenPasswordDefinedDoesnotMeetRequirementThenValidationFails() {
		var userReg = UserBuilder.user().withPassword(Faker.instance().internet().password()).build();

		Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(userReg);
		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo(
				"Password specified does not meet requirement to be acceptable, must have atleast(1 lowercase char, 1 uppercase char, 1 special character in(@#$&_), and a digit)");
	}
}
