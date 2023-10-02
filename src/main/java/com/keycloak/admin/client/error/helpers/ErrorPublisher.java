/**
 * 
 */
package com.keycloak.admin.client.error.helpers;

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import com.keycloak.admin.client.exceptions.ApplicationDefinedRuntimeException;
import com.keycloak.admin.client.exceptions.AuthenticationError;
import com.keycloak.admin.client.exceptions.BadRequestException;
import com.keycloak.admin.client.exceptions.BlockedCredentialsAttemptsLoginWarning;
import com.keycloak.admin.client.exceptions.InvalidInputException;
import com.keycloak.admin.client.exceptions.InvalidJwtTokenException;
import com.keycloak.admin.client.exceptions.NewLocationTokenValidationException;
import com.keycloak.admin.client.exceptions.PasswordNotAcceptableException;
import com.keycloak.admin.client.exceptions.ResetPasswordTokenValidationException;
import com.keycloak.admin.client.exceptions.ResourceAlreadyExistException;
import com.keycloak.admin.client.exceptions.ResourceNotFoundException;
import com.keycloak.admin.client.exceptions.ServiceException;
import com.keycloak.admin.client.exceptions.SessionExpiredException;
import com.keycloak.admin.client.exceptions.ServiceUnavailableException;
import com.keycloak.admin.client.exceptions.UnauthenticatedUserException;
import com.keycloak.admin.client.exceptions.UserAlreadyExistException;
import com.keycloak.admin.client.exceptions.UserHasNoAttributesException;
import com.keycloak.admin.client.exceptions.UserProfileDisabledException;
import com.keycloak.admin.client.exceptions.UserProfileExpiredException;
import com.keycloak.admin.client.exceptions.UserProfileLockedException;
import com.keycloak.admin.client.exceptions.UserProfileUnverifiedException;

import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
public class ErrorPublisher {

	private ErrorPublisher() {

	}

	public static <T> Mono<T> raiseBadCredentials(String messageKey, Object[] args) {
		return Mono.error(new AuthenticationError(messageKey, args));
	}

	public static <T> Mono<T> raiseUserAlreadyExist() {
		return Mono.error(new UserAlreadyExistException(new Object[] {}));
	}

	public static <T> Mono<T> raiseBadRequestError(String messageKey, Object[] args) {
		return Mono.error(new BadRequestException(messageKey, args));
	}

	public static <T> Mono<T> raiseLoginSessionExpiredError(String messageKey, Object[] args) {
		return Mono.error(new SessionExpiredException(messageKey, args));
	}

	public static <T> Mono<T> raiseNewLocationTokenInvalidError(String messageKey, Object[] args) {
		return Mono.error(new NewLocationTokenValidationException(messageKey, args));
	}

	public static <T> Mono<T> raiseResetPasswordSessionExpiredError(String messageKey, Object[] args) {
		return Mono.error(new SessionExpiredException(messageKey, args));
	}

	public static <T> Mono<T> raiseUnauthenticatedUserError(String messagekey, Object[] args) {
		return Mono.error(new UnauthenticatedUserException(messagekey, args));
	}

	public static <T> Mono<T> raiseRuntimeError(final String message, final Throwable cause) {
		return Mono.error(new ApplicationDefinedRuntimeException(message, cause));
	}

	public static <T> Mono<T> raiseServiceUnavailableError(final String messageKey, Object[] args) {
		return Mono.error(new ServiceUnavailableException(messageKey, args));
	}

	public static <T> Mono<T> raiseResourceNotFoundError(final String messageKey, Object[] args) {
		return Mono.error(new ResourceNotFoundException(messageKey, args));
	}

	public static <T> Mono<T> raiseInvalidInputRequestError(final String message) {
		Set<ConstraintViolation<?>> constraintViolations = new HashSet<>();

		return Mono.error(new ConstraintViolationException(message, constraintViolations));
	}

	public static <T> Mono<T> raiseServiceExceptionError(final String body, Integer statusCode) {
		return Mono.error(new ServiceException(body, statusCode));
	}

