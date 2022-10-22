/**
 * 
 */
package com.keycloak.admin.client.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.validator.routines.EmailValidator;

import com.keycloak.admin.client.validators.qualifiers.ValidEmail;


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
		if (email == null) {
			return true;
		}
		return (validateEmail(email));
	}

	private boolean validateEmail(String email) {
		EmailValidator validator = EmailValidator.getInstance();

		return validator.isValid(email);
	}

}
