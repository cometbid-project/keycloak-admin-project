/**
 * 
 */
package com.keycloak.admin.client.config;

import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Getter
@Configuration
@PropertySource(value = {"classpath:springScheduled.properties"})
public class SchedulerConfig {
	
	@Value("${reactive.thread.maximum-pool-size:10}")
	private Integer connectionPoolSize;
	
	@Value(value = "${async.worker.capacity:10}")
	private Integer asyncThreadCapacity;

	@Value(value = "${async.worker.size:1}")
	private Integer asyncThreadPoolSize;

	@Value(value = "${async.worker.max_size:20}")
	private Integer asyncMaxThreadPoolSize;

	@Value(value = "${async.worker.thread.await_time:30}")
	private Integer asyncThreadAwaitTime;

	@Value(value = "${async.worker.aliveTime:1000}")
	private Integer KeepAliveTime;

	@Value(value = "${async.worker.thread.sleepTime.max:200}")
	private Integer maxSleepTime;

	@Value(value = "${event.worker.capacity:10}")
	private Integer eventThreadCapacity;

	@Value(value = "${event.worker.size:1}")
	private Integer eventThreadPoolSize;

	@Value(value = "${event.worker.max_size:20}")
	private Integer eventMaxThreadPoolSize;

	@Bean
	Scheduler blockingConnectionScheduler() {
		log.info("Creates a blockingConnectionScheduler with connectionPoolSize = {}", connectionPoolSize);
		return Schedulers.fromExecutor(Executors.newFixedThreadPool(connectionPoolSize));
	}

}
