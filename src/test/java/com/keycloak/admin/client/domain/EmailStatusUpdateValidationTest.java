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
import com.keycloak.admin.client.models.EmailStatusUpdateRequest;

/**
 * @author Gbenga
 *
 */
class EmailStatusUpdateValidationTest {

	private static Validator validator;

	@BeforeAll
	static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}
	
	@Test
	void whenAllFieldsCorrectThenValidationSucceeds() {
		var req = UserBuilder.user().buildEmailUpdate();
		Set<ConstraintViolation<EmailStatusUpdateRequest>> violations = validator.validate(req); 

		assertThat(violations).isEmpty();
	}
	
	@Test
	void whenEmailIsNotDefinedValidationFails() {
		var userVo = UserBuilder.user().withEmail(null).buildEmailUpdate();

		Set<ConstraintViolation<EmailStatusUpdateRequest>> violations = validator.validate(userVo);
		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage())
				.isEqualTo("Email not specified, please supply your email address");
	}

	@Test
	void whenEmailDefinedButTooLongStringThenValidationFails() {
		var userVo = UserBuilder.user().withEmail(Faker.instance().random().hex(51)).buildEmailUpdate();

		Set<ConstraintViolation<EmailStatusUpdateRequest>> violations = validator.validate(userVo);
		assertThat(violations).hasSize(2);

		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());
		assertThat(constraintViolationMessages).contains("Email character length must not exceed 50") 
				.contains("Email specified is not a properly formatted email address");
	}

	@Test
	void whenEmailDefinedWithInvalidFormatThenValidationFails() {
		var userVo = UserBuilder.user().withEmail(Faker.instance().internet().domainName()).buildEmailUpdate();

		Set<ConstraintViolation<EmailStatusUpdateRequest>> violations = validator.validate(userVo);
		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage())
				.isEqualTo("Email specified is not a properly formatted email address");
	}
}
