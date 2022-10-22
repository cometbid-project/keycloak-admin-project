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
public class UserProfileLockedException extends ApplicationDefinedRuntimeException implements ErrorCode {

	/**
	 *
	 */
	private static final long serialVersionUID = -7164478036859920435L;

	private static final HttpStatus STATUS = HttpStatus.LOCKED;

	/**
	 *
	 */
	public UserProfileLockedException() {
		this(new Object[] {});
	}

	/**
	 *
	 * @param args
	 */
	public UserProfileLockedException(Object[] args) {
		this("exception.userHasBeenLocked", args);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 */
	public UserProfileLockedException(String messagekey, Object[] args) {
		this(messagekey, args, null);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 * @param ex
	 */
	public UserProfileLockedException(String messagekey, Object[] args, Throwable ex) {
		super(STATUS, ResourceBundleAccessor.accessMessageInBundle(messagekey, args), ex);
	}

	/**
	 * 
	 */
	@Override
	public String getErrorCode() {
		return ErrorCode.LOCKED_PROFILE_ERR_CODE;
	}

}
