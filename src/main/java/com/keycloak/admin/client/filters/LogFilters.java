/**
 * 
 */
package com.keycloak.admin.client.filters;

/**
 * @author Gbenga
 *
 */
import java.util.Arrays;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

@Log4j2
public class LogFilters {

	public static List<ExchangeFilterFunction> prepareFilters() {
		return Arrays.asList(logRequest(), logResponse());
	}

	public static ExchangeFilterFunction logRequest() {
		return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
			if (log.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder("Request: \n").append(clientRequest.method()).append(" ")
						.append(clientRequest.url());

				clientRequest.headers().forEach((name, values) -> values
						.forEach(value -> sb.append("\n").append(name).append(":").append(value)));
				
				log.debug(sb.toString());
			}
			return Mono.just(clientRequest);
		});
	}

	public static ExchangeFilterFunction logResponse() {
		return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
			if (log.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder("Response: \n").append("Status: ")
						.append(clientResponse.statusCode());
				
				clientResponse.headers().asHttpHeaders().forEach((key, value1) -> value1
						.forEach(value -> sb.append("\n").append(key).append(":").append(value)));
				
				log.debug(sb.toString());
			}
			return Mono.just(clientResponse);
		});
	}

	private void logHeader(String name, List<String> values) {
		values.forEach(value -> log.info("{}={}", name, value));
	}
}
