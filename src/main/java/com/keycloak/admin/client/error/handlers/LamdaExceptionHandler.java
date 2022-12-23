/**
 * 
 */
package com.keycloak.admin.client.error.handlers;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.util.ReflectionUtils;
import com.keycloak.admin.client.error.helpers.ErrorPublisher;
import com.keycloak.admin.client.exceptions.BadRequestException;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Log4j2
public class LamdaExceptionHandler {

	public static <T, E extends Exception> Consumer<T> handleGenericException(Consumer<T> targetConsumer,
			Class<E> exObjClass) {

		return obj -> {
			try {
				targetConsumer.accept(obj);
			} catch (Exception ex) {

				try {
					E exObj = exObjClass.cast(ex);

					log.error("Exception occured: {}", exObj.getMessage());
				} catch (ClassCastException ecx) {

					throw ex;
				}
			}
		};
	}

	public static <T, R> Function<T, Mono<R>> handleCheckedExceptionFunction(
			LambdaCheckedExceptionFunction<T, R> handlerFunction) {

		return obj -> {
			R r = null;
			try {
				r = handlerFunction.apply(obj);
			} catch (Exception ex) {
				log.error("Exception occured: ", ex);
				//ReflectionUtils.rethrowRuntimeException(ex);
				
				return Mono.error(ex);
			}
			return Mono.just(r);
		};
	}

	public static <T> Consumer<T> handleCheckedExceptionConsumer(
			LambdaCheckedExceptionConsumer<T, Exception> handlerConsumer) {

		return obj -> {
			try {
				handlerConsumer.accept(obj);
			} catch (Exception ex) {

				log.error("Exception occured: ", ex);
				ReflectionUtils.rethrowRuntimeException(ex);
			}
		};
	}

	public static <T> Runnable handleCheckedExceptionRunnable(
			RunnableCheckedExceptionHandler<T, Exception> handlerRunnable) {

		return () -> {
			try {
				handlerRunnable.run();
			} catch (Exception ex) {

				log.error("Exception occured: ", ex);
				ReflectionUtils.rethrowRuntimeException(ex);

			}
		};
	}

	public static <T> Function<? super Throwable, ? extends Mono<? extends T>> handleWebFluxError(String genericMsg) {

		return error -> {
			if (error instanceof BadRequestException) {
				return Mono.error(error);
			}

			return ErrorPublisher.raiseRuntimeError(genericMsg, error);
		};
	}

	public static void main(String args[]) {

		List<String> list = Arrays.asList("44", "373", "xyz");

		list.forEach(handleGenericException(s -> System.out.println(Integer.parseInt(s)), NumberFormatException.class));

		List<Integer> list2 = Arrays.asList(10, 20);
		list2.forEach(handleCheckedExceptionConsumer(i -> {

			Thread.sleep(i);
			System.out.println(i);
		}));
	}
}
