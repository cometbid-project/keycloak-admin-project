/**
 * 
 */
package com.keycloak.admin.client.error.handlers;

/**
 * @author Gbenga
 *
 */
@FunctionalInterface
public interface LambdaCheckedExceptionFunction<T, R> {

	public R apply(T t) throws Exception;
}
