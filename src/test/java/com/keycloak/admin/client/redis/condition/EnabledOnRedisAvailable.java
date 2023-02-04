/**
 * 
 */
package com.keycloak.admin.client.redis.condition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Gbenga
 *
 */
/**
 * {@code @EnabledOnRedisAvailable} is used to signal that the annotated test
 * class or test method is only <em>enabled</em> if Redis is running at
 * {@link #port() port}.
 * <p/>
 * When applied at the class level, all test methods within that class will be
 * enabled.
 *
 * @author Mark Paluch
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@ExtendWith(EnabledOnRedisAvailableCondition.class)
public @interface EnabledOnRedisAvailable {

	String host() default "localhost";

	/**
	 * Redis port number.
	 */
	int port() default 6379;
}