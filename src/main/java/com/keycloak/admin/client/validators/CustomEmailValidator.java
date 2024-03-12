/**
 * 
 */
package com.keycloak.admin.client.validators;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import com.keycloak.admin.client.validators.qualifiers.ValidEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @author Gbenga
 *
 */
public class CustomEmailValidator implements ConstraintValidator<ValidEmail, String> {

	@Override
	public void initialize(ValidEmail constraintAnnotation) {
	}

	@Override
	public boolean isValid(String email, ConstraintValidatorContext context) {
		if (StringUtils.isBlank(email)) {
			return true;
		}
		return (validateEmail(email));
	}

	private boolean validateEmail(String email) {
		EmailValidator validator = EmailValidator.getInstance();

		return validator.isValid(email);
	}

}
