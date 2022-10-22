/**
 * 
 */
package com.keycloak.admin.client.config;

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
@PropertySource(value = { "classpath:oauthClients-mtls.properties" })
public class AuthProperties {

	@Value("${keycloak.admin-client.client_id}")
	private String adminClientId;

	@Value("${keycloak.admin-client.client_secret}")
	private String adminClientSecret;

	@Value("${keycloak.admin.username}")
	private String adminUsername;

	@Value("${keycloak.admin.password}")
	private String adminPassword;

	@Value("${keycloak.credentials.client_id}")
	private String clientId;

	@Value("${keycloak.credentials.client_secret}")
	private String clientSecret;

	@Value("${keycloak.auth_server_url}")
	private String authServerUrl;

	@Value("${keycloak.realm}")
	private String appRealm;

	@Value("${keycloak.authorization_grant_type}")
	private String authGrantType;
	
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
	// ----------------------------------------------------------------

	@Value("${keycloak.auth.authorize-url-pattern}")
	private String authorizeUrlPattern;

	@Value("${server.auth.redirectUrl}")
	private String redirectUrl;

	@Value("${keycloak.logout-uri}")
	private String logoutUrl;

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

	// Add to property file
	@Value("${jwt.token.validityAgeInMins:10}")
	private Integer tokenValidityInMinutes;

	@Value("${jwt.refreshToken.expirationPeriodInMins:30}")
	private Integer refreshTokenValidityInMinutes;

	@Value("${jwt.refreshToken.cookieMaxAgeInMins:30}")
	private Integer refreshTokenCookieInMinutes;
	
	@Value("${keycloak.audience}")
	private String keycloakAudience;

	@Value("${jwt.token.audience}")
	private String localTokenAudience;

	@Value("${jwt.token.issuer}")
	private String issuer;

	public static final String ACCESS_TOKEN = "access_token";
	public static final String REFRESH_TOKEN = "refresh_token";
	public static final String REFRESH_TOKEN_COOKIE = "refreshToken";
	public static final String SESSION_ID_COOKIE = "sessionToken";
	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String HEADER_API_KEY = "x-api-key";
	public static final String BEARER = "Bearer ";
	public static final String SCOPES = "read write";

	public static final String GROUPS_CLAIM = "realm_access";
	public static final String ROLE_PREFIX = "ROLE_";
	public static final String REALM_ROLE_KEY = "roles";
	public static final String SUBJECT = "sub";

	public static final String AUTHORITIES_CLAIM = "auth";
	public static final String SUBSCRIPTION_CLAIM = "Subscription";
	public static final String TOTP_SECRET = "totp_secret";
	public static final String TOTP_ENABLED = "2fa_flag";

	public static final String PROFILE_LOCKED = "profile_locked";
	public static final String PROFILE_EXPIRED = "profile_expired";
	public static final String LAST_EXPIRYDATE = "last_expiration";
	public static final String LAST_MODIFIED_DATE = "last_modified_date";
	
	public static final String INVALID_TOKEN = "auth.message.invalidToken";
	public static final String EXPIRED_TOKEN = "auth.message.tokenExpired";

	public static final String COMPLETED = "Completed";
	public static final String FAILED = "Failure";
	public static final String SUCCESS = "Success";
	
}
