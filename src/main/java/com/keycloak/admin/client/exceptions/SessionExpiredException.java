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
public class SessionExpiredException extends ApplicationDefinedRuntimeException implements ErrorCode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9076188781838210202L;
	
	private static final HttpStatus STATUS = HttpStatus.UNAUTHORIZED;

	/**
	 *
	 */
	public SessionExpiredException() {
		this(new Object[] {});
	}

	/**
	 *
	 * @param args
	 */
	public SessionExpiredException(Object[] args) {
		this("session.expired", args);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 */
	public SessionExpiredException(String messagekey, Object[] args) {
		this(messagekey, args, null);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 * @param ex
	 */
	public SessionExpiredException(String messagekey, Object[] args, Throwable ex) {
		super(STATUS, ResourceBundleAccessor.accessMessageInBundle(messagekey, args), ex);
	}

	/**
	 * 
	 */
	@Override
	public String getErrorCode() {
		return ErrorCode.UNAUTHORIZED_ACCESS_ERR_CODE;
	}

}
