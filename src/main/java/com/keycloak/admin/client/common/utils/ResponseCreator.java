/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.keycloak.admin.client.common.utils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;

import com.keycloak.admin.client.common.enums.ResponseType;
import com.keycloak.admin.client.models.ObjectWithList;
import com.keycloak.admin.client.response.model.ApiError;
import com.keycloak.admin.client.response.model.ApiMessage;
import com.keycloak.admin.client.response.model.AppResponse;

import lombok.extern.log4j.Log4j2;

/**
 *
 * @author Gbenga
 */
@Log4j2
@Component
public class ResponseCreator {

	@Value("${api.common.version}")
	private String apiVersion;

	@Value("${api.common.documentation}")
	private String apiInfoUrl;

	@Value("${api.common.help}")
	private String sendReportUri;

	/**
	 * 
	 * @param <T>
	 * @param profiles
	 * @param type
	 * @param responseCookie
	 * @param headerFields
	 * @param r
	 * @return
	 */
	public <T> Mono<ServerResponse> defaultReadMultiAuthResponse(Flux<T> profiles, Class<? extends T> type,
			Map<String, List<String>> headerFields, ServerRequest r) {

		List<MediaType> mediaType = r.exchange().getRequest().getHeaders().getAccept();
		log.info("Response MediaType {}", mediaType);

		ServerResponse.BodyBuilder responseBuilder = ServerResponse.status(HttpStatus.FOUND);

		if (mediaType.contains(MediaType.APPLICATION_XML)) {
			Mono<List<T>> monoList = profiles.collectList();

			@SuppressWarnings("unchecked")
			Mono<ObjectWithList<T>> xmlResult = monoList.map(list -> {
				ObjectWithList<T> authList = new ObjectWithList<T>();
				authList.setList(list);

				ObjectWithList<T> output = XmlStreamConverterUtil.fromListModeltoXml(authList, ObjectWithList.class,
						type);

				return output;
			});

			return responseBuilder.headers(headers -> {
				if (headerFields != null) {
					headers.putAll(headerFields);
				}
			}).contentType(MediaType.APPLICATION_XML).body(xmlResult, ObjectWithList.class);
		} else {

			return responseBuilder.headers(headers -> {
				if (headerFields != null) {
					headers.putAll(headerFields);
				}
			}).contentType(MediaType.APPLICATION_JSON).body(profiles, type);
		}
	}

	/**
	 * 
	 * @param <T>
	 * @param profiles
	 * @param type
	 * @param responseCookie
	 * @param headerFields
	 * @param r
	 * @return
	 */
	public <T> Mono<ServerResponse> defaultReadResponse(Mono<T> profiles, Class<? extends T> type,
			Map<String, List<String>> headerFields, ServerRequest r) {

		List<MediaType> mediaType = r.exchange().getRequest().getHeaders().getAccept();
		log.info("Response MediaType {}", mediaType);

		ServerResponse.BodyBuilder responseBuilder = ServerResponse.status(HttpStatus.FOUND);

		if (mediaType.contains(MediaType.APPLICATION_XML)) {

			Mono<String> xmlResult = profiles
					.flatMap(m -> Mono.justOrEmpty(XmlStreamConverterUtil.fromModeltoXml(m, type)));

			return responseBuilder.headers(headers -> {
				if (headerFields != null) {
					headers.putAll(headerFields);
				}
			}).contentType(MediaType.APPLICATION_XML).body(xmlResult, String.class);
		} else {

			return responseBuilder.headers(headers -> {
				if (headerFields != null) {
					headers.putAll(headerFields);
				}
			}).contentType(MediaType.APPLICATION_JSON).body(profiles, type);

		}
	}

	/**
	 * 
	 * @param <T>
	 * @param profiles
	 * @param type
	 * @param responseCookie
	 * @param headerFields
	 * @param r
	 * @return
	 */
	public <T> Mono<ServerResponse> defaultAcceptedReadResponse(Mono<T> profiles, Class<? extends T> type,
			Map<String, List<String>> headerFields, ServerRequest r) {

		List<MediaType> mediaType = r.exchange().getRequest().getHeaders().getAccept();
		log.info("Response MediaType {}", mediaType);

		ServerResponse.BodyBuilder responseBuilder = ServerResponse.accepted();

		if (mediaType.contains(MediaType.APPLICATION_XML)) {

			Mono<String> xmlResult = profiles
					.flatMap(m -> Mono.justOrEmpty(XmlStreamConverterUtil.fromModeltoXml(m, type)));

			return responseBuilder.headers(headers -> {
				if (headerFields != null) {
					headers.putAll(headerFields);
				}
			}).contentType(MediaType.APPLICATION_XML).body(xmlResult, String.class);
		} else {

			return responseBuilder.headers(headers -> {
				if (headerFields != null) {
					headers.putAll(headerFields);
				}
			}).contentType(MediaType.APPLICATION_JSON).body(profiles, type);

		}
	}

