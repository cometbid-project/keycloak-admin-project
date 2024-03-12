/**
 * 
 */
package com.keycloak.admin.client.domain;

import java.util.ArrayList;
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
import com.keycloak.admin.client.common.enums.Role;
import com.keycloak.admin.client.common.utils.ResourceBundleAccessor;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.models.UserVO;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Gbenga
 *
 */
class UserValidationTests {

	private static Validator validator;

	@BeforeAll
	static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void whenAllFieldsCorrectThenValidationSucceeds() {
		var userVo = UserBuilder.user().userVo(UUID.randomUUID());
		Set<ConstraintViolation<UserVO>> violations = validator.validate(userVo);
		assertThat(violations).isEmpty();
	}
	
	@Test
	void whenIdNotDefinedThenValidationSucceeds() {
		var userVo = UserBuilder.user().withId(null).userVo();

		Set<ConstraintViolation<UserVO>> violations = validator.validate(userVo);
		assertThat(violations).isEmpty();
	}

	@Test
	void whenUsernameNotDefinedOrBlankThenValidationSucceeds() {
		var userVo = UserBuilder.user().withUsername(null).userVo();

		Set<ConstraintViolation<UserVO>> violations = validator.validate(userVo);
		assertThat(violations).isEmpty();
		
		//======================================================================================
		userVo = UserBuilder.user().withUsername("").userVo();

	    violations = validator.validate(userVo);
		assertThat(violations).isEmpty();
	}

	@Test
	void whenUsernameDefinedButTooLongStringThenValidationFails() {
		var userVo = UserBuilder.user().withUsername(Faker.instance().random().hex(331)).userVo();

		Set<ConstraintViolation<UserVO>> violations = validator.validate(userVo);
		assertThat(violations).hasSize(1);

		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());

		String message = ResourceBundleAccessor.accessMessageInBundle("User.username.size", new Object[] {});
		System.out.println("Message from Validation Bundle: " + message);

		assertThat(constraintViolationMessages).contains("Username character length must not exceed 50");
	}

	@Test
	void whenEmailIsNotDefinedValidationSucceeds() {
		var userVo = UserBuilder.user().withEmail(null).userVo();

		Set<ConstraintViolation<UserVO>> violations = validator.validate(userVo);
		assertThat(violations).isEmpty();
	}

	@Test
	void whenEmailDefinedButTooLongStringThenValidationFails() {
		var userVo = UserBuilder.user().withEmail(Faker.instance().random().hex(51)).userVo();

		Set<ConstraintViolation<UserVO>> violations = validator.validate(userVo);
		assertThat(violations).hasSize(2);

		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());
		assertThat(constraintViolationMessages).contains("Email character length must not exceed 50")
				.contains("Email specified is not a properly formatted email address");
	}

	@Test
	void whenEmailDefinedWithInvalidFormatThenValidationFails() {
		var userVo = UserBuilder.user().withEmail(Faker.instance().internet().domainName()).userVo();

		Set<ConstraintViolation<UserVO>> violations = validator.validate(userVo);
		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage())
				.isEqualTo("Email specified is not a properly formatted email address");
	}

	@Test
	void whenFirstNameNotDefinedOrBlankThenValidationFails() {
		var userVo = UserBuilder.user().withFirstName("").userVo();

		Set<ConstraintViolation<UserVO>> violations = validator.validate(userVo);
		assertThat(violations).hasSize(2);

		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());
		assertThat(constraintViolationMessages).contains("Firstname character length must not exceed 50")
				.contains("Firstname must be specified");

		// ==================================================================================================

		userVo = UserBuilder.user().withFirstName(null).userVo();
		violations = validator.validate(userVo);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Firstname must be specified");
	}

	@Test
	void whenFirstNameDefinedButTooLongStringThenValidationFails() {
		var userVo = UserBuilder.user().withFirstName(Faker.instance().random().hex(52)).userVo();

		Set<ConstraintViolation<UserVO>> violations = validator.validate(userVo);
		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage())
				.isEqualTo("Firstname character length must not exceed 50");
	}

	@Test
	void whenLastNameNotDefinedOrBlankThenValidationFails() {
		var userVo = UserBuilder.user().withLastName("").userVo();

		Set<ConstraintViolation<UserVO>> violations = validator.validate(userVo);
		assertThat(violations).hasSize(2);

		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());
		assertThat(constraintViolationMessages).contains("Lastname character length must not exceed 50")
				.contains("Lastname must be specified");

		// ==================================================================================================

		userVo = UserBuilder.user().withLastName(null).userVo();
		violations = validator.validate(userVo);

		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Lastname must be specified");
	}

	@Test
	void whenLastNameDefinedButTooLongStringThenValidationFails() {
		var userVo = UserBuilder.user().withLastName(Faker.instance().random().hex(52)).userVo();

		Set<ConstraintViolation<UserVO>> violations = validator.validate(userVo);
		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage()).isEqualTo("Lastname character length must not exceed 50");
	}

	@Test
	void whenRolesIsDefinedButEmptyThenValidationFails() {
		var userVo = UserBuilder.user().withRoles(new ArrayList<>()).userVo();

		Set<ConstraintViolation<UserVO>> violations = validator.validate(userVo);
		assertThat(violations).hasSize(2);

		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());
		assertThat(constraintViolationMessages).contains("Not more than one role is allowed per user")
				.contains("Atleast one role must be specified");
	}

	@Test
	void whenRolesIsDefinedWithMultipleRolesThenValidationFails() {
		var userVo = UserBuilder.user().withRoles(List.of(Role.ROLE_ADMIN, Role.ROLE_USER)).userVo();

		Set<ConstraintViolation<UserVO>> violations = validator.validate(userVo);
		assertThat(violations).hasSize(1);

		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());
		assertThat(constraintViolationMessages).contains("Not more than one role is allowed per user");
	}
}
