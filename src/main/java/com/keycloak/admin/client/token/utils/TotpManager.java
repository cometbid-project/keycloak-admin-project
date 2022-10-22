/**
 * 
 */
package com.keycloak.admin.client.token.utils;

/**
 * @author Gbenga
 *
 */
public interface TotpManager {

	String generateSecret();

	boolean validateCode(String code, String secret);

	String generateQrImage(String email, String secret);

	String generateOtp();

	String[] generateRecoveryCodes();

}
