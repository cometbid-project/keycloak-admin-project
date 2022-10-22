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
@SuppressWarnings("serial")
public class UnusualLocationException extends ApplicationDefinedRuntimeException implements ErrorCode {

	private static final HttpStatus STATUS = HttpStatus.EXPECTATION_FAILED;

	/**
	 *
	 */
	public UnusualLocationException() {
		this(new Object[] {});
	}

	/**
	 *
	 * @param args
	 */
	public UnusualLocationException(Object[] args) {
		this("exception.unusualLocation", args);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 */
	public UnusualLocationException(String messagekey, Object[] args) {
		this(messagekey, args, null);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 * @param ex
	 */
	public UnusualLocationException(String messagekey, Object[] args, Throwable ex) {
		super(STATUS, ResourceBundleAccessor.accessMessageInBundle(messagekey, args), ex);
	}

	/**
	 * 
	 */
	@Override
	public String getErrorCode() {
		return ErrorCode.UNUSUAL_LOCATION_ERR_CODE;
	}
}
