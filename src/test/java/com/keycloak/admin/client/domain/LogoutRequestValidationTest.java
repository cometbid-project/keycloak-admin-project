/**
 * 
 */
package com.keycloak.admin.client.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.keycloak.admin.client.dataacess.AuthBuilder;
import com.keycloak.admin.client.dataacess.GroupBuilder;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.models.LogoutRequest;
import com.keycloak.admin.client.models.SendOtpRequest;
import com.keycloak.admin.client.models.UserVO;

/**
 * @author Gbenga
 *
 */
class LogoutRequestValidationTest {

	private static Validator validator;

	@BeforeAll
	static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void whenAllFieldsCorrectThenValidationSucceeds() {
		UUID userId = UUID.randomUUID();
		UserVO userVO = UserBuilder.user().userVo(userId);

		var req = AuthBuilder.auth(userVO).logoutRequest();

		Set<ConstraintViolation<LogoutRequest>> violations = validator.validate(req);
		assertThat(violations).isEmpty();
	}

	@Test
	void whenRefreshTokenNotDefinedOrBlankThenValidationFails() {
		UUID userId = UUID.randomUUID();
		UserVO userVO = UserBuilder.user().userVo(userId);

		var req = AuthBuilder.auth(userVO).withRefreshToken(null).logoutRequest();

		Set<ConstraintViolation<LogoutRequest>> violations = validator.validate(req);

		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage()).isEqualTo("Refresh token is required");

		// ==================================================================================================

		req = AuthBuilder.auth(userVO).withRefreshToken("").logoutRequest();
		List<String> constraintViolationMessages = violations.stream().map(ConstraintViolation::getMessage)
				.collect(Collectors.toList());

		assertThat(violations).hasSize(1);
		assertThat(constraintViolationMessages).contains("Refresh token is required");
	}
}
