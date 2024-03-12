/**
 * 
 */
package com.keycloak.admin.client.validators;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.keycloak.admin.client.common.utils.ResourceBundleAccessor;
import com.keycloak.admin.client.exceptions.ErrorCode;
import com.keycloak.admin.client.response.model.ApiError;
import com.keycloak.admin.client.response.model.AppResponse;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Log4j2
public abstract class AbstractValidationHandler<T, U extends Validator> {

	private final Class<T> validationClass;

	private final U validator;

	@Value("${app.sendreport.uri}")
	private String sendReportUri;

	@Value("${app.api.version}")
	private String currentApiVersion;

	@Value("${app.moreinfo.url}")
	private String moreInfoUrl;

	protected AbstractValidationHandler(Class<T> clazz, U validator) {
		this.validationClass = clazz;
		this.validator = validator;
	}

	abstract protected Mono<ServerResponse> processBody(T validBody, final ServerRequest originalRequest);

	public final Mono<ServerResponse> handleRequest(final ServerRequest request) {
		log.info("Validating Request Payload executing...");

		return request.bodyToMono(this.validationClass).flatMap(body -> {
			log.debug("Request Body: {}", body);

			Errors errors = new BeanPropertyBindingResult(body, this.validationClass.getName());
			this.validator.validate(body, errors);

			if (errors == null || errors.getAllErrors().isEmpty()) {
				return processBody(body, request);
			} else {
				return onValidationErrors(errors, body, request);
			}
		});
	}

	protected Mono<ServerResponse> onValidationErrors(Errors errors, T invalidBody, final ServerRequest request) {
		final String httpMethod = request.exchange().getRequest().getMethod().name();

		ApiError customError = new ApiError(request.path(), httpMethod,
				ErrorCode.CONSTRAINT_VIOLATION_ERR_CODE.getErrCode(), HttpStatus.UNPROCESSABLE_ENTITY,
				getErrorMessage("validation.error.debugMessage"),
				getErrorMessage(ErrorCode.CONSTRAINT_VIOLATION_ERR_CODE.getErrMsgKey()));

		for (FieldError error : errors.getFieldErrors()) {
			customError.addValidationError(error);
		}
		for (ObjectError error : errors.getGlobalErrors()) {
			customError.addValidationError(error);
		}

		AppResponse appError = new AppResponse(currentApiVersion, getErrorMessage("requestValue.constraint.violation"),
				customError, sendReportUri, HttpStatus.UNPROCESSABLE_ENTITY.name(), moreInfoUrl);

		return ServerResponse.badRequest().body(Mono.just(appError), AppResponse.class);
	}

	private String getErrorMessage(String messagekey) {

		return ResourceBundleAccessor.accessMessageInBundle(messagekey, new Object[] {});
	}

}
