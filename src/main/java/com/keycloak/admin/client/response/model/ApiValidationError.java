/**
 * 
 */
package com.keycloak.admin.client.response.model;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Gbenga
 *
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class ApiValidationError extends ApiSubError {

	private static final HttpStatus STATUS = HttpStatus.UNPROCESSABLE_ENTITY;

	@JsonProperty(value = "object")
	private String object;

	@JsonProperty(value = "field")
	// Name of the field. Null in case of a form level error.
	private String field;

	@JsonProperty(value = "rejected_value")
	private Object rejectedValue;

	@JsonProperty(value = "message")
	// Error message
	private String message;

	public ApiValidationError() {
		super(null, STATUS.toString(), STATUS.getReasonPhrase());
		this.field = null;
		this.rejectedValue = null;
		this.message = null;
	}

	@JsonCreator
	public ApiValidationError(String object, String field, Object rejectedValue, String message, String code,
			String detailedMessage) {
		super(object, code, detailedMessage);
		this.field = field;
		this.rejectedValue = rejectedValue;
		this.message = message;
	}

	public ApiValidationError(String object, String field, Object rejectedValue, String message) {
		// TODO Auto-generated constructor stub
		super(object, STATUS.toString(), STATUS.getReasonPhrase());
		this.field = field;
		this.rejectedValue = rejectedValue;
		this.message = message;
	}

	public ApiValidationError(String object, String message) {
		// TODO Auto-generated constructor stub
		super(object, STATUS.toString(), STATUS.getReasonPhrase());
		this.field = null;
		this.rejectedValue = null;
		this.message = message;
	}
}
