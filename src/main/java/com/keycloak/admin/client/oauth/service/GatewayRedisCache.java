/**
 * 
 */
package com.keycloak.admin.client.oauth.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import static com.keycloak.admin.client.error.helpers.ErrorPublisher.*;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import com.keycloak.admin.client.models.AuthenticationResponse;
import com.keycloak.admin.client.redis.service.ReactiveRedisComponent;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 * 
 *         Helper class handling runtime dynamic evaluation of User Permissions
 * 
 */
@Log4j2
@Service
public class GatewayRedisCache {

	private final Environment environment;
	private Long totpSessionTTL;
	private Long passwordResetSessionTTL;
	private Long pricingPlanTTL;
	private Long blockedIpTTL;

	private final ReactiveRedisComponent<String> cacheUtil;
	//private final static String BLOCKED_IPADDRESSES = "blocked_ips";

	public GatewayRedisCache(@Qualifier("redis") ReactiveRedisComponent<String> cacheUtil, Environment environment) {
		this.cacheUtil = cacheUtil;
		this.environment = environment;
		
		init();
	}

	// @PostConstruct
	public void init() {

		String configTotpTimeout = environment.getProperty("totp.session.ttl");
		this.totpSessionTTL = StringUtils.isNotBlank(configTotpTimeout) ? Long.valueOf(configTotpTimeout) : 1800L;
		log.info("Totp Session Time to live {}", totpSessionTTL);

		String configPasswdResetTimeout = environment.getProperty("password.reset.session.ttl");
		this.passwordResetSessionTTL = StringUtils.isNotBlank(configPasswdResetTimeout)
				? Long.valueOf(configPasswdResetTimeout)
				: 1800L;
		log.info("Password reset Session Time to live {}", passwordResetSessionTTL);

		String configPricingPlanTokenTimeout = environment.getProperty("pricing.plan.token.ttl");
		this.pricingPlanTTL = StringUtils.isNotBlank(configPricingPlanTokenTimeout)
				? Long.valueOf(configPricingPlanTokenTimeout)
				: 1800L;
		log.info("Pricing plan Time to live {}", pricingPlanTTL);
		
		String configBlockedIpTimeout = environment.getProperty("blocked.ips.ttl");
		this.blockedIpTTL = StringUtils.isNotBlank(configBlockedIpTimeout)
				? Long.valueOf(configBlockedIpTimeout)
				: 86400L;
		log.info("Blocked IPs Time to live {}", blockedIpTTL);
	}

	/**
	 * 
	 * @return
	 */
	public Mono<Long> saveAsBlockedIp(String ipAddr) {

		return cacheUtil.putIfAbsent(ipAddr, String.valueOf(0), blockedIpTTL)
				.then(cacheUtil.incrementThis(ipAddr));
		//return cacheUtil.incrementThis(ipAddr);
	}

	/**
	 * 
	 * @return
	 */
	public Mono<Boolean> isBlockedIp(String ipAddr) {

		return cacheUtil.getPojo(ipAddr)
				.flatMap(obj -> Mono.just(Optional.of(obj)))
				.defaultIfEmpty(Optional.empty())
				.map(valueOptional -> valueOptional.isPresent()).onErrorReturn(false);
	}

	/**
	 * 
	 * @param cacheKey
	 * @return
	 */
	public Mono<Long> incrementFailedLogin(String cacheKey) {

		return cacheUtil.putIfAbsent(cacheKey, String.valueOf(0)).then(cacheUtil.incrementThis(cacheKey));
	}

	/**
	 * 
	 * @param resetPasswordSessionId
	 * @param username
	 * @return
	 */
	public Mono<Boolean> savePasswordResetSession(String resetPasswordSessionId, String username) {

		return cacheUtil.putIfAbsent(resetPasswordSessionId, username, passwordResetSessionTTL);
	}

	/**
	 * 
	 * @param resetPasswordSessionId
	 * @param username
	 * @return
	 */
	public Mono<Boolean> deletePasswordResetSession(String resetPasswordSessionId) {

		return cacheUtil.deletePojo(resetPasswordSessionId);
	}

	/**
	 * 
	 * @param resetPasswordSessionId
	 * @return
	 */
	public Mono<String> getPasswordResetSession(String resetPasswordSessionId) {

		// TO DO
		return cacheUtil.getPojo(resetPasswordSessionId).flatMap(obj -> Mono.just(Optional.of(obj)))
				.defaultIfEmpty(Optional.empty()).flatMap(valueOptional -> {
					if (valueOptional.isPresent()) {
						String authResponse = (String) valueOptional.get();

						return Mono.just(authResponse);
					}

					return raiseResetPasswordSessionExpiredError("reset.password.session.notfound", new Object[] {});
				});
	}

	/**
	 * 
	 * @param cacheKey
	 * @return
	 */
	public Mono<Boolean> removeCacheEntry(String cacheKey) {
		// TODO Auto-generated method stub
		return cacheUtil.deletePojo(cacheKey);
	}

	/**
	 * 
	 * @param cacheKey
	 * @return
	 */
	public Mono<Boolean> removeBlockedIPEntry(String ipAddress) {
		// TODO Auto-generated method stub
		return cacheUtil.deletePojo(ipAddress);
	}

	/**
	 * 
	 * @param cacheKey
	 * @return
	 */
	/*
	public Mono<Long> purgeBlockedIPEntriesCache() {
		// TODO Auto-generated method stub
		return cacheUtil.deleteAll(BLOCKED_IPADDRESSES, getDefaultIPList());
	}
	*/
}