	public static <T> Mono<T> raiseInvalidJwtToken(String messageKey, Object[] args) {
		return Mono.error(new InvalidJwtTokenException(messageKey, args));
	}

	public static <T> Mono<T> raiseResetPasswordTokenError(String messageKey, Object[] args) {
		return Mono.error(new ResetPasswordTokenValidationException(messageKey, args));
	}

	public static <T> Mono<T> raiseUserHasNoAttributesException(Object[] args) {
		return Mono.error(new UserHasNoAttributesException(args));
	}

	public static <T> Mono<T> raiseUserProfileLockedException(Object[] args) {
		return Mono.error(new UserProfileLockedException(args));
	}

	public static <T> Mono<T> raiseUserProfileExpiredException(Object[] args) {
		return Mono.error(new UserProfileExpiredException(args));
	}

	public static <T> Mono<T> raiseUserProfileUnverifiedException(Object[] args) {
		return Mono.error(new UserProfileUnverifiedException(args));
	}

	public static <T> Mono<T> raiseUserProfileDisabledException(Object[] args) {
		return Mono.error(new UserProfileDisabledException(args));
	}
	
	public static <T> Mono<T> raisePasswordUnacceptableException(String messageKey, Object[] args) {
		return Mono.error(new PasswordNotAcceptableException(messageKey, args));
	}
	
	public static <T> Mono<T> raiseResourceAlreadyExistException(Object[] args) {
		return Mono.error(new ResourceAlreadyExistException(args));
	}
	
	public static <T> Mono<T> raiseInvalidInputException(String message, 
			Set<ConstraintViolation<?>> constraintViolations) {
		//Set<ConstraintViolation<?>> constraintViolations = new HashSet<>();

		return Mono.error(new ConstraintViolationException(message, constraintViolations));
	}
	
	public static <T> Mono<T> raiseBlockedIPAttemptLoginAlert(Object[] args) {
		return Mono.error(new BlockedCredentialsAttemptsLoginWarning(args));
	}


// ===============================================================================================
	
	public static void raiseBadRequestException(String messageKey, Object[] args) {
		throw new BadRequestException(messageKey, args);
	}
	
	public static void raiseRuntimeException(String message, Throwable ex) {
		throw new ApplicationDefinedRuntimeException(message, ex);
	}

	/*
	public static ServiceUnavailableException raiseServiceUnavailableException(
						String messageKey, Object[] args) {
		throw new ServiceUnavailableException(messageKey, args);
	}

	public static void raiseResourceNotFoundException(Object[] args) {
		throw new ResourceNotFoundException(args);
	}

	public static void raisePasswordUnacceptableException(Object[] args) {
		throw new PasswordNotAcceptableException(args);
	}

	public static void raiseBadCredentialsException(String messageKey, Object[] args) {
		throw new AuthenticationError(messageKey, args);
	}

	public static void raiseUserAlreadyExistException(Object[] args) {
		throw new UserAlreadyExistException(args);
	}

	public static void raiseResourceAlreadyExistException(Object[] args) {
		throw new ResourceAlreadyExistException(args);
	}

	public static void raiseUnauthenticatedUserException(Object[] args) {
		throw new UnauthenticatedUserException(args);
	}

	public static void raiseNewLocationTokenInvalidException(Object[] args) {
		throw new NewLocationTokenValidationException(args);
	}

	public static void raiseBlockedIPAttemptLoginAlert(Object[] args) {
		throw new BlockedCredentialsAttemptsLoginWarning(args);
	}

	public static void raiseLoginSessionExpiredException(String messageKey, Object[] args) {
		throw new SessionExpiredException(messageKey, args);
	}

	public static InvalidInputException raiseInvalidInputException(String message) {
		Set<ConstraintViolation<?>> constraintViolations = new HashSet<>();

		throw new ConstraintViolationException(message, constraintViolations);
	}

	public static void raiseInvalidJwtTokenException(String messageKey, Object[] args) {
		throw new InvalidJwtTokenException(messageKey, args);
	}
	*/
}
