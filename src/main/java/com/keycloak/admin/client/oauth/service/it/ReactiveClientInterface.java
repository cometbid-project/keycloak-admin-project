/**
 * 
 */
package com.keycloak.admin.client.oauth.service.it;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

import io.netty.handler.codec.http.cookie.Cookie;
import lombok.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
public interface ReactiveClientInterface {

	/**
	 * 
	 * @param <T>
	 * @param uri
	 * @param params
	 * @param clazzResponse
	 * @return
	 */
	<T> Mono<T> performGetToMono(WebClient webClient, URI uri, Class<? extends T> clazzResponse,
			@NonNull Map<String, List<String>> headerFields, MultiValueMap<String, String> params);

	/**
	 * 
	 * @param <T>
	 * @param uri
	 * @param params
	 * @param clazzResponse
	 * @return
	 */
	<T> Flux<T> performGetToFlux(WebClient webClient, URI uri, Class<? extends T> clazzResponse,
			@NonNull Map<String, List<String>> headerFields, MultiValueMap<String, String> params);

	/**
	 * 
	 * @param <T>
	 * @param uri
	 * @param requestBody
	 * @param clazzResponse
	 * @return
	 */
	<T> Mono<T> performPostFormToMono(WebClient webClient, URI uri, MultiValueMap<String, String> formData,
			Class<? extends T> clazzResponse, @NonNull Map<String, List<String>> headerFields,
			MultiValueMap<String, String> params);

	/**
	 * 
	 * @param templateVar
	 * @param params
	 * @param path
	 * @param headerFields
	 * @param listOfCookies
	 * @return
	 */
	ResponseSpec doGet(WebClient webClient, Map<String, Object> templateVar, MultiValueMap<String, String> params,
			String path, Map<String, List<String>> headerFields, List<Cookie> listOfCookies);

	/**
	 * 
	 * @param templateVar
	 * @param formData
	 * @param path
	 * @param headerFields
	 * @param listOfCookies
	 * @param methodType
	 * @return
	 */
	ResponseSpec doFormDataPostOrPut(WebClient webClient, Map<String, Object> templateVar,
			MultiValueMap<String, String> formData, String path, Map<String, List<String>> headerFields,
			List<Cookie> listOfCookies, HttpMethod methodType);

	/**
	 * 
	 * @param templateVar
	 * @param parts
	 * @param path
	 * @param headerFields
	 * @param listOfCookies
	 * @param methodType
	 * @return
	 */
	ResponseSpec doMultipartPostOrPut(WebClient webClient, Map<String, Object> templateVar,
			MultiValueMap<String, HttpEntity<?>> parts, String path, Map<String, List<String>> headerFields,
			List<Cookie> listOfCookies, HttpMethod methodType);

	/**
	 * 
	 * @param templateVar
	 * @param path
	 * @param headerFields
	 * @param listOfCookies
	 * @return
	 */
	ResponseSpec doDelete(WebClient webClient, Map<String, Object> templateVar, String path,
			MultiValueMap<String, String> params, Map<String, List<String>> headerFields, List<Cookie> listOfCookies);

	/**
	 * 
	 * @param <T>
	 * @param uri
	 * @param requestBody
	 * @param clazzResponse
	 * @return
	 */
	<T> Mono<T> performPostToMono(WebClient webClient, URI uri, Object requestBody, Class<? extends T> clazzResponse,
			@NonNull Map<String, List<String>> headerFields, MultiValueMap<String, String> params);

	/**
	 * 
	 * @param <T>
	 * @param uri
	 * @param requestBody
	 * @param clazzResponse
	 * @return
	 */
	<T> Mono<T> performPutToMono(WebClient webClient, URI uri, Object requestBody, Class<? extends T> clazzResponse,
			@NonNull Map<String, List<String>> headerFields, MultiValueMap<String, String> params);

	/**
	 * 
	 * @param <T>
	 * @param uri
	 * @param params
	 * @param clazzResponse
	 * @return
	 */
	<T> Mono<T> performDeleteToMono(WebClient webClient, URI uri, Class<? extends T> clazzResponse,
			@NonNull Map<String, List<String>> headerFields, MultiValueMap<String, String> params);

	/**
	 * 
	 * @param <T>
	 * @param uri
	 * @param requestBody
	 * @param clazzResponse
	 * @return
	 */
	<T, R> Mono<R> performPublisherPost(WebClient webClient, URI uri, Mono<T> requestBody,
			Class<? extends T> clazzRequest, Class<? extends R> clazzResponse,
			@NonNull Map<String, List<String>> headerFields, MultiValueMap<String, String> params);

	/**
	 * 
	 * @param <T>
	 * @param uri
	 * @param requestBody
	 * @param clazzResponse
	 * @return
	 */
	<T, R> Mono<R> performPublisherPut(WebClient webClient, URI uri, Mono<T> requestBody,
			Class<? extends T> clazzRequest, Class<? extends R> clazzResponse,
			@NonNull Map<String, List<String>> headerFields, MultiValueMap<String, String> params);

	/**
	 * 
	 * @param templateVar
	 * @param data
	 * @param path
	 * @param headerFields
	 * @param listOfCookies
	 * @param methodType
	 * @return
	 */
	ResponseSpec doPostOrPutOrPatch(WebClient webClient, @NonNull Map<String, Object> templateVar, @NonNull Object data,
			@NonNull String path, @NonNull MultiValueMap<String, String> params,
			@NonNull Map<String, List<String>> headerFields, List<Cookie> listOfCookies,
			@NonNull HttpMethod methodType);

	/**
	 * 
	 * @param templateVar
	 * @param data
	 * @param path
	 * @param headerFields
	 * @param listOfCookies
	 * @param methodType
	 * @return
	 */
	<T> ResponseSpec doPublisherPostOrPutOrPatch(WebClient webClient, @NonNull Map<String, Object> templateVar,
			@NonNull Mono<T> monoData, Class<? extends T> clazzRequest, @NonNull String path,
			@NonNull MultiValueMap<String, String> params, @NonNull Map<String, List<String>> headerFields,
			List<Cookie> listOfCookies, @NonNull HttpMethod methodType);

	/**
	 * 
	 * @param <T>
	 * @param uri
	 * @param params
	 * @param clazzResponse
	 * @return
	 */
	<T> Flux<T> getWithUriTemplateToFlux(WebClient webClient, String uri, Class<? extends T> clazzResponse,
			Map<String, List<String>> headerFields, @NonNull Map<String, Object> uriVariables,
			Map<String, String> params);

	/**
	 * 
	 * @param <T>
	 * @param uri
	 * @param params
	 * @param clazzResponse
	 * @return
	 */
	<T> Mono<T> postWithUriTemplateToMono(WebClient webClient, String uri, Class<? extends T> clazzResponse,
			Map<String, List<String>> headerFields, @NonNull Map<String, Object> uriVariables,
			Map<String, String> params);

}
