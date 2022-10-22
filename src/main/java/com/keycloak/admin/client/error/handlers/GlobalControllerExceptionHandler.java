/**
 * 
 */
package com.keycloak.admin.client.error.handlers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;

import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;
import org.keycloak.util.JsonSerialization;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import com.keycloak.admin.client.common.utils.YamlPropertySourceFactory;
import com.keycloak.admin.client.components.TraceIdFilter;
import com.keycloak.admin.client.exceptions.ActivationTokenValidationException;
import com.keycloak.admin.client.exceptions.AuthenticationError;
import com.keycloak.admin.client.exceptions.BadRequestException;
import com.keycloak.admin.client.exceptions.CustomConstraintViolationException;
import com.keycloak.admin.client.exceptions.ErrorCode;
import com.keycloak.admin.client.exceptions.InsufficientFundException;
import com.keycloak.admin.client.exceptions.InvalidRequestException;
import com.keycloak.admin.client.exceptions.PasswordNotAcceptableException;
import com.keycloak.admin.client.exceptions.ResourceNotFoundException;
import com.keycloak.admin.client.exceptions.UnusualLocationException;
import com.keycloak.admin.client.exceptions.UserProfileDisabledException;
import com.keycloak.admin.client.exceptions.UserProfileExpiredException;
import com.keycloak.admin.client.exceptions.UserProfileLockedException;
import com.keycloak.admin.client.exceptions.UserProfileUnverifiedException;
import com.keycloak.admin.client.response.model.ApiError;
import com.keycloak.admin.client.response.model.AppResponse;

import org.springframework.security.core.AuthenticationException;
import static org.springframework.http.HttpStatus.*;

/**
 * @author Gbenga
 *
 */
@Log4j2
@RestControllerAdvice
@Configuration
@PropertySource(value = "classpath:application.yml", factory = YamlPropertySourceFactory.class)
public class GlobalControllerExceptionHandler {

	@Value("${api.common.reportError}")
	private String sendReportUri;

	@Value("${api.common.version}")
	private String currentApiVersion;

	@Value("${api.common.help}")
	private String moreInfoUrl;

	private String errorCode = ErrorCode.SYS_DEFINED_ERR_CODE;

	/**
	 * 
	 * @param e
	 * @param ex
	 * @return
	 */
	@ResponseStatus(SERVICE_UNAVAILABLE)
	@ExceptionHandler(value = { Exception.class })
	public @ResponseBody AppResponse gottaCatchEmAll(Exception ex, ServerHttpRequest request) {

		return createHttpErrorInfo(INTERNAL_SERVER_ERROR, errorCode, request, null, ex);

		// return
		// Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage()));
	}

	/**
	 * 
	 * @param request
	 * @param ex
	 * @return
	 */
	@ResponseStatus(LOCKED)
	@ExceptionHandler({ UserProfileLockedException.class })
	public @ResponseBody AppResponse handleLockedProfileExceptions(ServerHttpRequest request, Exception ex) {

		if (ex instanceof UserProfileLockedException) {
			errorCode = ((UserProfileLockedException) ex).getErrorCode();
		}
		return createHttpErrorInfo(LOCKED, errorCode, request, null, ex);
	}

	/**
	 * 
	 * @param request
	 * @param ex
	 * @return
	 */
	@ResponseStatus(PRECONDITION_FAILED)
	@ExceptionHandler({ UserProfileUnverifiedException.class })
	public @ResponseBody AppResponse handleUnverifiedProfileExceptions(ServerHttpRequest request, Exception ex) {

		if (ex instanceof UserProfileUnverifiedException) {
			errorCode = ((UserProfileUnverifiedException) ex).getErrorCode();
		}
		return createHttpErrorInfo(HttpStatus.PRECONDITION_FAILED, errorCode, request, null, ex);
	}

	/**
	 * 
	 * @param request
	 * @param ex
	 * @return
	 */
	@ResponseStatus(FAILED_DEPENDENCY)
	@ExceptionHandler({ UserProfileDisabledException.class })
	public @ResponseBody AppResponse handleDisabledProfileExceptions(ServerHttpRequest request, Exception ex) {

		if (ex instanceof UserProfileDisabledException) {
			errorCode = ((UserProfileDisabledException) ex).getErrorCode();
		}
		return createHttpErrorInfo(HttpStatus.FAILED_DEPENDENCY, errorCode, request, null, ex);
	}

