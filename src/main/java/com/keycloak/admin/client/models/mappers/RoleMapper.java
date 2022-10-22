/**
 * 
 */
package com.keycloak.admin.client.models.mappers;

import org.keycloak.representations.idm.RoleRepresentation;
import com.keycloak.admin.client.models.RoleVO;

/**
 * @author Gbenga
 *
 */
public class RoleMapper {

	private RoleMapper() {
	}

	public static RoleVO toViewObject(RoleRepresentation group) {

		return RoleVO.builder().id(group.getId()).name(group.getName()).description(group.getDescription())
				.attributes(group.getAttributes()).build();
	}
}
