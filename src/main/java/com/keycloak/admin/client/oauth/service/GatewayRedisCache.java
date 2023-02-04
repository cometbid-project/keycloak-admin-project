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
import com.keycloak.admin.client.models.TotpRequest;
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

	private final ReactiveRedisComponent cacheUtil;

	private final static String BLOCKED_IPADDRESSES = "blocked_ips";

	public GatewayRedisCache(@Qualifier("redis") ReactiveRedisComponent cacheUtil, Environment environment) {
		this.cacheUtil = cacheUtil;
		this.environment = environment;
		
		init();
	}

	//@PostConstruct
	public void init() {

		cacheUtil.append(BLOCKED_IPADDRESSES, getDefaultIPList());

		String configTotpTimeout = environment.getProperty("totp.session.ttl");
		this.totpSessionTTL = StringUtils.isNotBlank(configTotpTimeout) ? Long.valueOf(configTotpTimeout) : 1800L;
		
		String configPasswdResetTimeout = environment.getProperty("password.reset.session.ttl");
		this.passwordResetSessionTTL = StringUtils.isNotBlank(configPasswdResetTimeout) ? Long.valueOf(configPasswdResetTimeout) : 1800L;
		
		String configPricingPlanTokenTimeout = environment.getProperty("pricing.plan.token.ttl");
		this.pricingPlanTTL = StringUtils.isNotBlank(configPricingPlanTokenTimeout) ? Long.valueOf(configPricingPlanTokenTimeout) : 1800L;
	}
	
	private List<String> getDefaultIPList() {
		List<String> list = new ArrayList<>();
		// add arbitrary IP Addr
		list.add("xxx.xxx.xxx.xxx");
		
		return list;
	}

	/**
	 * 
	 * @return
	 */
	public Mono<Long> saveAsBlockedIp(String ipAddr) {
		
		return cacheUtil.appendIfPresent(BLOCKED_IPADDRESSES, ipAddr);
	}

	/**
	 * 
	 * @param totpSessionId
	 * @param authResponse
	 * @return
	 */
	public Mono<Boolean> saveAuthenticationResponse(String totpSessionId, AuthenticationResponse authResponse) {

		return cacheUtil.putPojo(totpSessionId, authResponse, totpSessionTTL);
	}

	/**
	 * 
	 * @param totpSessionId
	 * @return
	 */
	public Mono<AuthenticationResponse> getAuthenticationResponse(String totpSessionId) {

		return cacheUtil.getPojo(totpSessionId).flatMap(obj -> Mono.just(Optional.of(obj)))
				.defaultIfEmpty(Optional.empty()).flatMap(valueOptional -> {
					if (valueOptional.isPresent()) {
						AuthenticationResponse authResponse = (AuthenticationResponse) valueOptional.get();

						return Mono.just(authResponse);
					}

					return Mono.just(new AuthenticationResponse());
				});
	}

	/**
	 * 
	 * @return
	 */
	public Mono<Boolean> isBlockedIp(String ipAddr) {

		return cacheUtil.get(BLOCKED_IPADDRESSES, ipAddr).flatMap(obj -> Mono.just(Optional.of(obj)))
				.defaultIfEmpty(Optional.empty()).map(valueOptional -> valueOptional.isPresent()).onErrorReturn(false);
	}

	/**
	 * 
	 * @param cacheKey
	 * @return
	 */
	public Mono<Long> incrementFailedLogin(String cacheKey) {

		return cacheUtil.putIfAbsent(cacheKey, Integer.valueOf(0)).then(cacheUtil.incrementThis(cacheKey));
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
	public Mono<Long> removeBlockedIPEntry(String ipAddress) {
		// TODO Auto-generated method stub
		return cacheUtil.delete(BLOCKED_IPADDRESSES, 1L, ipAddress); 
	}

	/**
	 * 
	 * @param cacheKey
	 * @return
	 */
	public Mono<Long> purgeBlockedIPEntriesCache() {
		// TODO Auto-generated method stub
		return cacheUtil.deleteAll(BLOCKED_IPADDRESSES, getDefaultIPList()); 
	}

}
