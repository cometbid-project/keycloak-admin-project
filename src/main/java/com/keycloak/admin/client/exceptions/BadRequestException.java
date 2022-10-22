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
public class BadRequestException extends ApplicationDefinedRuntimeException implements ErrorCode {

	/**
	 *
	 */
	private static final long serialVersionUID = 2415606351370043861L;

	private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

	/**
	 *
	 */
	public BadRequestException() {
		this(new Object[] {});
	}

	/**
	 *
	 * @param args
	 */
	public BadRequestException(Object[] args) {
		this("exception.badRequest", args);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 */
	public BadRequestException(String messagekey, Object[] args) {
		this(messagekey, args, null);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 * @param ex
	 */
	public BadRequestException(String messagekey, Object[] args, Throwable ex) {
		super(STATUS, ResourceBundleAccessor.accessMessageInBundle(messagekey, args), ex);
	}
	
	/**
	 * 
	 */
	@Override
	public String getErrorCode() {
		return ErrorCode.BAD_REQUEST_ERR_CODE;
	}

}
