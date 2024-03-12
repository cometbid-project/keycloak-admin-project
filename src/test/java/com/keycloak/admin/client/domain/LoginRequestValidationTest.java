/**
 * 
 */
package com.keycloak.admin.client.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.javafaker.Faker;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.models.AuthenticationRequest;

/**
 * @author Gbenga
 *
 */
class LoginRequestValidationTest {

	private static Validator validator;

	@BeforeAll
	static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void whenAllFieldsCorrectThenValidationSucceeds() {
		var req = UserBuilder.user().loginRequest();
		
		Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(req); 
		assertThat(violations).isEmpty();
	}

	@Test
	void whenUsernameNotDefinedOrBlankThenValidationFails() {
		var req = UserBuilder.user().withUsername(null).loginRequest();

		Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(req); 
		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage())
				.isEqualTo("Username must be specified");

		// ======================================================================================
		req = UserBuilder.user().withUsername("").loginRequest();

		violations = validator.validate(req);
		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage())
				.isEqualTo("Username must be specified");
	}
	
	@Test
	void whenUsernameDefinedButTooLongStringThenValidationFails() {
		var userVo = UserBuilder.user().withUsername(Faker.instance().random().hex(331)).loginRequest();

		Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(userVo); 
		assertThat(violations).hasSize(1);

		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());

		assertThat(constraintViolationMessages).contains("Username character length must not exceed 50");
	}


	@Test
	void whenPasswordIsNotDefinedValidationSucceeds() {
		var req = UserBuilder.user().withPassword(null).loginRequest();

		Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(req);
		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Your current password is required");

		// =======================================================================================================

		req = UserBuilder.user().withPassword("").loginRequest();
		violations = validator.validate(req); 

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Your current password is required");
	}
}
