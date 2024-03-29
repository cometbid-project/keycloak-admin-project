/**
 * 
 */
package com.keycloak.admin.client.validators;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.passay.WhitespaceRule;

import com.keycloak.admin.client.common.enums.SpecialCharacterData;
import com.keycloak.admin.client.validators.qualifiers.ValidPassword;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


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
    	if (StringUtils.isBlank(password)) { 
			return true;
		}
             
       PasswordValidator validator = new PasswordValidator(
    		   // Arrays.asList(new LengthRule(8, 30),
				new CharacterRule(EnglishCharacterData.UpperCase, 1),
				new CharacterRule(EnglishCharacterData.LowerCase, 1), 
				new CharacterRule(EnglishCharacterData.Digit, 1),
				new CharacterRule(SpecialCharacterData.Special, 1), 
				new WhitespaceRule());
    		 // );
 
        RuleResult result = validator.validate(new PasswordData(password));
        return result.isValid();
        
        //context.disableDefaultConstraintViolation();        
       // context.buildConstraintViolationWithTemplate(String.join(",", validator.getMessages(result)))
        //  .addConstraintViolation();
    }
}
