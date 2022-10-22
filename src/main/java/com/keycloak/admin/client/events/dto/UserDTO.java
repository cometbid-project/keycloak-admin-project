/**
 * 
 */
package com.keycloak.admin.client.events.dto;

import java.util.Locale;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Gbenga
 *
 */
@Builder
@Setter
@Getter
@ToString
public class UserDTO {

	private String userId;

	private String sessionId;

	private String name;

	private String email;

	private String phoneNo;

	private String password;

	private String socialProvider;

	private String username;
	
	private String token;

	private String ip;

	private String deviceDetails;

	private String userAgent;

	private String location;
	
	private Locale locale;
}
