/**
 * 
 */
package com.keycloak.admin.client.token.utils;

import org.springframework.security.core.Authentication;

/**
 * @author Gbenga
 *
 */
public interface TokenManager {
	
	String createJwtToken(Authentication authentication);
	
	Authentication getAuthentication(String token);
	
	boolean validateToken(String authToken);
 
	String getSubscriptionType(String token);

}
