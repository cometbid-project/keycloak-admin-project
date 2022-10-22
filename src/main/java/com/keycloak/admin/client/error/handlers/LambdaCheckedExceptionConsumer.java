/**
 * 
 */
package com.keycloak.admin.client.error.handlers;

/**
 * @author Gbenga
 *
 */
@FunctionalInterface
public interface LambdaCheckedExceptionConsumer<T, E extends Exception> {

	public void accept(T target) throws E;
}