	/**
	 * 
	 * @param <T>
	 * @param profiles
	 * @param type
	 * @param responseCookie
	 * @param headerFields
	 * @param r
	 * @return
	 */
	public <T> Mono<ServerResponse> defaultWriteResponse(Mono<T> profiles, Class<? extends T> type, String identifier,
			Map<String, List<String>> headerFields, ServerRequest r) {

		List<MediaType> mediaType = r.exchange().getRequest().getHeaders().getAccept();
		log.info("Response MediaType {}", mediaType);

		URI uri = UriComponentsBuilder.fromPath(r.path() + "/{username}").buildAndExpand(identifier).toUri();
		ServerResponse.BodyBuilder responseBuilder = ServerResponse.created(uri);

		if (mediaType.contains(MediaType.APPLICATION_XML)) {

			Mono<String> xmlResult = profiles
					.flatMap(m -> Mono.justOrEmpty(XmlStreamConverterUtil.fromModeltoXml(m, type)));

			return responseBuilder.headers(headers -> {
				if (headerFields != null) {
					headers.putAll(headerFields);
				}
			}).contentType(MediaType.APPLICATION_XML).body(xmlResult, String.class);
		} else {

			return responseBuilder.headers(headers -> {
				if (headerFields != null) {
					headers.putAll(headerFields);
				}
			}).contentType(MediaType.APPLICATION_JSON).body(profiles, type);
		}
	}

	/**
	 * 
	 * @param message
	 * @param responseCookie
	 * @param headerFields
	 * @param r
	 * @return
	 */
	public Mono<ServerResponse> createAcceptedMessageResponse(String message, Map<String, List<String>> headerFields,
			ServerRequest r) {

		List<MediaType> mediaType = r.exchange().getRequest().getHeaders().getAccept();
		log.info("Response MediaType {}", mediaType);

		ApiMessage apiMessage = ApiMessage.builder().httpStatus(HttpStatus.ACCEPTED).message(message).path(r.path())
				.debugMessage(getMessage("accepted.detail.message"))
				.timestamp(ZonedDateTime.of(LocalDateTime.now(), ZoneOffset.UTC).toString())
				.responseTyp(ResponseType.SUCCESS).build();
		apiMessage.add(path(r), ResponseType.SUCCESS.toString(), getMessage("email.notification.step"));

		AppResponse appResponse = new AppResponse(apiVersion, "", apiMessage, HttpStatus.ACCEPTED.toString(),
				apiInfoUrl);

		ServerResponse.BodyBuilder responseBuilder = ServerResponse.accepted();

		if (mediaType.contains(MediaType.APPLICATION_XML)) {
			Mono<String> xmlResult = Mono.just(appResponse)
					.flatMap(m -> Mono.justOrEmpty(XmlStreamConverterUtil.fromModeltoXml(m, AppResponse.class)));

			return responseBuilder.headers(headers -> {
				if (headerFields != null) {
					headers.putAll(headerFields);
				}
			}).contentType(MediaType.APPLICATION_XML).body(xmlResult, String.class);
		} else {
			return responseBuilder.headers(headers -> {
				if (headerFields != null) {
					headers.putAll(headerFields);
				}
			}).contentType(MediaType.APPLICATION_JSON).body(Mono.just(appResponse), AppResponse.class);
		}
	}

	/**
	 * 
	 * @param message
	 * @param responseCookie
	 * @param headerFields
	 * @param r
	 * @return
	 */
	public Mono<ServerResponse> createSuccessMessageResponse(String message, ResponseCookie responseCookie,
			Map<String, List<String>> headerFields, ServerRequest r) {

		List<MediaType> mediaType = r.exchange().getRequest().getHeaders().getAccept();
		log.info("Response MediaType {}", mediaType);

		ApiMessage apiMessage = ApiMessage.builder().httpStatus(HttpStatus.OK).message(message).path(r.path())
				.timestamp(ZonedDateTime.of(LocalDateTime.now(), ZoneOffset.UTC).toString())
				.debugMessage(getMessage("success.detail.message")).responseTyp(ResponseType.SUCCESS).build();

		apiMessage.add(path(r), ResponseType.SUCCESS.toString(), getMessage("email.notification.step"));

		AppResponse appResponse = new AppResponse(apiVersion, "", apiMessage, HttpStatus.OK.toString(), apiInfoUrl);

		ServerResponse.BodyBuilder responseBuilder = ServerResponse.ok().cookies(cookie -> {
			if (responseCookie != null) {
				cookie.add(responseCookie.getName(), responseCookie);
			}
		});

		if (mediaType.contains(MediaType.APPLICATION_XML)) {
			Mono<String> xmlResult = Mono.just(appResponse)
					.flatMap(m -> Mono.justOrEmpty(XmlStreamConverterUtil.fromModeltoXml(m, AppResponse.class)));

			return responseBuilder.headers(headers -> {
				if (headerFields != null) {
					headers.putAll(headerFields);
				}
			}).contentType(MediaType.APPLICATION_XML).body(xmlResult, String.class);
		} else {
			return responseBuilder.headers(headers -> {
				if (headerFields != null) {
					headers.putAll(headerFields);
				}
			}).contentType(MediaType.APPLICATION_JSON).body(Mono.just(appResponse), AppResponse.class);
		}
	}

