/**
 * 
 */
package com.keycloak.admin.client.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.javafaker.Faker;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.models.SearchUserRequest;

/**
 * @author Gbenga
 *
 */
class SearchUserRequestValidationTest {

	private static Validator validator;

	@BeforeAll
	static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void whenAllFieldsCorrectThenValidationSucceeds() {
		var user = UserBuilder.user().buildSearchUserRequest();

		Set<ConstraintViolation<SearchUserRequest>> violations = validator.validate(user);

		assertThat(violations).isEmpty();
	}

	@Test
	void whenFirstnameNotDefinedOrBlankThenValidationSucceeds() {
		var user = UserBuilder.user().withFirstName(null).buildSearchUserRequest();

		Set<ConstraintViolation<SearchUserRequest>> violations = validator.validate(user);

		assertThat(violations).isEmpty();

		// ==================================================================================================

		user = UserBuilder.user().withFirstName("").buildSearchUserRequest();
		violations = validator.validate(user);

		assertThat(violations).isEmpty();
	}
	
	@Test
	void whenFirstnameDefinedButTooLongStringThenValidationFails() {
		var user = UserBuilder.user().withFirstName(Faker.instance().random().hex(52)).buildSearchUserRequest();
		Set<ConstraintViolation<SearchUserRequest>> violations = validator.validate(user);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Firstname character length must not exceed 50");
	}
	
	@Test
	void whenLastnameNotDefinedOrBlankThenValidationSucceeds() {
		var user = UserBuilder.user().withLastName(null).buildSearchUserRequest();

		Set<ConstraintViolation<SearchUserRequest>> violations = validator.validate(user);

		assertThat(violations).isEmpty();

		// ==================================================================================================

		user = UserBuilder.user().withLastName("").buildSearchUserRequest();
		violations = validator.validate(user);

		assertThat(violations).isEmpty();
	}
	
	@Test
	void whenLastnameDefinedButTooLongStringThenValidationFails() {
		var user = UserBuilder.user().withLastName(Faker.instance().random().hex(52)).buildSearchUserRequest();
		Set<ConstraintViolation<SearchUserRequest>> violations = validator.validate(user);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Lastname character length must not exceed 50");
	}
	
	@Test
	void whenEmailNotDefinedOrBlankThenValidationSucceeds() {
		var user = UserBuilder.user().withEmail(null).buildSearchUserRequest();

		Set<ConstraintViolation<SearchUserRequest>> violations = validator.validate(user);

		assertThat(violations).isEmpty();

		// ==================================================================================================

		user = UserBuilder.user().withEmail("").buildSearchUserRequest();
		violations = validator.validate(user);

		assertThat(violations).isEmpty();
	}
	
	@Test
	void whenEmailDefinedButTooLongStringThenValidationFails() {
		var user = UserBuilder.user().withEmail(Faker.instance().random().hex(52)).buildSearchUserRequest();
		Set<ConstraintViolation<SearchUserRequest>> violations = validator.validate(user);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Email character length must not exceed 50");
	}
	
}
