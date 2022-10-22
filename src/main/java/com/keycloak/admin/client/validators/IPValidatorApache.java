/**
 * 
 */
package com.keycloak.admin.client.validators;

import org.apache.commons.validator.routines.InetAddressValidator;

/**
 * @author Gbenga
 *
 */
public class IPValidatorApache {

	private static final InetAddressValidator validator = InetAddressValidator.getInstance();

	public static boolean isValid(final String ip) {

		// only IPv4
		// return validator.isValidInet4Address(ip);

		// IPv4 + IPv6
		return validator.isValid(ip);

		// IPv6 only
		// return validator.isValidInet6Address(ip);
	}

}
