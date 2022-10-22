/**
 * 
 */
package com.keycloak.admin.client.error.handlers;

/**
 * @author Gbenga
 *
 */
@FunctionalInterface
public interface RunnableCheckedExceptionHandler<T, E extends Exception> {

	void run() throws E;
}
