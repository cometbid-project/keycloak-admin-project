/**
 * 
 */
package com.keycloak.admin.client.exceptions;

import java.util.Locale;
import org.springframework.http.HttpStatus;

import com.keycloak.admin.client.common.utils.ResourceBundleAccessor;

/**
 * @author Gbenga
 *
 */
public class UnauthenticatedUserException extends ApplicationDefinedRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6566804102885111686L;
	private static final HttpStatus STATUS = HttpStatus.UNAUTHORIZED;

	/**
	 *
	 */
	public UnauthenticatedUserException() {
		this(new Object[] {});
	}

	/**
	 *
	 * @param args
	 */
	public UnauthenticatedUserException(Object[] args) {
		this("exception.unauthenticatedUserException", args);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 */
	public UnauthenticatedUserException(String messagekey, Object[] args) {
		this(messagekey, args, null);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 * @param ex
	 */
	public UnauthenticatedUserException(String messagekey, Object[] args, Throwable ex) {
		super(STATUS, ResourceBundleAccessor.accessMessageInBundle(messagekey, args), ex);
	}

	/**
	 * 
	 */
	@Override
	public String getErrorCode() {
		return ErrorCode.UNAUTHENTICATED_USER_ERR_CODE.getErrCode();
	}

	/**
	 * 
	 */
	@Override
	public String getErrorMessage() {
		String msgKey = ErrorCode.UNAUTHENTICATED_USER_ERR_CODE.getErrMsgKey();
		return ResourceBundleAccessor.accessMessageInBundle(msgKey, new Object[] {});
	}

}
