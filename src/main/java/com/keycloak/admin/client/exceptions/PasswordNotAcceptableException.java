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
public class PasswordNotAcceptableException extends ApplicationDefinedRuntimeException implements ErrorCode {

	/**
	 *
	 */
	private static final long serialVersionUID = 4399584063816193177L;

	private static final HttpStatus STATUS = HttpStatus.PRECONDITION_REQUIRED;

	/**
	 *
	 */
	public PasswordNotAcceptableException() {
		this(new Object[] {});
	}

	/**
	 *
	 * @param args
	 */
	public PasswordNotAcceptableException(Object[] args) {
		this("exception.unacceptablePassword", args);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 */
	public PasswordNotAcceptableException(String messagekey, Object[] args) {
		this(messagekey, args, null);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 * @param ex
	 */
	public PasswordNotAcceptableException(String messagekey, Object[] args, Throwable ex) {
		super(STATUS, ResourceBundleAccessor.accessMessageInBundle(messagekey, args), ex);
	}

	/**
	 * 
	 */
	@Override
	public String getErrorCode() {
		return ErrorCode.INVALID_PASSWORD_ERR_CODE;
	}

}