	/**
	 * 
	 * @param request
	 * @param ex
	 * @return
	 */
	@ResponseStatus(EXPECTATION_FAILED)
	@ExceptionHandler({ ActivationTokenValidationException.class, UnusualLocationException.class })
	public @ResponseBody AppResponse handleAuthenticationError(ServerHttpRequest request, Exception ex) {

		if (ex instanceof ActivationTokenValidationException) {
			errorCode = ((ActivationTokenValidationException) ex).getErrorCode();
		} else if (ex instanceof UnusualLocationException) {
			errorCode = ((UnusualLocationException) ex).getErrorCode();
		}
		return createHttpErrorInfo(EXPECTATION_FAILED, errorCode, request, null, ex);
	}

	/**
	 * 
	 * @param request
	 * @param ex
	 * @return
	 */
	@ResponseStatus(FORBIDDEN)
	@ExceptionHandler({ AccessDeniedException.class, AuthenticationException.class, AuthenticationError.class })
	public @ResponseBody AppResponse handle(ServerHttpRequest request, Exception ex) {

		if (ex instanceof AuthenticationError) {
			errorCode = ((AuthenticationError) ex).getErrorCode();
		}
		return createHttpErrorInfo(FORBIDDEN, errorCode, request, null, ex);
	}

	/**
	 * 
	 * @param request
	 * @param ex
	 * @return
	 */
	@ResponseStatus(PRECONDITION_REQUIRED)
	@ExceptionHandler({ PasswordNotAcceptableException.class, InsufficientFundException.class,
			UserProfileExpiredException.class })
	public @ResponseBody AppResponse handleUnAcceptableRequiredValueExceptions(ServerHttpRequest request,
			Exception ex) {

		if (ex instanceof PasswordNotAcceptableException) {
			errorCode = ((PasswordNotAcceptableException) ex).getErrorCode();
		} else if (ex instanceof InsufficientFundException) {
			errorCode = ((InsufficientFundException) ex).getErrorCode();
		} else if (ex instanceof UserProfileExpiredException) {
			errorCode = ((UserProfileExpiredException) ex).getErrorCode();
		}
		return createHttpErrorInfo(PRECONDITION_REQUIRED, errorCode, request, null, ex);
	}

	/**
	 * 
	 * @param request
	 * @param ex
	 * @return
	 */
	@ResponseStatus(FORBIDDEN)
	@ExceptionHandler(AccessDeniedException.class)
	public @ResponseBody AppResponse handle(ServerHttpRequest request, AccessDeniedException ex) {

		return createHttpErrorInfo(FORBIDDEN, errorCode, request, null, ex);
	}

	/**
	 * 
	 * @param ex
	 * @return
	 */
	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler({ MethodArgumentNotValidException.class })
	protected @ResponseBody AppResponse handleMethodArgumentNotValid(ServerHttpRequest request, Exception ex) {
		log.error("400 Status Code", ex);

		AppResponse customError = null;

		if (ex instanceof MethodArgumentNotValidException) {
			MethodArgumentNotValidException err = (MethodArgumentNotValidException) ex;
			final BindingResult result = err.getBindingResult();

			customError = createHttpErrorInfo(BAD_REQUEST, errorCode, request, null, ex);
			ApiError apiError = (ApiError) customError.getApiResponse();

			result.getAllErrors().stream().forEach(e -> {

				if (e instanceof FieldError) {
					apiError.addValidationError(((FieldError) e));
					// return ((FieldError) e).getField() + " : " + e.getDefaultMessage();
				} else {
					apiError.addValidationError(new ObjectError(e.getObjectName(), e.getDefaultMessage()));
					// return e.getObjectName() + " : " + e.getDefaultMessage();
				}
			});
		}

		return customError;
	}

	/**
	 * 
	 * @param request
	 * @param ex
	 * @return
	 */
	@ResponseStatus(NOT_FOUND)
	@ExceptionHandler(ResourceNotFoundException.class)
	public @ResponseBody AppResponse handleNotFoundExceptions(ServerHttpRequest request, ResourceNotFoundException ex) {

		errorCode = ((ResourceNotFoundException) ex).getErrorCode();

		return createHttpErrorInfo(NOT_FOUND, errorCode, request, null, ex);
	}

	/**
	 * 
	 * @param request
	 * @param ex
	 * @return
	 */
	@ResponseStatus(UNPROCESSABLE_ENTITY)
	@ExceptionHandler(InvalidRequestException.class)
	public @ResponseBody AppResponse handleInvalidInputException(ServerHttpRequest request,
			InvalidRequestException ex) {

		errorCode = ((InvalidRequestException) ex).getErrorCode();

		return createHttpErrorInfo(UNPROCESSABLE_ENTITY, errorCode, request, null, ex);
	}

