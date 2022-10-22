/**
 * 
 */
package com.keycloak.admin.client.validators.qualifiers;

/**
 * @author Gbenga
 *
 */
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({ FIELD, METHOD })
@Documented
@Constraint(validatedBy = VerifyEnumValidator.class)
public @interface VerifyValue {

	String message() default "${invalid.value}";

	Class<?>[] groups() default {};  

	Class<? extends Payload>[] payload() default {};

	Class<? extends Enum<?>> value();

}