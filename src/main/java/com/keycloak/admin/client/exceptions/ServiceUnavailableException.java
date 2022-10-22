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
public class ServiceUnavailableException extends ApplicationDefinedRuntimeException implements ErrorCode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9076188781838210202L;

	private static final HttpStatus STATUS = HttpStatus.SERVICE_UNAVAILABLE;

	/**
	 *
	 */
	public ServiceUnavailableException() {
		this(new Object[] {});
	}

	/**
	 *
	 * @param args
	 */
	public ServiceUnavailableException(Object[] args) {
		this("exception.UnavailableService", args);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 */
	public ServiceUnavailableException(String messagekey, Object[] args) {
		this(messagekey, args, null);
	}

	/**
	 *
	 * @param messagekey
	 * @param args  
	 * @param ex
	 */
	public ServiceUnavailableException(String messagekey, Object[] args, Throwable ex) {
		super(STATUS, ResourceBundleAccessor.accessMessageInBundle(messagekey, args), ex);
	}

	/**
	 * 
	 */
	@Override
	public String getErrorCode() {
		return ErrorCode.UNAVAILABLE_SERVICE_ERR_CODE;
	}

}
