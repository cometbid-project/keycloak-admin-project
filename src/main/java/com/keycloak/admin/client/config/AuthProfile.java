/**
 * 
 */
package com.keycloak.admin.client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Gbenga
 *
 */
@Getter
@Log4j2
@Configuration
@PropertySource(value = "classpath:authProfile.properties")
public class AuthProfile {

	@Value("${auth.params.maximum_login_attempt}")
	private Long maximumLoginAttempt;

	@Value("${auth.params.blockedIP_expiration}")
	private Long blockedIpTTL;

	@Value("${auth.params.failed_login_expiration}")
	private Integer failedLoginExpirationPeriod;

	@Value("${auth.params.success_login_expiration}")
	private Integer refreshTokenSessionDuration;

	@Value("${auth.params.success_login_deletion}")
	private Integer loginSessionDuration;

	@Value("${auth.params.failed_login_deletion}")
	private Integer failedLoginDeletion;

	@Value(value = "${auth.params.max_password_history}")
	private Integer maximumPasswordHistory;

	@Value(value = "${auth.params.activation_token_expiration}")
	private Integer activationTokenExpirationPeriod;

	@Value(value = "${auth.params.activation_token_deletion}")
	private Integer activationTokenDeletionPeriod;

	@Value(value = "${auth.params.max_login_location_history}")
	private Integer maximumLoginLocationHistory;

	@Value(value = "${auth.params.password_token_expiration:24}")
	private Integer passwordResetTokenExpirationPeriod;

	@Value(value = "${auth.params.password_token_deletion:7}")
	private Integer passwordResetTokenDeletionPeriod;

	@Value("${auth.params.location_token_deletion:5}")
	public Integer newLocationTokenDeletionPeriod;
	
	@Value("${auth.params.password_expiration.period}")
	private Integer passwordExpirationPeriod;

	@Value("${auth.params.password_expiration_batchSize:10}")
	public Integer passwordExpirationBatchSize;

}
