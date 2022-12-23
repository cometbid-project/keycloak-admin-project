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
public class UserAlreadyExistException extends ApplicationDefinedRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8486744460448531453L;

	private static final HttpStatus STATUS = HttpStatus.CONFLICT;

	/**
	 *
	 */
	public UserAlreadyExistException() {
		this(new Object[] {});
	}

	/**
	 *
	 * @param args
	 */
	public UserAlreadyExistException(Object[] args) {
		this("exception.userAlreadyExist", args);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 */
	public UserAlreadyExistException(String messagekey, Object[] args) {
		this(messagekey, args, null);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 * @param ex
	 */
	public UserAlreadyExistException(String messagekey, Object[] args, Throwable ex) {
		super(STATUS, ResourceBundleAccessor.accessMessageInBundle(messagekey, args), ex);
	}

	/**
	 * 
	 */
	@Override
	public String getErrorCode() {
		return ErrorCode.USER_EXIST_ERR_CODE.getErrCode();
	}

	/**
	 * 
	 */
	@Override
	public String getErrorMessage() {
		String msgKey = ErrorCode.USER_EXIST_ERR_CODE.getErrMsgKey();
		return ResourceBundleAccessor.accessMessageInBundle(msgKey, new Object[] {});
	}

}
