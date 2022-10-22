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
public class ActivationTokenValidationException extends ApplicationDefinedRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8102535045739100019L;
	private static final HttpStatus STATUS = HttpStatus.EXPECTATION_FAILED;

	/**
	 *
	 */
	public ActivationTokenValidationException() {
		this(new Object[] {});
	}

	/**
	 *
	 * @param args
	 */
	public ActivationTokenValidationException(Object[] args) {
		this("exception.invalidActivationToken", args);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 */
	public ActivationTokenValidationException(String messagekey, Object[] args) {
		this(messagekey, args, null);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 * @param ex
	 */
	public ActivationTokenValidationException(String messagekey, Object[] args, Throwable ex) {
		super(STATUS, ResourceBundleAccessor.accessMessageInBundle(messagekey, args), ex);
	}

	/**
	 * 
	 */
	@Override
	public String getErrorCode() {
		return ErrorCode.INVALID_ACTIVATION_TOKEN_ERR_CODE;
	}
}
