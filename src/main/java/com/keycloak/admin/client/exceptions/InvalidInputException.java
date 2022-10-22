/**
 * 
 */
package com.keycloak.admin.client.exceptions;

import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.keycloak.admin.client.common.utils.ResourceBundleAccessor;

/**
 * @author Gbenga
 *
 */
public class InvalidInputException extends ResponseStatusException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6157997962887448293L;

	private static final HttpStatus STATUS = HttpStatus.UNPROCESSABLE_ENTITY;

	/**
	 *
	 */
	public InvalidInputException(Locale locale) {
		this(new Object[] {});
	}

	/**
	 *
	 * @param args
	 */
	public InvalidInputException(Object[] args) {
		this("exception.invalidInput", args);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 */
	public InvalidInputException(String messagekey, Object[] args) {
		this(messagekey, args, null);
	}

	/**
	 *
	 * @param messagekey
	 * @param args
	 * @param ex
	 */
	public InvalidInputException(String messagekey, Object[] args, Throwable ex) {
		super(STATUS, ResourceBundleAccessor.accessMessageInBundle(messagekey, args), ex);
	}

	/**
	 * 
	 */
	public String getErrorCode() {
		return "INV-INPUT-001";
	}

}
