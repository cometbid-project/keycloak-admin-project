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
public class ResourceAlreadyExistException extends ApplicationDefinedRuntimeException implements ErrorCode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8486744460448531453L;

	private static final HttpStatus STATUS = HttpStatus.CONFLICT;

	/**
	 *
	 */
	public ResourceAlreadyExistException() {
		this(new Object[] {});
	}

	/**
	 *
	 * @param args
	 */
	public ResourceAlreadyExistException(Object[] args) {
		this("exception.resourceAlreadyExist", args);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 */
	public ResourceAlreadyExistException(String messagekey, Object[] args) {
		this(messagekey, args, null);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 * @param ex
	 */
	public ResourceAlreadyExistException(String messagekey, Object[] args, Throwable ex) {
		super(STATUS, ResourceBundleAccessor.accessMessageInBundle(messagekey, args), ex);
	}

	/**
	 * 
	 */
	@Override
	public String getErrorCode() {
		return ErrorCode.RESOURCE_EXIST_ERR_CODE;
	}

}

