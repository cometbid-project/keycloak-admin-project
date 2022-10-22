/**
 * 
 */
package com.keycloak.admin.client.filters;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import lombok.extern.log4j.Log4j2;

/**
 * @author Gbenga
 *
 */
@Log4j2
public class WebClientFilters {

	public static final String AUTH_PREFIX = "Bearer ";
	public static final String TRACE_ID_KEY = "X-B3-TraceId";

	/*
	 * 
	 */
	public static ExchangeFilterFunction authFilter(String jwtToken) {
		ExchangeFilterFunction filterFunction = (clientRequest, nextFilter) -> {
			clientRequest.headers().add("Authorization", AUTH_PREFIX + jwtToken);
			return nextFilter.exchange(clientRequest);
		};
		return filterFunction;
	}

	/**
	 * 
	 * @param getCounter
	 * @return
	 */
	public static ExchangeFilterFunction countingGetRequestFilter(AtomicInteger getCounter) {
		ExchangeFilterFunction countingFilter = (clientRequest, nextFilter) -> {
			HttpMethod httpMethod = clientRequest.method();
			if (httpMethod == HttpMethod.GET) {
				getCounter.incrementAndGet();
			}
			return nextFilter.exchange(clientRequest);
		};
		return countingFilter;
	}

	/**
	 * 
	 * @param printStream
	 * @return
	 */
	public static ExchangeFilterFunction tracingFilter() {
		ExchangeFilterFunction loggingFilter = (clientRequest, nextFilter) -> {
			clientRequest.headers().add(TRACE_ID_KEY, ThreadContext.get(TRACE_ID_KEY));
			return nextFilter.exchange(clientRequest);
		};
		return loggingFilter;
	}

}
