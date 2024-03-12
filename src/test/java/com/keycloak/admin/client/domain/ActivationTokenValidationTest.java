/**
 * 
 */
package com.keycloak.admin.client.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.keycloak.admin.client.dataacess.AuthBuilder;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.models.ActivationTokenModel;
import com.keycloak.admin.client.models.UserVO;

/**
 * @author Gbenga
 *
 */
class ActivationTokenValidationTest {

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

		var req = AuthBuilder.auth(userVO).buildActivationToken();

		Set<ConstraintViolation<ActivationTokenModel>> violations = validator.validate(req); 
		assertThat(violations).isEmpty();
	}
	
	@Test
	void whenUsernameNotDefinedOrBlankThenValidationFails() {
		UUID userId = UUID.randomUUID();
		UserVO userVO = UserBuilder.user().userVo(userId);

		var req = AuthBuilder.auth(userVO).withUsername(null).buildActivationToken();

		Set<ConstraintViolation<ActivationTokenModel>> violations = validator.validate(req); 
		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage())
				.isEqualTo("Username is required for activation");
		
		//======================================================================================
		
		req = AuthBuilder.auth(userVO).withUsername(null).buildActivationToken();

		violations = validator.validate(req); 
		assertThat(violations).hasSize(1);

		assertThat(violations.iterator().next().getMessage())
				.isEqualTo("Username is required for activation");
	}
	
	@Test
	void whenActivationTokenNotDefinedOrBlankThenValidationFails() {
		UUID userId = UUID.randomUUID();
		UserVO userVO = UserBuilder.user().userVo(userId);

		var req = AuthBuilder.auth(userVO).withAccessToken(null).buildActivationToken();

		Set<ConstraintViolation<ActivationTokenModel>> violations = validator.validate(req); 

		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage()).isEqualTo("Activation token is not specified");

		// ===============================================================================================================

		req = AuthBuilder.auth(userVO).withAccessToken("").buildActivationToken();

		violations = validator.validate(req); 

		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage()).isEqualTo("Activation token is not specified");
	}
}
