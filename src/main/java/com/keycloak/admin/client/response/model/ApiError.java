/**
 * 
 */
package com.keycloak.admin.client.response.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import javax.validation.ConstraintViolation;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.apache.logging.log4j.ThreadContext;
import org.hibernate.validator.internal.engine.path.PathImpl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Gbenga
 *
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
//@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.CUSTOM, property = "error", visible = true)
//@JsonTypeIdResolver(LowerCaseClassNameResolver.class)
public class ApiError extends ApiResponse implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 2097253529786466907L;

	@JsonProperty(value = "error")
	private String error;
	
	@JsonProperty("error_code")
	private String errorCode;

	@JsonProperty(value = "reason")
	private String reason;

	@JsonProperty(value = "path")
	private String path;

	@JsonProperty(value = "message")
	private String message;

	@JsonProperty(value = "status")
	private Integer status; // We'd need it as integer in JSON serialization

	@JsonIgnore
	private HttpStatus statusCode;
	
	@JsonProperty(value = "error_time")
	private String timestamp;

	@JsonProperty("trace_id")
	private String traceId;

	@JsonProperty(value = "debug_message")
	private String debugMessage;

	@JsonProperty(value = "error_details")
	private List<ApiSubError> subErrors;

	public ApiError() {
		this.timestamp = ZonedDateTime.of(LocalDateTime.now(), ZoneOffset.UTC).toString();
		this.traceId = ThreadContext.get("X-B3-TraceId");
	}

	public ApiError(String path, String errorCode, String reason, int code, String message, String detailMessage) {
		this();
		this.status = code;
		this.path = path;
		this.statusCode = HttpStatus.resolve(code);
		this.message = message;
		this.debugMessage = detailMessage;
		this.error = statusCode.getReasonPhrase();
		this.errorCode = errorCode;
		this.reason = reason;
	}

	public ApiError(String path, String errorCode, HttpStatus code, String message, String detailMessage) {
		this();
		this.status = code.value();
		this.path = path;
		this.statusCode = code;
		this.message = message;
		this.debugMessage = detailMessage;
		this.error = code.getReasonPhrase();
		this.errorCode = errorCode;
		this.reason = code.getReasonPhrase();
	}

	public ApiError(String path, String errorCode, HttpStatus statusCode, String message, Throwable ex) {
		this(path, errorCode, statusCode, message, ex.getLocalizedMessage());
	}

	ApiError(HttpStatus statusCode) {
		this();
		this.status = statusCode.value();
		this.statusCode = statusCode;
		this.reason = statusCode.getReasonPhrase();
	}

	ApiError(HttpStatus statusCode, String errorCode, Throwable ex) {
		this();
		this.status = statusCode.value();
		this.statusCode = statusCode;
		this.setMessage("Unexpected error");
		this.debugMessage = ex.getLocalizedMessage();
		this.errorCode = errorCode;
		this.reason = statusCode.getReasonPhrase();
	}

	ApiError(HttpStatus statusCode, String errorCode, String message, Throwable ex) {
		this();
		this.status = statusCode.value();
		this.statusCode = statusCode;
		this.setMessage(message);
		this.errorCode = errorCode;
		this.debugMessage = ex.getLocalizedMessage();
		this.reason = statusCode.getReasonPhrase();
	}

	private void addSubError(ApiSubError subError) {
		if (subErrors == null) {
			subErrors = new ArrayList<>();
		}
		subErrors.add(subError);
	}

	private void addValidationError(String object, String field, Object rejectedValue, String message) {
		addSubError(new ApiValidationError(object, field, rejectedValue, message));
	}

	private void addValidationError(String object, String message) {
		addSubError(new ApiValidationError(object, message));
	}

	public void addValidationError(FieldError fieldError) {
		this.addValidationError(fieldError.getObjectName(), fieldError.getField(), fieldError.getRejectedValue(),
				fieldError.getDefaultMessage());
	}

	public void addValidationErrors(List<FieldError> fieldErrors) {
		fieldErrors.forEach(this::addValidationError);
	}

	public void addValidationError(ObjectError objectError) {
		this.addValidationError(objectError.getObjectName(), objectError.getDefaultMessage());
	}

	public void addValidationError(List<ObjectError> globalErrors) {
		globalErrors.forEach(this::addValidationError);
	}

	/**
	 * Utility method for adding error of ConstraintViolation. Usually when
	 * a @Validated validation fails.
	 *
	 * @param cv the ConstraintViolation
	 */
	private void addValidationError(ConstraintViolation<?> cv) {
		this.addValidationError(cv.getRootBeanClass().getSimpleName(),
				((PathImpl) cv.getPropertyPath()).getLeafNode().asString(), cv.getInvalidValue(), cv.getMessage());
	}

	public void addValidationErrors(Set<ConstraintViolation<?>> constraintViolations) {
		constraintViolations.forEach(this::addValidationError);
	}

}
