/**
 * 
 */
package com.keycloak.admin.client.aop.qualifiers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Gbenga
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Loggable {

	boolean printParamsValues() default false;

	String callMethodWithNoParamsToString() default "toString";
}
