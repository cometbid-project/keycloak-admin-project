/**
 * 
 */
package com.keycloak.admin.client.validators.qualifiers;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.keycloak.admin.client.validators.CustomEmailValidator;

/**
 * @author Gbenga
 *
 */
@Target({TYPE_USE, FIELD, ANNOTATION_TYPE}) 
@Retention(RUNTIME)
@Constraint(validatedBy = CustomEmailValidator.class)
@Documented
public @interface ValidEmail {  
	
    String message() default "{invalid.email}";
    
    Class<?>[] groups() default {}; 
    
    Class<? extends Payload>[] payload() default {};
}
