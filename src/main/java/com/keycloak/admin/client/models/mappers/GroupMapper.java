/**
 * 
 */
package com.keycloak.admin.client.models.mappers;

import org.keycloak.representations.idm.GroupRepresentation;
import com.keycloak.admin.client.models.GroupVO;

/**
 * @author Gbenga
 *
 */
public class GroupMapper {

	private GroupMapper() {
	}

	public static GroupVO toViewObject(GroupRepresentation group) {

		return GroupVO.builder().id(group.getId()).name(group.getName()).path(group.getPath())
				.attributes(group.getAttributes()).realmRoles(group.getRealmRoles()).clientRoles(group.getClientRoles())
				.build();
	}
}
