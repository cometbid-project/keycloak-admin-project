/**
 * 
 */
package com.keycloak.admin.client.exceptions;

import java.util.Locale;

import org.springframework.http.HttpStatus;

import com.keycloak.admin.client.common.utils.ResourceBundleAccessor;

/**
 * @author Gbenga
 *
 */
public class InvalidJwtTokenException extends ApplicationDefinedRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6472078305776553107L;
	private static final HttpStatus STATUS = HttpStatus.UNAUTHORIZED;

	/**
	 *
	 */
	public InvalidJwtTokenException() {
		this(new Object[] {});
	}

	/**
	 *
	 * @param args
	 */
	public InvalidJwtTokenException(Object[] args) {
		this("exception.invalidJwtTokenException", args);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 */
	public InvalidJwtTokenException(String messagekey, Object[] args) {
		this(messagekey, args, null);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 * @param ex
	 */
	public InvalidJwtTokenException(String messagekey, Object[] args, Throwable ex) {
		super(STATUS, ResourceBundleAccessor.accessMessageInBundle(messagekey, args), ex);
	}

	/**
	 * 
	 */
	@Override
	public String getErrorCode() {
		return ErrorCode.INVALID_JWT_TOKEN_ERR_CODE;
	}

}
