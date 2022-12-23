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
public class UserProfileDisabledException extends ApplicationDefinedRuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = -7164478036859920435L;

	private static final HttpStatus STATUS = HttpStatus.PRECONDITION_REQUIRED;

	/**
	 *
	 */
	public UserProfileDisabledException() {
		this(new Object[] {});
	}

	/**
	 *
	 * @param args
	 */
	public UserProfileDisabledException(Object[] args) {
		this("exception.userProfileInactive", args);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 */
	public UserProfileDisabledException(String messagekey, Object[] args) {
		this(messagekey, args, null);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 * @param ex
	 */
	public UserProfileDisabledException(String messagekey, Object[] args, Throwable ex) {
		super(STATUS, ResourceBundleAccessor.accessMessageInBundle(messagekey, args), ex);
	}

	/**
	 * 
	 */
	@Override
	public String getErrorCode() {
		return ErrorCode.EXPIRED_PROFILE_ERR_CODE.getErrCode();
	}

	/**
	 * 
	 */
	@Override
	public String getErrorMessage() {
		String msgKey = ErrorCode.EXPIRED_PROFILE_ERR_CODE.getErrMsgKey();
		return ResourceBundleAccessor.accessMessageInBundle(msgKey, new Object[] {});
	}

}
