package com.keycloak.admin.client.exceptions;

import org.springframework.security.core.AuthenticationException;

/**
 * 
 * @author Gbenga
 *
 */
public class OAuth2AuthenticationProcessingException extends AuthenticationException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3392450042101522832L;

	public OAuth2AuthenticationProcessingException(String msg, Throwable t) {
		super(msg, t);
	}

	public OAuth2AuthenticationProcessingException(String msg) {
		super(msg);
	}
}