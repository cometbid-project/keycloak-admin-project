/**
 * 
 */
package com.keycloak.admin.client.exceptions;

import org.springframework.http.HttpStatus;

import com.keycloak.admin.client.common.utils.ResourceBundleAccessor;

/**
 * @author Gbenga
 *
 */
public class AuthenticationError extends ApplicationDefinedRuntimeException implements ErrorCode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6970411053185572571L;

	private static final HttpStatus STATUS = HttpStatus.UNAUTHORIZED;

	/**
	 *
	 */
	public AuthenticationError() {
		this(new Object[] {});
	}

	/**
	 *
	 * @param args
	 */
	public AuthenticationError(Object[] args) {
		this("exception.authenticationFailed", args);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 */
	public AuthenticationError(String messagekey, Object[] args) {
		this(messagekey, args, null);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 * @param ex
	 */
	public AuthenticationError(String messagekey, Object[] args, Throwable ex) {
		super(STATUS, ResourceBundleAccessor.accessMessageInBundle(messagekey, args), ex);
	}

	/**
	 * 
	 * @param errorMessage
	 */
	public AuthenticationError(String errorMessage) {
		// TODO Auto-generated constructor stub
		super(STATUS, errorMessage);
	}

	/**
	 * 
	 */
	@Override
	public String getErrorCode() {
		return ErrorCode.AUTHENTICATION_ERR_CODE;
	}
}
