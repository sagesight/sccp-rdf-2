package com.goodforgoodbusiness.endpoint.processor;

import static com.goodforgoodbusiness.endpoint.processor.PrioritizedTask.Priority.NORMAL;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Create the {@link ExecutorService} for queries.
 * This is a priority-queue backed threadpool that can assess which tasks to run first.
 */
@Singleton
public class ExecutorProvider implements Provider<ExecutorService> {
	private static final Logger log = Logger.getLogger(ExecutorProvider.class);
	private static final int INITIAL_CAPACITY = 10;
	
	/**
	 * Assess the priority of Runnable tasks
	 */
	private static int getPriority(Runnable r) {
		return (r instanceof PrioritizedTask ? ((PrioritizedTask)r).getPriority() : NORMAL).ordinal;
	}
	
	private final int poolSize;
	
	@Inject
	public ExecutorProvider(@Named("threadpool.size") int threadPoolSize) {
		this.poolSize = threadPoolSize;
	}

	@Override @Singleton
	public ExecutorService get() {
		// use a priority queue to decide the order of task execution
		var priorityQueue = new PriorityBlockingQueue<Runnable>(
			INITIAL_CAPACITY,
			(a, b) -> getPriority(b) - getPriority(a)
		);
		
		return new ThreadPoolExecutor(poolSize, poolSize, 1, MINUTES, priorityQueue) {
			@Override
			protected void beforeExecute(Thread t, Runnable r) {
				log.info("ExecutorService queue size = " + priorityQueue.size());
				super.beforeExecute(t, r);
			}
			
			@Override
			protected void afterExecute(Runnable r, Throwable t) {
				super.afterExecute(r, t);
				
				var cause = t;
		        if (t == null && r instanceof Future<?>) {
		            try {
		                Future<?> future = (Future<?>) r;
		                if (future.isDone()) {
		                    future.get();
		                }
		            }
		            catch (CancellationException ce) {
		                cause = ce;
		            }
		            catch (ExecutionException ee) {
		                cause = ee.getCause();
		            }
		            catch (InterruptedException ie) {
		                Thread.currentThread().interrupt();
		            }
		        }
		        
		        if (cause != null) {
		            log.error("Error from task", cause);
		        }
		        
		        log.info("ExecutorService queue size = " + priorityQueue.size());
			}
		};
	}
}