/**
 * 
 */
package com.keycloak.admin.client.common.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.log4j.Log4j2;

/**
 * @author Gbenga
 *
 */
@Log4j2
public class BlockingThreadPoolExecutor extends ThreadPoolExecutor {

	private final Semaphore semaphore;

	public BlockingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);

		semaphore = new Semaphore(maximumPoolSize);
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);

		log.info("Perform beforeExecute() logic");
	}

	@Override
	public void execute(final Runnable task) {
		boolean acquired = false;
		do {
			try {
				semaphore.acquire();
				acquired = true;
			} catch (final InterruptedException e) {
				// LOGGER.warn("InterruptedException whilst aquiring semaphore", e);
			}
		} while (!acquired);
		
		try {
			super.execute(task);
		} catch (final RejectedExecutionException e) {
			log.info("Task Rejected");
			semaphore.release();
			throw e;
		}
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);
		if (t != null) {
			t.printStackTrace();

			log.info("Perform exception handler logic");
		}

		log.info("Perform afterExecute() logic");

		// Release lock
		semaphore.release();
	}

}

