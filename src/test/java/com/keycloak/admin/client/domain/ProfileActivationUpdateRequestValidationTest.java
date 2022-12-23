/**
 * 
 */
package com.keycloak.admin.client.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.UUID;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.javafaker.Faker;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.models.ProfileActivationUpdateRequest;

/**
 * @author Gbenga
 *
 */
class ProfileActivationUpdateRequestValidationTest {

	private static Validator validator;

	@BeforeAll
	static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void whenAllFieldsCorrectThenValidationSucceeds() {
		var req = UserBuilder.user().buildProfileActivationRequest();

		Set<ConstraintViolation<ProfileActivationUpdateRequest>> violations = validator.validate(req);
		assertThat(violations).isEmpty();
	}

	@Test
	void whenUsernameNotDefinedOrBlankThenValidationFails() {
		var user = UserBuilder.user().withUsername(null).buildProfileActivationRequest();

		Set<ConstraintViolation<ProfileActivationUpdateRequest>> violations = validator.validate(user);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Username must be specified");

		// ==================================================================================================

		user = UserBuilder.user().withUsername("").buildProfileActivationRequest();
		violations = validator.validate(user);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Username must be specified");
	}

	@Test
	void whenUsernameDefinedButTooLongStringThenValidationFails() {
		var user = UserBuilder.user().withUsername(Faker.instance().random().hex(52)).buildProfileActivationRequest();

		Set<ConstraintViolation<ProfileActivationUpdateRequest>> violations = validator.validate(user);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Username character length must not exceed 50");
	}

}
