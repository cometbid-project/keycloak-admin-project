/**
 * 
 */
package com.keycloak.admin.client.validators;

import java.util.Set;

import com.keycloak.admin.client.exceptions.CustomConstraintViolationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Log4j2
public class GenericProgrammaticValidator<T> {

	private static Validator validator;

	static {
		ValidatorFactory factory = Validation
				.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	public static <T> Mono<T> validate(T model) {

		log.info("Validating model object: {}", model);
		Set<ConstraintViolation<Object>> violations = validator.validate(model);

		if (!violations.isEmpty()) {
			return Mono.error(new CustomConstraintViolationException(violations));
		}

		return Mono.just(model);
	}
}
