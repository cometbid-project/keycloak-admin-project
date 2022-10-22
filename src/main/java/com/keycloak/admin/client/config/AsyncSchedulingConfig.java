/**
 * 
 */
package com.keycloak.admin.client.config;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.BlockingQueue;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.keycloak.admin.client.common.utils.BlockingThreadPoolExecutor;
import com.keycloak.admin.client.common.utils.DateUtil;

import lombok.extern.log4j.Log4j2;

/**
 * 
 * @author Gbenga
 *
 */
@Log4j2
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncSchedulingConfig implements AsyncConfigurer, SchedulingConfigurer {

	@Autowired
	private SchedulerConfig appConfig;

	//@Autowired
	//private GrpcServerProperties properties;

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean(name = "eventTaskExecutor")
	public Executor threadPoolTaskExecutor() {

		final int QUEUE_CAPACITY = appConfig.getEventThreadCapacity();
		final int QUEUE_POOL_SIZE = appConfig.getEventThreadPoolSize();
		final int MAX_QUEUE_POOL_SIZE = appConfig.getEventMaxThreadPoolSize();
		final int KEEP_ALIVE_TIME = appConfig.getKeepAliveTime();
		final int MAX_SLEEP_TIME = appConfig.getMaxSleepTime();

		BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<Runnable>(QUEUE_CAPACITY);

		BlockingThreadPoolExecutor executor = new BlockingThreadPoolExecutor(QUEUE_POOL_SIZE, MAX_QUEUE_POOL_SIZE,
				KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS, blockingQueue);

		executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				log.info("Event Worker Task Rejected : {} time: {}", Thread.currentThread().getName(), DateUtil.now());

				log.info("Waiting for a second !!");
				try {

					Thread.sleep(MAX_SLEEP_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				log.info("Lets add another time : " + r);
				executor.execute(r);
			}
		});

		// Let start all core threads initially
		executor.prestartAllCoreThreads();

		return executor;
	}

	@Override
	@Bean(name = "AsyncEventTaskExecutor")
	public Executor getAsyncExecutor() {
		final int MAX_SLEEP_TIME = appConfig.getMaxSleepTime();
		
		ThreadPoolTaskExecutor threadPoolExecutor = new ThreadPoolTaskExecutor();

		threadPoolExecutor.setAllowCoreThreadTimeOut(true);
		threadPoolExecutor.setAwaitTerminationSeconds(appConfig.getAsyncThreadAwaitTime());
		threadPoolExecutor.setWaitForTasksToCompleteOnShutdown(true);
		threadPoolExecutor.setCorePoolSize(appConfig.getAsyncThreadPoolSize());
		threadPoolExecutor.setQueueCapacity(appConfig.getAsyncThreadCapacity());
		threadPoolExecutor.setMaxPoolSize(appConfig.getAsyncMaxThreadPoolSize());
		threadPoolExecutor.setThreadNamePrefix("MerchantAsyncEventTaskExecutor-");
		threadPoolExecutor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				log.info("Async #threadPoolTaskExecutor Task Rejected : {} time: {}", Thread.currentThread().getName(),
						DateUtil.now());

				log.info("Waiting for a second !!");
				try {
					
					Thread.sleep(MAX_SLEEP_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				log.info("Lets add another time : " + r);
				executor.execute(r);
			}
		});

		return threadPoolExecutor;
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new CustomAsyncExceptionHandler();
	}

	@Bean(name = "applicationEventMulticaster")
	public ApplicationEventMulticaster simpleApplicationEventMulticaster(
			@Autowired @Qualifier("AsyncEventTaskExecutor") Executor taskExecutor) {
		SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();

		eventMulticaster.setTaskExecutor(taskExecutor);
		return eventMulticaster;
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		try {
			taskRegistrar.setScheduler(taskExecutor());

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			log.error(String.format("Configuring Async Event Scheduler failed - %s", e.getMessage()), e);
		}
	}

	@Bean(name = "merchantSchedulerThread", destroyMethod = "shutdown")
	public Executor taskExecutor() throws InterruptedException {
		ThreadFactory customThreadfactory = new ThreadFactoryBuilder()
				.setNameFormat("Merchant-Service-JobScheduler-Executor-WorkerthreadPool-%d").setDaemon(false)
				.setPriority(Thread.MAX_PRIORITY).setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
					@Override
					public void uncaughtException(Thread t, Throwable e) {

						log.error(String.format("Thread %s threw exception - %s", t.getName(), e.getMessage()), e);
					}
				}).build();

		int awaitTerminationInMillis = appConfig.getAsyncThreadAwaitTime();
		int threadSize = appConfig.getAsyncThreadPoolSize();

		ExecutorService executorService = Executors.newFixedThreadPool(threadSize, customThreadfactory);
		executorService.awaitTermination(awaitTerminationInMillis, TimeUnit.MILLISECONDS);
		
		return executorService;
	}

}
