/**
 * 
 */
package com.keycloak.admin.client.components;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static java.util.stream.Collectors.toMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * @author Gbenga
 *
 */
@Component
@Log4j2
public class TraceIdFilter implements WebFilter {

	public static final String TRACE_ID_PREFIX = "X-B3-";
	public static final String TRACE_ID_KEY = "X-B3-TraceId";
	public static final String USER_CONTEXT_KEY = "X-Span-Export";
	public static final String CONTEXT_MAP = "context-map";

	public static final String BRANCH_CODE = "branch_code";
	public static final String RC_NO = "rc_no";

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

		exchange.getResponse().beforeCommit(() -> addContextToHttpResponseHeaders(exchange.getResponse()));

		return chain.filter(exchange).contextWrite(ctx -> addRequestHeadersToContext(exchange, ctx));

	}

	private Context addRequestHeadersToContext(final ServerWebExchange exchange, final Context context) {
		Map<String, String> headers = exchange.getRequest().getHeaders().toSingleValueMap();

		Map<String, String> mapEx = extractRequestContext(headers, exchange);

		return context.put(CONTEXT_MAP, mapEx);
	}

	private Mono<Void> addContextToHttpResponseHeaders(final ServerHttpResponse res) {

		return Mono.deferContextual(Mono::just).doOnNext(ctx -> {
			if (!ctx.hasKey(CONTEXT_MAP))
				return;

			final HttpHeaders headers = res.getHeaders();

			ctx.<Map<String, String>>get(CONTEXT_MAP)
					.forEach((key, value) -> headers.add(TRACE_ID_PREFIX + key.toUpperCase(), value));
		}).then();
	}

	private Map<String, String> extractRequestContext(Map<String, String> headers, ServerWebExchange exchange) {
		
		final Map<String, String> contextMap = headers.entrySet().stream()
				.filter(x -> StringUtils.startsWithIgnoreCase(x.getKey(), TRACE_ID_PREFIX))
				.collect(toMap(v -> v.getKey().substring(TRACE_ID_PREFIX.length()), Map.Entry::getValue));
				
		if (headers.containsKey(BRANCH_CODE) && headers.containsKey(RC_NO)) {
			String branchCode = headers.get(BRANCH_CODE);
			String rcNo = headers.get(RC_NO);

			contextMap.put(BRANCH_CODE, branchCode);  
			contextMap.put(RC_NO, rcNo);

			log.info("User context found...");
			String userContext = String.format("[rcNo => %s, branch => %s]", rcNo, branchCode);
			ThreadContext.put(USER_CONTEXT_KEY, userContext);
		} 

		String searchKey = TRACE_ID_KEY;
		if (headers.containsKey(searchKey)) {
			String traceId = headers.getOrDefault(searchKey, "");

			ThreadContext.put(TRACE_ID_KEY, traceId);
  
			log.info("Trace id found...");
			// simple hack to provide the context with the exchange, so the whole chain can
			// get the same trace id
			exchange.getAttributes().put(TRACE_ID_KEY, traceId);

		} else if (!exchange.getRequest().getURI().getPath().contains("/actuator")) {
			log.warn("TRACE_ID not present in header: {}", exchange.getRequest().getURI());
		}

		return contextMap;
	}

}