	/**
	 * 
	 * @param message
	 * @param httpStatus
	 * @param responseCookie
	 * @param headerFields
	 * @param r
	 * @return
	 */
	public Mono<ServerResponse> createErrorMessageResponse(String message, HttpStatus httpStatus,
			ResponseCookie responseCookie, Map<String, List<String>> headerFields, ServerRequest r) {

		List<MediaType> mediaType = r.exchange().getRequest().getHeaders().getAccept();
		log.info("Response MediaType {}", mediaType);

		ApiError apiError = ApiError.builder().status(httpStatus.value()).message(message).path(r.path())
				.debugMessage(getMessage("error.detail.message"))
				.timestamp(ZonedDateTime.of(LocalDateTime.now(), ZoneOffset.UTC).toString()).statusCode(httpStatus)
				.error(ResponseType.ERROR.toString()).reason(getMessage("error.short.message"))
				.errorCode(ResponseType.ERROR.getStatus()).path(path(r)).build();

		AppResponse appResponse = new AppResponse(apiVersion, "", apiError, sendReportUri, httpStatus.toString(),
				apiInfoUrl);

		ServerResponse.BodyBuilder responseBuilder = ServerResponse.status(httpStatus).cookies(cookie -> {
			if (responseCookie != null) {
				cookie.add(responseCookie.getName(), responseCookie);
			}
		});

		if (mediaType.contains(MediaType.APPLICATION_XML)) {
			Mono<String> xmlResult = Mono.just(appResponse)
					.flatMap(m -> Mono.justOrEmpty(XmlStreamConverterUtil.fromModeltoXml(m, AppResponse.class)));

			return responseBuilder.headers(headers -> {
				if (headerFields != null) {
					headers.putAll(headerFields);
				}
			}).contentType(MediaType.APPLICATION_XML).body(xmlResult, String.class);
		} else {
			return responseBuilder.headers(headers -> {
				if (headerFields != null) {
					headers.putAll(headerFields);
				}
			}).contentType(MediaType.APPLICATION_JSON).body(Mono.just(appResponse), AppResponse.class);
		}
	}

	/**
	 * 
	 * @param message
	 * @param httpStatus
	 * @param responseCookie
	 * @param headerFields
	 * @param r
	 * @return
	 */
	public AppResponse createAppResponse(String message, ServerRequest r) {

		ApiMessage apiMessage = ApiMessage.builder().httpStatus(HttpStatus.OK).message(message).path(r.path())
				.timestamp(ZonedDateTime.of(LocalDateTime.now(), ZoneOffset.UTC).toString())
				.debugMessage(getMessage("success.detail.message")).responseTyp(ResponseType.SUCCESS).build();

		apiMessage.add(path(r), ResponseType.SUCCESS.toString(), getMessage("email.notification.step"));

		return new AppResponse(apiVersion, "", apiMessage, HttpStatus.OK.toString(), apiInfoUrl);

	}

	private static String uri(ServerRequest r) {
		return UriComponentsBuilder.fromPath(r.path()).build().toUriString();
	}

	private static String path(ServerRequest r) {
		return r.exchange().getRequest().getPath().pathWithinApplication().value();
	}

	private static URI uri(ServerRequest r, String pathSegments, Object... uriVariables) {

		return UriComponentsBuilder.fromPath(r.path()).path(pathSegments).build(uriVariables);
	}

	private static String getMessage(String messagekey) {

		return ResourceBundleAccessor.accessMessageInBundle(messagekey, new Object[] {});
	}

	private static String getMessage(String messagekey, Object[] args) {

		return ResourceBundleAccessor.accessMessageInBundle(messagekey, args);
	}

}
