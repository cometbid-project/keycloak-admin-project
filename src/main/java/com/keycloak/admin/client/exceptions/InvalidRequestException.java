/**
 * 
 */
package com.keycloak.admin.client.exceptions;

import org.springframework.http.HttpStatus;

import com.keycloak.admin.client.common.utils.ResourceBundleAccessor;

import lombok.Getter;

/**
 * @author Gbenga
 *
 */
public class InvalidRequestException extends ApplicationDefinedRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6073988945101399813L;

	@Getter
	private int statusCode;

	public InvalidRequestException() {
		this(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
	}

	public InvalidRequestException(String message) {
		this(HttpStatus.BAD_REQUEST, message);
	}

	public InvalidRequestException(String message, Throwable cause) {
		this(HttpStatus.BAD_REQUEST, message, cause);
	}

	public InvalidRequestException(HttpStatus status, String message) {
		this(status, message, null);
	}

	public InvalidRequestException(HttpStatus status, Throwable cause) {
		this(status, status.getReasonPhrase(), cause);
	}

	public InvalidRequestException(HttpStatus status, String message, Throwable cause) {
		// TODO Auto-generated constructor stub
		super(message, cause);
		this.statusCode = status.value();
	}

	/**
	 * 
	 */
	@Override
	public String getErrorCode() {
		return ErrorCode.BAD_REQUEST_ERR_CODE.getErrCode();
	}

	/**
	 * 
	 */
	@Override
	public String getErrorMessage() {
		String msgKey = ErrorCode.BAD_REQUEST_ERR_CODE.getErrMsgKey();
		return ResourceBundleAccessor.accessMessageInBundle(msgKey, new Object[] {});
	}
}
