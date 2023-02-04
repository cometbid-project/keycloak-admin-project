/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.keycloak.admin.client.response.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.keycloak.admin.client.common.enums.ResponseType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 *
 * @author Gbenga
 */
@ToString
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
//@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = false)
//@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.CUSTOM, property = "error", visible = true)
//@JsonTypeIdResolver(LowerCaseClassNameResolver.class)
public class ApiMessage extends ApiResponse implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 2097253529786466907L;

	@JsonProperty(value = "reason")
	private String reason;

	/**
	 * Url of request that produced the error.
	 */
	@Builder.Default
	@JsonProperty(value = "path")
	private String path = "Not available";

	/**
	 * Short, human-readable summary of the problem.
	 */
	@JsonProperty(value = "message")
	private final String message;

	/**
	 * Method of request that produced the error.
	 */
	@Builder.Default
	@JsonProperty(value = "method")
	private String reqMethod = "Not available";

	/**
	 * HTTP status code for this occurrence of the problem, set by the origin
	 * server.
	 */
	@JsonProperty(value = "status")
	private Integer status; // We'd need it as integer in JSON serialization

	@JsonIgnore
	private final HttpStatus httpStatus;

	@JsonProperty("trace_id")
	private String traceId;

	@JsonProperty(value = "detail_message")
	private String debugMessage;

	@JsonProperty(value = "timestamp")
	private final String timestamp;

	@Builder
	public ApiMessage(HttpStatus httpStatus, String reqMethod, String path, String message, String detailMessage) {
		this.timestamp = ZonedDateTime.of(LocalDateTime.now(), ZoneOffset.UTC).toString();
		this.traceId = ThreadContext.get("X-B3-TraceId");
		this.httpStatus = httpStatus;
		this.reqMethod = reqMethod;
		this.path = path;
		this.message = message;
		this.debugMessage = detailMessage;
		this.responses = new ArrayList<>();
	}

	@JsonCreator
	public ApiMessage(HttpStatus httpStatus, String reqMethod, String path, String message) {
		this.timestamp = ZonedDateTime.of(LocalDateTime.now(), ZoneOffset.UTC).toString();
		this.traceId = ThreadContext.get("X-B3-TraceId");
		this.httpStatus = httpStatus;
		this.reqMethod = reqMethod;
		this.path = path;
		this.message = message;
		this.responses = new ArrayList<>();
	}

	public String getTimestamp() {
		return timestamp;
	}

	public String getPath() {
		return path;
	}

	public int getStatus() {
		return httpStatus.value();
	}

	public String getReason() {
		return httpStatus.getReasonPhrase();
	}

	public String getMessage() {
		return message;
	}

	@JsonProperty("type")
	private ResponseType responseTyp;

	@Builder.Default
	@JsonProperty("responses")
	private List<Response> responses = new ArrayList<>();

	public void add(String path, String code, String message) {
		this.responses.add(new Response(path, code, message));
	}

	@ToString
	@Getter
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = false)
	public class Response implements Serializable {

		/**
		 *
		 */
		private static final long serialVersionUID = -6444882637188942761L;

		private String path;
		private String code;
		private String message;

		@JsonCreator
		Response(String path, String code, String message) {
			this.path = path;
			this.code = code;
			this.message = message;
		}

	}
}
