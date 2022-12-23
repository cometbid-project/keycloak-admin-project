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
import com.keycloak.admin.client.dataacess.RoleBuilder;
import com.keycloak.admin.client.models.CreateRoleRequest;

/**
 * @author Gbenga
 *
 */
class CreateRoleValidationTest {

	private static Validator validator;

	@BeforeAll
	static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void whenAllFieldsCorrectThenValidationSucceeds() {
		var roleVo = RoleBuilder.role().build();

		Set<ConstraintViolation<CreateRoleRequest>> violations = validator.validate(roleVo);  
		assertThat(violations).isEmpty();
	}

	@Test
	void whenNameNotDefinedOrBlankThenValidationFails() {
		var roleVo = RoleBuilder.role().withName(null).build();

		Set<ConstraintViolation<CreateRoleRequest>> violations = validator.validate(roleVo);
		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Role name must be specified");

		// ==================================================================================================
 
		roleVo = RoleBuilder.role().withName("").build();
		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());

		assertThat(violations).hasSize(1);
		assertThat(constraintViolationMessages)//.contains("Role name character length must not exceed 30")
				.contains("Role name must be specified");
	}

	@Test
	void whenNameDefinedButTooLongStringThenValidationFails() {
		var roleVo = RoleBuilder.role().withName(Faker.instance().random().hex(31)).build();

		Set<ConstraintViolation<CreateRoleRequest>> violations = validator.validate(roleVo); 
		assertThat(violations).hasSize(1);

		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());

		assertThat(constraintViolationMessages).contains("Role name character length must not exceed 30");
	}
	
	@Test
	void whenDescriptionNotDefinedThenValidationSucceeds() {
		var roleVo = RoleBuilder.role().withDescription(null).build();

		Set<ConstraintViolation<CreateRoleRequest>> violations = validator.validate(roleVo); 
		assertThat(violations).isEmpty();
	}

	@Test
	void whenDescriptionDefinedButTooLongStringThenValidationFails() {
		var userVo = RoleBuilder.role().withDescription(Faker.instance().random().hex(71)).build();

		Set<ConstraintViolation<CreateRoleRequest>> violations = validator.validate(userVo); 
		assertThat(violations).hasSize(1);

		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());

		assertThat(constraintViolationMessages).contains("Role description character length must not exceed 70");
	}

}
