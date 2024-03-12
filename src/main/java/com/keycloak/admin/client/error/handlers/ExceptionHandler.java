/**
 * 
 */
package com.keycloak.admin.client.error.handlers;

import static com.keycloak.admin.client.error.helpers.ErrorPublisher.*;
import java.io.IOException;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.ws.rs.ClientErrorException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.keycloak.admin.client.exceptions.ApplicationDefinedRuntimeException;
import com.keycloak.admin.client.exceptions.AuthenticationError;
import com.keycloak.admin.client.exceptions.InvalidRequestException;
import com.keycloak.admin.client.exceptions.ResourceNotFoundException;
import com.keycloak.admin.client.exceptions.ServiceException;
import com.keycloak.admin.client.exceptions.ServiceUnavailableException;
import com.keycloak.admin.client.exceptions.SessionExpiredException;
import com.keycloak.admin.client.exceptions.UnauthenticatedUserException;
import com.keycloak.admin.client.exceptions.UserAlreadyExistException;
import com.keycloak.admin.client.exceptions.UserProfileDisabledException;
import com.keycloak.admin.client.exceptions.UserProfileExpiredException;
import com.keycloak.admin.client.exceptions.UserProfileLockedException;
import com.keycloak.admin.client.exceptions.UserProfileUnverifiedException;
import com.keycloak.admin.client.response.model.ApiError;
import com.keycloak.admin.client.response.model.AppResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Log4j2
public class ExceptionHandler {

	private static Integer UNKNOWN_ERRORCODE = 501;
	
	public static Predicate<? super Throwable> ERROR_Predicate = ex -> !(ex instanceof ApplicationDefinedRuntimeException);

	public static <T> Function<? super Throwable, ? extends Mono<? extends T>> handleWebFluxError(String genericMsg) {

		return error -> {
			if (error instanceof ClientErrorException || error instanceof ApplicationDefinedRuntimeException) {

				return Mono.error(error);
			}

			return raiseRuntimeError(genericMsg, error);
		};
	}

	public static <R> Mono<R> processResponse(ClientResponse clientResponse, Class<? extends R> clazzResponse) {
		HttpStatusCode status = clientResponse.statusCode();

		Mono<R> respObj = Mono.empty();

		if (status.is2xxSuccessful()) {
			respObj = clientResponse.bodyToMono(clazzResponse);

		} else if (status.isError()) {
			if (status.is4xxClientError()) {
				log.error("Client Error occurred while processing request");
				// String errorMsgKey = "client.error";

				return clientResponse.createException().flatMap(ex -> Mono.error(handle4xxException(ex)));

			} else if (status.is5xxServerError()) {
				log.error("Server Error occurred while processing request");
				// String errorMsgKey = "server.error";
				return clientResponse.createException().flatMap(ex -> Mono.error(handle5xxException(ex)));
			} else {

				return clientResponse.createException().flatMap(ex -> {

					return raiseServiceExceptionError(clientResponse.statusCode().toString(), clientResponse.statusCode().value());
				});
			}
		}

		return respObj;
	}

	public static Throwable handle5xxException(Throwable ex) {

		if (!(ex instanceof WebClientResponseException)) {
			log.warn("Got a unexpected error: {}, will rethrow it", ex.toString());

			return new ServiceException(ex.getMessage(), UNKNOWN_ERRORCODE);
		}

		WebClientResponseException wcre = (WebClientResponseException) ex;

		switch (wcre.getStatusCode().value()) {

		case 503: //HttpStatus.SERVICE_UNAVAILABLE:

			return new ServiceUnavailableException(new Object[] { getErrorMessage(wcre) });
		case 509: //HttpStatus.BANDWIDTH_LIMIT_EXCEEDED:

			return new InvalidRequestException(getErrorMessage(wcre));
		case 500: //HttpStatus.INTERNAL_SERVER_ERROR:

			return new ServiceUnavailableException(new Object[] { getErrorMessage(wcre) });
		default:
			log.warn("Got a unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
			log.warn("Error body: {}", wcre.getResponseBodyAsString());

			return new ServiceException(wcre.getMessage(), wcre.getStatusCode().value());
		}
	}

	public static Throwable handle4xxException(Throwable ex) {

		if (!(ex instanceof WebClientResponseException)) {
			log.warn("Got a unexpected error: {}, will rethrow it", ex.toString());

			return new ServiceException(ex.getMessage(), UNKNOWN_ERRORCODE);
		}

		WebClientResponseException wcre = (WebClientResponseException) ex;

		switch (wcre.getStatusCode().value()) {

		case 404: //HttpStatus.NOT_FOUND

			return new ResourceNotFoundException(new Object[] { getErrorMessage(wcre) });
		case 422: //HttpStatus.UNPROCESSABLE_ENTITY

			return new InvalidRequestException(getErrorMessage(wcre));
		case 401: //HttpStatus.UNAUTHORIZED.value():
		case 403: //HttpStatus.FORBIDDEN.value():

			return new AuthenticationError(getErrorMessage(wcre));
		default:
			log.warn("Got a unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
			log.warn("Error body: {}", wcre.getResponseBodyAsString());

			return new ServiceException(wcre.getMessage(), wcre.getStatusCode().value());
		}
	}

	static String getErrorMessage(WebClientResponseException ex) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			AppResponse appResponse = mapper.readValue(ex.getResponseBodyAsString(), AppResponse.class);
			
			ApiError apiError = (ApiError)appResponse.getApiResponse();
			
			return apiError.getMessage();
			
		} catch (IOException ioex) {
			return ex.getMessage();
		}
	}
}
