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
public class UserHasNoAttributesException extends ApplicationDefinedRuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = -7164478036859920435L;

	private static final HttpStatus STATUS = HttpStatus.PRECONDITION_REQUIRED;

	/**
	 *
	 */
	public UserHasNoAttributesException() {
		this(new Object[] {});
	}

	/**
	 *
	 * @param args
	 */
	public UserHasNoAttributesException(Object[] args) {
		this("exception.userHasNoAttributes", args);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 */
	public UserHasNoAttributesException(String messagekey, Object[] args) {
		this(messagekey, args, null);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 * @param ex
	 */
	public UserHasNoAttributesException(String messagekey, Object[] args, Throwable ex) {
		super(STATUS, ResourceBundleAccessor.accessMessageInBundle(messagekey, args), ex);
	}

	/**
	 * 
	 */
	@Override
	public String getErrorCode() {
		return ErrorCode.NOATTRIBUTES_PROFILE_ERR_CODE.getErrCode();
	}

	/**
	 * 
	 */
	@Override
	public String getErrorMessage() {
		String msgKey = ErrorCode.NOATTRIBUTES_PROFILE_ERR_CODE.getErrMsgKey();
		return ResourceBundleAccessor.accessMessageInBundle(msgKey, new Object[] {});
	}

}
