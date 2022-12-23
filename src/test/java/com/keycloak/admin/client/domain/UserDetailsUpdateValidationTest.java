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
import com.keycloak.admin.client.models.UserDetailsUpdateRequest;
import com.keycloak.admin.client.models.UserRegistrationRequest;

/**
 * @author Gbenga
 *
 */
class UserDetailsUpdateValidationTest {

	private static Validator validator;

	@BeforeAll
	static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}
	
	@Test
	void whenAllFieldsCorrectThenValidationSucceeds() {
		var userReg = UserBuilder.user().buildUpdate();
		Set<ConstraintViolation<UserDetailsUpdateRequest>> violations = validator.validate(userReg);

		assertThat(violations).isEmpty();
	}

	@Test
	void whenFirstNameNotDefinedOrBlankThenValidationFails() {
		var userReg = UserBuilder.user().withFirstName("").buildUpdate();
		Set<ConstraintViolation<UserDetailsUpdateRequest>> violations = validator.validate(userReg);

		assertThat(violations).hasSize(2);

		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());
		assertThat(constraintViolationMessages).contains("Firstname character length must not exceed 50")
				.contains("Firstname must be specified");

		// ==================================================================================================

		userReg = UserBuilder.user().withFirstName(null).buildUpdate();
		violations = validator.validate(userReg);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Firstname must be specified");
	}

	@Test
	void whenFirstNameDefinedButTooLongStringThenValidationFails() {
		var userReg = UserBuilder.user().withFirstName(Faker.instance().random().hex(52)).buildUpdate();
		Set<ConstraintViolation<UserDetailsUpdateRequest>> violations = validator.validate(userReg);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage())
				.isEqualTo("Firstname character length must not exceed 50");
	}

	@Test
	void whenLastNameNotDefinedOrBlankThenValidationFails() {
		var userReg = UserBuilder.user().withLastName("").buildUpdate();
		Set<ConstraintViolation<UserDetailsUpdateRequest>> violations = validator.validate(userReg);

		assertThat(violations).hasSize(2);

		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());
		assertThat(constraintViolationMessages).contains("Lastname character length must not exceed 50")
				.contains("Lastname must be specified");

		// ==================================================================================================

		userReg = UserBuilder.user().withLastName(null).buildUpdate();
		violations = validator.validate(userReg);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Lastname must be specified");
	}

	@Test
	void whenLastNameDefinedButTooLongStringThenValidationFails() {
		var userReg = UserBuilder.user().withLastName(Faker.instance().random().hex(52)).buildUpdate();
		Set<ConstraintViolation<UserDetailsUpdateRequest>> violations = validator.validate(userReg);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Lastname character length must not exceed 50");
	}
}
