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
public class UnexpectedResultException extends ApplicationDefinedRuntimeException implements ErrorCode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9076188781838210202L;

	private static final HttpStatus STATUS = HttpStatus.INTERNAL_SERVER_ERROR;

	/**
	 *
	 */
	public UnexpectedResultException() {
		this(new Object[] {});
	}

	/**
	 *
	 * @param args
	 */
	public UnexpectedResultException(Object[] args) {
		this("exception.UnexpectedOutcome", args);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 */
	public UnexpectedResultException(String messagekey, Object[] args) {
		this(messagekey, args, null);
	}

	/**
	 *
	 * @param messagekey
	 * @param args  
	 * @param ex
	 */
	public UnexpectedResultException(String messagekey, Object[] args, Throwable ex) {
		super(STATUS, ResourceBundleAccessor.accessMessageInBundle(messagekey, args), ex);
	}

	/**
	 * 
	 */
	@Override
	public String getErrorCode() {
		return ErrorCode.SYS_DEFINED_ERR_CODE;
	}

}
