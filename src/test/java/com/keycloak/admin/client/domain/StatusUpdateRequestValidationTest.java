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
import com.keycloak.admin.client.models.StatusUpdateRequest;
import com.keycloak.admin.client.models.UserDetailsUpdateRequest;

/**
 * @author Gbenga
 *
 */
class StatusUpdateRequestValidationTest {

	private static Validator validator;

	@BeforeAll
	static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void whenAllFieldsCorrectThenValidationSucceeds() {
		var user = UserBuilder.user().buildStatusUpdate();

		Set<ConstraintViolation<StatusUpdateRequest>> violations = validator.validate(user);

		assertThat(violations).isEmpty();
	}

	@Test
	void whenUsernameNotDefinedOrBlankThenValidationFails() {
		var user = UserBuilder.user().withUsername(null).buildStatusUpdate();

		Set<ConstraintViolation<StatusUpdateRequest>> violations = validator.validate(user);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Username must be specified");

		// ==================================================================================================

		user = UserBuilder.user().withUsername("").buildStatusUpdate();
		violations = validator.validate(user);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Username must be specified");
	}

	@Test
	void whenUsernameDefinedButTooLongStringThenValidationFails() {
		var user = UserBuilder.user().withUsername(Faker.instance().random().hex(52)).buildStatusUpdate();
		Set<ConstraintViolation<StatusUpdateRequest>> violations = validator.validate(user);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Username character length must not exceed 50");
	}

	@Test
	void whenStatusNotDefinedOrBlankThenValidationFails() {
		var user = UserBuilder.user().withStatus(null).buildStatusUpdate();

		Set<ConstraintViolation<StatusUpdateRequest>> violations = validator.validate(user);

		assertThat(violations).hasSize(2);

		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());
		assertThat(constraintViolationMessages)
				.contains("Status must be specified(possible values are ACTIVE, EXPIRE, LOCK, DISABLE)")
				.contains("Status value specified is not valid(acceptable values are ACTIVE, EXPIRE, LOCK, DISABLE)");
	}

	@Test
	void whenStatusDefinedWithInvalidStatusTypeThenValidationFails() {
		var user = UserBuilder.user().withStatus("UNKNOWN").buildStatusUpdate();

		Set<ConstraintViolation<StatusUpdateRequest>> violations = validator.validate(user);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage())
				.isEqualTo("Status value specified is not valid(acceptable values are ACTIVE, EXPIRE, LOCK, DISABLE)");

	}
}
