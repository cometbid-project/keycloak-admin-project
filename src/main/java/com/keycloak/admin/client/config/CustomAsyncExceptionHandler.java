/**
 * 
 */
package com.keycloak.admin.client.config;

/**
 * @author Gbenga
 *
 */
import java.lang.reflect.Method;
import lombok.extern.log4j.Log4j2;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

/**
 * 
 * @author Gbenga
 *
 */
@Log4j2
public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

	@Override
	public void handleUncaughtException(final Throwable throwable, final Method method, final Object... obj) {
		log.error("Exception occured with a message - {}", throwable.getMessage());
		log.error("Method name that threw exception - {}", method.getName());

		for (final Object param : obj) {
			log.info("Param - {}", param);
		}
	}

}