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
import com.keycloak.admin.client.dataacess.GroupBuilder;
import com.keycloak.admin.client.dataacess.RoleBuilder;
import com.keycloak.admin.client.models.GroupVO;
import com.keycloak.admin.client.models.RoleVO;

/**
 * @author Gbenga
 *
 */
class GroupValidationTests {

	private static Validator validator;

	@BeforeAll
	static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void whenAllFieldsCorrectThenValidationSucceeds() {
		var groupVo = GroupBuilder.group().build();

		Set<ConstraintViolation<GroupVO>> violations = validator.validate(groupVo);
		assertThat(violations).isEmpty();
	}

	@Test
	void whenIdNotDefinedThenValidationSucceeds() {
		var groupVo = GroupBuilder.group().withId(null).build();

		Set<ConstraintViolation<GroupVO>> violations = validator.validate(groupVo);
		assertThat(violations).isEmpty();
	}

	@Test
	void whenNameNotDefinedOrBlankThenValidationFails() {
		var groupVo = GroupBuilder.group().withName(null).build();

		Set<ConstraintViolation<GroupVO>> violations = validator.validate(groupVo);
		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Group name must be specified");

		// ==================================================================================================

		groupVo = GroupBuilder.group().withName("").build();
		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());

		assertThat(violations).hasSize(1);
		assertThat(constraintViolationMessages)// .contains("Role name character length must not exceed 30")
				.contains("Group name must be specified");
	}

	@Test
	void whenNameDefinedButTooLongStringThenValidationFails() {
		var groupVo = GroupBuilder.group().withName(Faker.instance().random().hex(31)).build();

		Set<ConstraintViolation<GroupVO>> violations = validator.validate(groupVo);
		assertThat(violations).hasSize(1);

		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());

		assertThat(constraintViolationMessages).contains("Group name character length must not exceed 30");
	}

	@Test
	void whenPathNotDefinedThenValidationSucceeds() {
		var groupVo = GroupBuilder.group().withPath(null).build();

		Set<ConstraintViolation<GroupVO>> violations = validator.validate(groupVo);
		assertThat(violations).isEmpty();

		// ==================================================================================================

		groupVo = GroupBuilder.group().withPath("").build();

		violations = validator.validate(groupVo);
		assertThat(violations).isEmpty();
	}

	@Test
	void whenPathDefinedButTooLongStringThenValidationFails() {
		var groupVo = GroupBuilder.group().withPath(Faker.instance().random().hex(71)).build();

		Set<ConstraintViolation<GroupVO>> violations = validator.validate(groupVo); 
		assertThat(violations).hasSize(1);

		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());

		assertThat(constraintViolationMessages).contains("Group path character length must not exceed 50");
	}

}
