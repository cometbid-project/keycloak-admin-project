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
public class UserProfileUnverifiedException extends ApplicationDefinedRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -607083704208786569L;

	private static final HttpStatus STATUS = HttpStatus.LOCKED;

	/**
	 *
	 */
	public UserProfileUnverifiedException() {
		this(new Object[] {});
	}

	/**
	 *
	 * @param args
	 */
	public UserProfileUnverifiedException(Object[] args) {
		this("exception.userUnverified", args);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 */
	public UserProfileUnverifiedException(String messagekey, Object[] args) {
		this(messagekey, args, null);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 * @param ex
	 */
	public UserProfileUnverifiedException(String messagekey, Object[] args, Throwable ex) {
		super(STATUS, ResourceBundleAccessor.accessMessageInBundle(messagekey, args), ex);
	}

	/**
	 * 
	 */
	@Override
	public String getErrorCode() {
		return ErrorCode.UNVERIFIED_PROFILE_ERR_CODE.getErrCode();
	}

	/**
	 * 
	 */
	@Override
	public String getErrorMessage() {
		String msgKey = ErrorCode.UNVERIFIED_PROFILE_ERR_CODE.getErrMsgKey();
		return ResourceBundleAccessor.accessMessageInBundle(msgKey, new Object[] {});
	}

}
