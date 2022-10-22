/**
 * 
 */
package com.keycloak.admin.client.validators;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import com.keycloak.admin.client.exceptions.CustomConstraintViolationException;

/**
 * @author Gbenga
 *
 */
public class GlobalProgrammaticValidator {

	private static Validator validator;
	
	static {   
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	public static void validateInput(Object input) {

		Set<ConstraintViolation<Object>> violations = validator.validate(input);

		if (!violations.isEmpty()) {
			throw new CustomConstraintViolationException(violations);
		}
	}
}
