package com.keycloak.admin.client.validators.qualifiers;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.keycloak.admin.client.validators.PasswordConstraintValidator;

/**
 * 
 * @author Gbenga
 *
 */
@Constraint(validatedBy = PasswordConstraintValidator.class)
@Documented
@Retention(RUNTIME)
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
public @interface ValidPassword {

	String message() default "{invalid.password}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
