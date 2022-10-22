/**
 * 
 */
package com.keycloak.admin.client.oauth.service;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.keycloak.admin.client.error.helpers.ErrorPublisher;
import com.keycloak.admin.client.models.AuthTokenResponse;

import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Component
public class AuthorizationServerRemoteClient {

	private final WebClient webClient;

	/**
	 * @param webClientBuilder
	 * @param request
	 */
	public AuthorizationServerRemoteClient(@Qualifier("keycloakClient") WebClient webClient) {
		this.webClient = webClient;
	}

	/**
	 * 
	 * @param url
	 * @param formParams
	 * @param sessionId
	 * @return
	 */
	public Mono<ClientResponse> post(String url, MultiValueMap<String, String> formParams, String sessionId) {

		return webClient.method(HttpMethod.POST).uri(url).body(BodyInserters.fromFormData(formParams))
				.cookie("AUTH_SESSION_ID", sessionId).accept(MediaType.APPLICATION_JSON)
				.acceptCharset(StandardCharsets.UTF_8).exchangeToMono(response -> Mono.fromSupplier(() -> response));
	}

	/**
	 *
	 * @param url
	 * @return
	 */
	public Mono<AuthTokenResponse> get(String url) {

		log.info("Making Remote Get Request...");

		Mono<AuthTokenResponse> responseSpec = webClient.get().uri(url).accept(MediaType.APPLICATION_JSON)
				.acceptCharset(StandardCharsets.UTF_8).exchangeToMono(response -> processResponse(response));

		return responseSpec;
	}

	private Mono<AuthTokenResponse> processResponse(ClientResponse response) {
		HttpStatus status = response.statusCode();

		Mono<AuthTokenResponse> respObj = Mono.empty();

		if (status.is2xxSuccessful()) {
			Mono<String> responseStr = response.bodyToMono(String.class);
			// AuthTokenResponse resp = new AuthTokenResponse();

			respObj = responseStr.map(body -> {
				log.info("Auth Url Response: {}", body);

				String sessionId = null;
				if (StringUtils.isNotBlank(body)) {
					ResponseCookie cookie = response.cookies().getFirst("AUTH_SESSION_ID");
					if (cookie != null) {
						sessionId = cookie.getValue();
						//resp.setSessionId(sessionId);
					}
					//resp.setBody(body);
				}

				return new AuthTokenResponse(body, sessionId);
			});
		} else if (status.isError()) {
			String errorMsgKey = null;
			if (status.is4xxClientError()) {
				log.error("Client Error occurred while processing request");
				errorMsgKey = "client.error";
			} else if (status.is5xxServerError()) {
				log.error("Server Error occurred while processing request");
				errorMsgKey = "server.error";
			}

			ErrorPublisher.raiseBadCredentials(errorMsgKey,
					new Object[] { status.toString(), status.getReasonPhrase() });
		}

		return respObj;
	}

}
