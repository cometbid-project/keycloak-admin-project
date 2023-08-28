/**
 * 
 */
package com.keycloak.admin.client.test.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 *
 * @author Gbenga
 */
@Getter
@Configuration
@PropertySource(value = { "classpath:keycloak-test.properties" })
public class KeycloakProperties {

	@Value("${keycloak.admin-client.client_id}")
	private String adminClientId;

	@Value("${keycloak.admin-client.client_secret}")
	private String adminClientSecret;
	/*
	@Value("${keycloak.admin.username}")
	private String adminUsername;
	*/
	
	@Value("${keycloak.ssl-enabled}")
	private boolean keycloakSslEnabled;	

	@Value("${keycloak.credentials.clientA_id}")
	private String clientId;

	@Value("${keycloak.credentials.clientA_secret}")
	private String clientSecret;

	//@Value("${keycloak.auth_server_url}")
	//private String authServerUrl;

	@Value("${keycloak.realm}")
	private String appRealm;

	//@Value("${keycloak.authorization_grant_type}")
	//private String authGrantType;
	
	@Value("${keycloak.client.requestTimeoutInMillis:100}")
	private Integer requestTimeoutInMillis;

	@Value("${keycloak.client.connection.writeTimeoutInMillis:100}")
	private Integer writeTimeoutInMillis;

	@Value("${keycloak.client.connection.readtimeoutInMillis:100}")
	private Integer readTimeoutInMillis;

	@Value("${keycloak.client.connection.timeoutInMillis:100}")
	private Integer connectTimeoutInMillis;

	@Value("${keycloak.client.connection.ttlInSeconds:2}")
	private Integer connectTTLInSeconds;

	@Value("${keycloak.client.connection.checkoutTimeoutInMillis:100}")
	private Integer connectCheckoutTimeoutInMillis;

	// ----------------------------------------------------------------
	@Value("${keycloak.client.waitTimeInMillis:500}")
	private Integer threadAwaitTimeInMillis;

	@Value("${keycloak.client.threadPoolSize:20}")
	private Integer threadPoolSize;
	
	@Value("${keycloak.client.connectionPoolSize:10}")
	private Integer connectionPoolSize;
	// ----------------------------------------------------------------

	@Value("${keycloak.auth.authorize-url-pattern}")
	private String authorizeUrlPattern;

	//@Value("${server.auth.redirectUrl}")
	//private String redirectUrl;

	@Value("${keycloak.user-info-uri}")
	private String keycloakUserInfo;

	@Value("${keycloak.logout-uri}")
	private String keycloakLogout;

	@Value("${keycloak.token-uri}")
	private String tokenUrl;
	
	@Value("${keycloak.scope}")
	private String scope;

	@Value("${keycloak.revoke-token-uri}")
	private String revokeTokenUrl;

	@Value("${keycloak.base-uri}")
	public String baseUrl;
	
}
