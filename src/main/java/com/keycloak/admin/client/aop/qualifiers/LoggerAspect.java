/**
 * 
 */
package com.keycloak.admin.client.aop.qualifiers;

import static com.keycloak.admin.client.components.TraceIdFilter.*;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.ThreadContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * @author Gbenga
 *
 */
@Aspect
@Log4j2
@Component
public class LoggerAspect {
	
	/*
	@Pointcut("execution(@com.apress.messaging.annotation.Log * com.apress.messaging..*.*(..)) && @annotation(codeLog)")
	public void codeLogger(Loggable codeLog){}
	*/

	@SuppressWarnings("unchecked")
	@Around("@annotation(Loggable)")
	public Object logAround(final ProceedingJoinPoint joinPoint) throws Throwable {

		long start = System.currentTimeMillis();
		Object result = joinPoint.proceed();
		if (result instanceof Mono) {
			Mono monoResult = (Mono) result;
			AtomicReference<String> traceId = new AtomicReference<>("");

			return monoResult.doOnSuccess(o -> {
				if (!traceId.get().isEmpty()) {
					ThreadContext.put(TRACE_ID_KEY, traceId.get());
				}
				String response = "";
				if (Objects.nonNull(o)) {
					response = o.toString();
				}
				log.info("Enter: {}.{}() with argument[s] = {}", joinPoint.getSignature().getDeclaringTypeName(),
						joinPoint.getSignature().getName(), joinPoint.getArgs());
				log.info("Exit: {}.{}() had arguments = {}, with result = {}, Execution time = {} ms",
						joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(),
						joinPoint.getArgs()[0], response, (System.currentTimeMillis() - start));
			}).contextWrite(context -> {
				Context contextTmp = (Context) context;
				if (contextTmp.hasKey(TRACE_ID_KEY)) {
					traceId.set(contextTmp.get(TRACE_ID_KEY));
					ThreadContext.put(TRACE_ID_KEY, contextTmp.get(TRACE_ID_KEY));
				}
				return context;
			}).doOnError(o -> {
				if (!traceId.get().isEmpty()) {
					ThreadContext.put(TRACE_ID_KEY, traceId.get());
				}
				log.info("Enter: {}.{}() with argument[s] = {}", joinPoint.getSignature().getDeclaringTypeName(),
						joinPoint.getSignature().getName(), joinPoint.getArgs());
				log.error("Exit: {}.{}() had arguments = {}, with result = {}, Execution time = {} ms",
						joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(),
						joinPoint.getArgs()[0], o.toString(), (System.currentTimeMillis() - start));
			});
		} else if (result instanceof Flux) {
			Flux fluxResult = (Flux) result;

			return fluxResult.doFinally(o -> {
				log.info("Enter: {}.{}() with argument[s] = {}", joinPoint.getSignature().getDeclaringTypeName(),
						joinPoint.getSignature().getName(), joinPoint.getArgs());
				log.info("Exit: {}.{}() had arguments = {}, with result = {}, Execution time = {} ms",
						joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(),
						joinPoint.getArgs()[0], o.toString(), (System.currentTimeMillis() - start));
			}).doOnError(o -> {
				log.info("Enter: {}.{}() with argument[s] = {}", joinPoint.getSignature().getDeclaringTypeName(),
						joinPoint.getSignature().getName(), joinPoint.getArgs());
				log.error("Exit: {}.{}() had arguments = {}, with result = {}, Execution time = {} ms",
						joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(),
						joinPoint.getArgs()[0], o.toString(), (System.currentTimeMillis() - start));
			});
		} else {
			log.warn("Body type is not Mono/Flux for {}.{}()", joinPoint.getSignature().getDeclaringTypeName(),
					joinPoint.getSignature().getName());

			try {
				log.info("Enter: {}.{}() with argument[s] = {}", joinPoint.getSignature().getDeclaringTypeName(),
						joinPoint.getSignature().getName(), joinPoint.getArgs());
				log.info("Exit: {}.{}() had arguments = {}, with result = {}, Execution time = {} ms",
						joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(),
						joinPoint.getArgs()[0], result, (System.currentTimeMillis() - start));
			} catch (Exception e) {
				log.info("Enter: {}.{}() with argument[s] = {}", joinPoint.getSignature().getDeclaringTypeName(),
						joinPoint.getSignature().getName(), joinPoint.getArgs());
				log.error("Exit: {}.{}() had arguments = {}, with result = {}, Execution time = {} ms",
						joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(),
						joinPoint.getArgs()[0], e, (System.currentTimeMillis() - start));
			}

			return result;
		}

	}
}
