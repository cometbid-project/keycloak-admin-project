/**
 * 
 */
package com.keycloak.admin.client.token.utils;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import com.keycloak.admin.client.models.AuthenticationResponse;
import lombok.extern.log4j.Log4j2;
import org.keycloak.representations.AccessTokenResponse;

/**
 * @author Gbenga
 *
 */
@Log4j2
public class KeycloakJwtTokenUtil {

	/**
	 * 
	 * @param data
	 * @param roles
	 * @param username
	 * @return
	 */
	public static AuthenticationResponse generateLoginResponse(@NotBlank final AccessTokenResponse data,
			final List<String> roles, @NotBlank final String username) {
		log.info("Access Code response Data {}", data);

		String accessToken = data.getToken();
		String refreshToken = data.getRefreshToken();
		Long expiresIn = data.getExpiresIn();
		Long refreshExpiresIn = data.getRefreshExpiresIn();
		String scope = data.getScope();

		log.info("Access Token: {}", accessToken);
		log.info("Refresh Token: {}", refreshToken);
		log.info("Expires in: {}", expiresIn);
		log.info("Refresh Expires in: " + refreshExpiresIn);

		AuthenticationResponse authenticationResponse = AuthenticationResponse.builder().username(username).roles(roles)
				.accessToken(accessToken).refreshToken(refreshToken).expiresIn(expiresIn)
				.refreshExpiresIn(refreshExpiresIn).scope(scope)
				// .secret(secret)
				.build();

		return authenticationResponse;
	}

}
