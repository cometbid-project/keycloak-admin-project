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
public class InsufficientFundException extends ApplicationDefinedRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1835472710620887335L;
	private static final HttpStatus STATUS = HttpStatus.PRECONDITION_REQUIRED;

	/**
	 *
	 */
	public InsufficientFundException() {
		this(new Object[] {});
	}

	/**
	 *
	 * @param args
	 */
	public InsufficientFundException(Object[] args) {
		this("exception.insufficientFund", args);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 */
	public InsufficientFundException(String messagekey, Object[] args) {
		this(messagekey, args, null);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 * @param ex
	 */
	public InsufficientFundException(String messagekey, Object[] args, Throwable ex) {
		super(STATUS, ResourceBundleAccessor.accessMessageInBundle(messagekey, args), ex);
	}

	/**
	 * 
	 */
	@Override
	public String getErrorCode() {
		return ErrorCode.INSUFFICIENT_FUND_ERR_CODE.getErrCode();
	}

	/**
	 * 
	 */
	@Override
	public String getErrorMessage() {
		String msgKey = ErrorCode.INSUFFICIENT_FUND_ERR_CODE.getErrMsgKey();
		return ResourceBundleAccessor.accessMessageInBundle(msgKey, new Object[] {});
	}

}
