/**
 * 
 */
package com.keycloak.admin.client.models;

import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.Value;

/**
 * @author Gbenga
 *
 */
@Value
@ToString
@Schema(name = "Username", description = "Representation of currently loggedin user")
public class Username {

	@Schema(name = "username", description = "User identifier")
	private String username;

	@Schema(name = "roles", description = "User specific application roles, comma separated")
	private String roles;

	public Username() {
		this(null, null);
	}
	
	public Username(String username) {
		this(username, null);
	}
	
	public Username(String username, String roles) {
		this.username = username;
		this.roles = roles;
	}

	public static Username createUser(User user) {
		Username userDet = null;
		if (Objects.nonNull(user)) {
			String username = user.getUsername();
			String roles = user.getAuthorities().parallelStream().map(role -> (GrantedAuthority) role)
					.map(a -> a.getAuthority()).collect(Collectors.joining(","));

			userDet = new Username(username, roles);
		} else {
			userDet = new Username();
		}

		return userDet;
	}
}
