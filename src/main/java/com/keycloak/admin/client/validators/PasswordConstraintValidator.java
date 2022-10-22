/**
 * 
 */
package com.keycloak.admin.client.validators;

import java.util.Arrays;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.passay.WhitespaceRule;

import com.keycloak.admin.client.common.enums.SpecialCharacterData;
import com.keycloak.admin.client.validators.qualifiers.ValidPassword;


/**
 * @author Gbenga
 *
 */
public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {
	 
    @Override
    public void initialize(ValidPassword arg0) {
    }
 
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
             
       PasswordValidator validator = new PasswordValidator(
    		   Arrays.asList(new LengthRule(8, 30),
				new CharacterRule(EnglishCharacterData.UpperCase, 1),
				new CharacterRule(EnglishCharacterData.LowerCase, 1), 
				new CharacterRule(EnglishCharacterData.Digit, 1),
				new CharacterRule(SpecialCharacterData.Special, 1), 
				new WhitespaceRule()));
 
        RuleResult result = validator.validate(new PasswordData(password));
        if (result.isValid()) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(String.join(",", validator.getMessages(result)))
          .addConstraintViolation();
        return false;
    }
}
