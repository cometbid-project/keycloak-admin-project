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
import com.keycloak.admin.client.dataacess.GroupBuilder;
import com.keycloak.admin.client.models.CreateGroupRequest;
import com.keycloak.admin.client.models.GroupVO;

/**
 * @author Gbenga
 *
 */
class CreateGroupValidationTest {

	private static Validator validator;
	
	@BeforeAll
	static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void whenAllFieldsCorrectThenValidationSucceeds() {
		var groupVo = GroupBuilder.group().buildCreateGroupRequest();

		Set<ConstraintViolation<CreateGroupRequest>> violations = validator.validate(groupVo);
		assertThat(violations).isEmpty();
	}

	@Test
	void whenNameNotDefinedOrBlankThenValidationFails() {
		var groupVo = GroupBuilder.group().withName(null).buildCreateGroupRequest();

		Set<ConstraintViolation<CreateGroupRequest>> violations = validator.validate(groupVo);
		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Group name must be specified");

		// ==================================================================================================

		groupVo = GroupBuilder.group().withName("").buildCreateGroupRequest();
		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());

		assertThat(violations).hasSize(1);
		assertThat(constraintViolationMessages)// .contains("Role name character length must not exceed 30")
				.contains("Group name must be specified");
	}

	@Test
	void whenNameDefinedButTooLongStringThenValidationFails() {
		var groupVo = GroupBuilder.group().withName(Faker.instance().random().hex(31)).buildCreateGroupRequest();

		Set<ConstraintViolation<CreateGroupRequest>> violations = validator.validate(groupVo);
		assertThat(violations).hasSize(1);

		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());

		assertThat(constraintViolationMessages).contains("Group name character length must not exceed 30");
	}

	@Test
	void whenDescriptionNotDefinedThenValidationSucceeds() {
		var groupVo = GroupBuilder.group().withDescription(null).buildCreateGroupRequest();

		Set<ConstraintViolation<CreateGroupRequest>> violations = validator.validate(groupVo);
		assertThat(violations).isEmpty();

		// ==================================================================================================

		groupVo = GroupBuilder.group().withDescription("").buildCreateGroupRequest();

		violations = validator.validate(groupVo);
		assertThat(violations).isEmpty();
	}

	@Test
	void whenDescriptionDefinedButTooLongStringThenValidationFails() {
		var groupVo = GroupBuilder.group().withDescription(Faker.instance().random().hex(71)).buildCreateGroupRequest();

		Set<ConstraintViolation<CreateGroupRequest>> violations = validator.validate(groupVo); 
		assertThat(violations).hasSize(1);

		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());

		assertThat(constraintViolationMessages).contains("Group description character length must not exceed 70");
	}
}
