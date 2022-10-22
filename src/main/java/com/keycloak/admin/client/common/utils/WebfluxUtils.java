/**
 * 
 */
package com.keycloak.admin.client.common.utils;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import org.reactivestreams.Publisher;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

/**
 * @author Gbenga
 *
 */
@Log4j2
public class WebfluxUtils {

	/*
	 * 
	 */
	public static <T> Flux<T> asyncFlux(Supplier<Publisher<T>> publisherSupplier, Scheduler scheduler) {
		return Flux.defer(publisherSupplier).subscribeOn(scheduler);
	}

	public static <T> Mono<? extends T> asyncMono(Callable<? extends T> function, Scheduler scheduler) {
		return Mono.fromCallable(function).publishOn(scheduler);
	}

}
