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
public class BlockedCredentialsAttemptsLoginWarning extends ApplicationDefinedRuntimeException implements ErrorCode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9076188781838210202L;

	private static final HttpStatus STATUS = HttpStatus.FORBIDDEN;

	/**
	 *
	 */
	public BlockedCredentialsAttemptsLoginWarning() {
		this(new Object[] {});
	}

	/**
	 *
	 * @param args
	 */
	public BlockedCredentialsAttemptsLoginWarning(Object[] args) {
		this("warning.intrusive.alert", args);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 */
	public BlockedCredentialsAttemptsLoginWarning(String messagekey, Object[] args) {
		this(messagekey, args, null);
	}

	/**
	 *
	 * @param messagekey
	 * @param args  
	 * @param ex
	 */
	public BlockedCredentialsAttemptsLoginWarning(String messagekey, Object[] args, Throwable ex) {
		super(STATUS, ResourceBundleAccessor.accessMessageInBundle(messagekey, args), ex);
	}

	/**
	 * 
	 */
	@Override
	public String getErrorCode() {
		return ErrorCode.UNAVAILABLE_SERVICE_ERR_CODE;
	}

}