	/**
	 * 
	 * @param e
	 * @return
	 */
	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler(WebExchangeBindException.class)
	public @ResponseBody AppResponse handleException(WebExchangeBindException ex, ServerHttpRequest request) {

		AppResponse customError = createHttpErrorInfo(HttpStatus.BAD_REQUEST, errorCode, request, null, ex);

		ApiError apiError = (ApiError) customError.getApiResponse();
		apiError.addValidationError(ex.getBindingResult().getAllErrors());

		return customError;
	}

	/**
	 * 
	 * @param request
	 * @param ex
	 * @return
	 */
	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler({ ClientErrorException.class, BadRequestException.class })
	public @ResponseBody AppResponse handleClientError(ServerWebExchange exchange, Exception ex) {

		AppResponse customError = null;
		ServerHttpRequest request = exchange.getRequest();

		if (ex instanceof ClientErrorException) {
			customError = handleClientErrorException((ClientErrorException) ex, exchange.getRequest());

		} else if (ex instanceof BadRequestException) {
			errorCode = ((BadRequestException) ex).getErrorCode();

			customError = createHttpErrorInfo(BAD_REQUEST, errorCode, request, null, ex);
		}

		return customError;
	}

	/**
	 * 
	 * @param request
	 * @param ex
	 * @return
	 */
	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler({ ConstraintViolationException.class, CustomConstraintViolationException.class,
			ConversionFailedException.class })
	public @ResponseBody AppResponse handleValidationExceptions(ServerHttpRequest request, Exception ex) {

		AppResponse customError = null;

		if (ex instanceof CustomConstraintViolationException) {
			CustomConstraintViolationException err = (CustomConstraintViolationException) ex;

			customError = createHttpErrorInfo(HttpStatus.BAD_REQUEST, err.getErrorCode(), request, null, ex);

			ApiError apiError = (ApiError) customError.getApiResponse();
			apiError.addValidationErrors(err.getConstraintViolations());

		} else if (ex instanceof ConversionFailedException) {
			ConversionFailedException err = (ConversionFailedException) ex;
			customError = createHttpErrorInfo(HttpStatus.BAD_REQUEST, errorCode, request, "Conversion error", ex);

			ApiError apiError = (ApiError) customError.getApiResponse();
			apiError.addValidationError(new ObjectError(err.getValue().toString(), err.getMessage()));
		} else {

			errorCode = ErrorCode.CONSTRAINT_VIOLATION_ERR_CODE;
			customError = createHttpErrorInfo(HttpStatus.BAD_REQUEST, errorCode, request, null, ex);
		}

		return customError;
	}

	private AppResponse createHttpErrorInfo(HttpStatus httpStatus, String errorCode, ServerHttpRequest request,
			String message, Exception ex) {
		final String path = request.getPath().pathWithinApplication().value();

		if (StringUtils.isBlank(message)) {
			message = ex.getMessage();
		}

		log.debug("Returning HTTP status: {} for path: {}, message: {}", httpStatus, path, message);
		// return new ApiError(path, httpStatus, message, ex);
		String traceId = (String) ThreadContext.get(TraceIdFilter.TRACE_ID_KEY);

		return new AppResponse(currentApiVersion, errorCode, httpStatus, message, path, traceId, sendReportUri,
				moreInfoUrl, ex);
	}

	/*
	 * 
	 */
	public AppResponse handleClientErrorException(ClientErrorException e, ServerHttpRequest request) {

		// e.printStackTrace();
		Response response = e.getResponse();
		final String path = request.getPath().pathWithinApplication().value();

		System.out.println("status: " + response.getStatus());
		System.out.println("reason: " + response.getStatusInfo().getReasonPhrase());

		Integer status = response.getStatus();
		String reason = response.getStatusInfo().getReasonPhrase();

		String detailedDesc = null;

		try {
			Map error = JsonSerialization.readValue((ByteArrayInputStream) response.getEntity(), Map.class);

			detailedDesc = (String) error.get("error_description");
			String message = (String) error.get("error");

			System.out.println("error: " + message);
			System.out.println("error_description: " + detailedDesc);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String traceId = (String) ThreadContext.get(TraceIdFilter.TRACE_ID_KEY);

		return new AppResponse(currentApiVersion, errorCode, status, detailedDesc, path, reason, traceId, sendReportUri,
				moreInfoUrl);
	}

}
