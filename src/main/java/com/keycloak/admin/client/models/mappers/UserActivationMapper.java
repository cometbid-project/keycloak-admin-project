/**
 * 
 */
package com.keycloak.admin.client.models.mappers;

import java.time.LocalDateTime;

import com.keycloak.admin.client.common.enums.StatusType;
import com.keycloak.admin.client.entities.ActivationToken;
import com.keycloak.admin.client.models.ActivationTokenModel;

/**
 * @author Gbenga
 *
 */
public class UserActivationMapper {

	private UserActivationMapper() {
	}

	public static ActivationToken create(ActivationTokenModel activationToken) {

		ActivationToken userAct = ActivationToken.builder().username(activationToken.getUsername())
				.token(activationToken.getToken()).status(StatusType.VALID.name()).creationDate(LocalDateTime.now())
				.build();

		return userAct;
	}

	public static ActivationTokenModel toViewObject(ActivationToken token) {

		return ActivationTokenModel.builder().username(token.getUsername()).token(token.getToken()).build();
	}

}
