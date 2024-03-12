/**
 * 
 */
package com.keycloak.admin.client.validators.qualifiers;

/**
 * @author Gbenga
 *
 */
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

import com.keycloak.admin.client.validators.IpAddressValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 
 * @author Gbenga
 *
 */
@Target({ FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = IpAddressValidator.class)
@Documented
public @interface IpAddress {

	String message() default "invalid IP address";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
