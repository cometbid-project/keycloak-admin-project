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
public class UserProfileExpiredException extends ApplicationDefinedRuntimeException implements ErrorCode {

	/**
	 *
	 */
	private static final long serialVersionUID = -7164478036859920435L;

	private static final HttpStatus STATUS = HttpStatus.PRECONDITION_REQUIRED;

	/**
	 *
	 */
	public UserProfileExpiredException() {
		this(new Object[] {});
	}

	/**
	 *
	 * @param args
	 */
	public UserProfileExpiredException(Object[] args) {
		this("exception.userHasBeenExpired", args);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 */
	public UserProfileExpiredException(String messagekey, Object[] args) {
		this(messagekey, args, null);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 * @param ex
	 */
	public UserProfileExpiredException(String messagekey, Object[] args, Throwable ex) {
		super(STATUS, ResourceBundleAccessor.accessMessageInBundle(messagekey, args), ex);
	}

	/**
	 * 
	 */
	@Override
	public String getErrorCode() {
		return ErrorCode.EXPIRED_PROFILE_ERR_CODE;
	}

}
