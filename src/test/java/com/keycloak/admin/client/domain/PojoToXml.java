/**
 * 
 */
package com.keycloak.admin.client.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections4.MapUtils;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.keycloak.admin.client.models.UserVO;

import lombok.extern.log4j.Log4j2;

import com.keycloak.admin.client.common.utils.XmlStreamConverterUtil;
import com.keycloak.admin.client.dataacess.UserBuilder;
import com.keycloak.admin.client.models.ObjectWithList;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Log4j2
class PojoToXml {

	@SuppressWarnings("unchecked")
	public <T> Mono<String> doXmlConversion1(Flux<T> profiles, Class<? extends T> type) {

		Mono<List<T>> monoList = profiles.collectList();

		return monoList.map(list -> {
			ObjectWithList<T> authList = new ObjectWithList<T>();
			authList.setList(list);

			String output = XmlStreamConverterUtil.fromListModeltoXml(authList, ObjectWithList.class, type);

			// System.out.println("Output " + output);
			return output;
		});
	}

	@SuppressWarnings("unchecked")
	public <T> Mono<ObjectWithList<T>> wrapToCollection(Flux<T> profiles, Class<? extends T> type) {

		Mono<List<T>> monoList = profiles.collectList();

		return monoList.map(list -> {
			ObjectWithList<T> authList = new ObjectWithList<T>();
			authList.setList(list);

			return authList;
		});
	}

	public <T> Mono<ServerResponse> defaultReadMultiAuthResponse(Flux<T> profiles, Class<? extends T> type,
			Map<String, List<String>> headerFields, ServerRequest r) {

		return buildMultiResponse(profiles, type, HttpStatus.FOUND, headerFields, r);
	}

	private <T> Mono<ServerResponse> buildMultiResponse(Flux<T> profiles, Class<? extends T> type, HttpStatus status,
			Map<String, List<String>> headerFields, ServerRequest r) {

		List<MediaType> mediaType = r.exchange().getRequest().getHeaders().getAccept();
		log.info("Response MediaType {}", mediaType);

		ServerResponse.BodyBuilder responseBuilder = ServerResponse.status(HttpStatus.FOUND).headers(headers -> {
			if (!MapUtils.isEmpty(headerFields)) {
				headers.putAll(headerFields);
			}
		});

		if (mediaType.contains(MediaType.APPLICATION_XML)) {
			Mono<ObjectWithList<T>> result = wrapToCollection(profiles, type);

			return responseBuilder.contentType(MediaType.APPLICATION_XML).body(result, ObjectWithList.class);
		} else {
			return responseBuilder.contentType(MediaType.APPLICATION_JSON).body(profiles, type);
		}
	}

	public <T> Mono<ServerResponse> defaultReadResponse(Mono<T> profiles, Class<? extends T> type,
			Map<String, List<String>> headerFields, ServerRequest r) {

		return buildSingleResponse(HttpStatus.FOUND, headerFields, r).body(profiles, type);
	}

	private ServerResponse.BodyBuilder buildSingleResponse(HttpStatus status, Map<String, List<String>> headerFields,
			ServerRequest r) {
		List<MediaType> mediaType = r.exchange().getRequest().getHeaders().getAccept();
		log.info("Response MediaType {}", mediaType);

		ServerResponse.BodyBuilder responseBuilder = ServerResponse.status(HttpStatus.FOUND).headers(headers -> {
			if (!MapUtils.isEmpty(headerFields)) {
				headers.putAll(headerFields);
			}
		});

		if (mediaType.contains(MediaType.APPLICATION_XML)) {
			responseBuilder.contentType(MediaType.APPLICATION_XML);
		} else {
			responseBuilder.contentType(MediaType.APPLICATION_JSON);
		}

		return responseBuilder;
	}

	@Test
	public <T> void testUserVO() {
		var user1 = UserBuilder.user().userVo(UUID.randomUUID());
		var user2 = UserBuilder.user().userVo(UUID.randomUUID());

		Flux<UserVO> profiles = Flux.just(user1, user2);
		doXmlConversion1(profiles, UserVO.class).subscribe(System.out::println);
	}

}
