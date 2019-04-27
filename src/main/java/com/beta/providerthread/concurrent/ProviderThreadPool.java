package com.beta.providerthread.concurrent;

import com.beta.providerthread.collect.Collector;
import com.beta.providerthread.collect.CollectorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ProviderThreadPool extends ThreadPoolExecutor {

    private static int CORE_POOL_SIZE = Runtime.getRuntime()
            .availableProcessors();

    private static int MAXIMUM_POOL_SIZE = Runtime.getRuntime()
            .availableProcessors();

    private static RejectedTaskController REJECTED_TASK_CONTROLLER
            = new RejectedTaskController();

    private static long KEEP_ALIVE_TIME = 10;

    private static final Logger logger = LoggerFactory.getLogger(ProviderThreadPool.class);

    public ProviderThreadPool() {
        super(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                REJECTED_TASK_CONTROLLER);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        ProviderTask providerTask = (ProviderTask)r;
        CollectorImpl collector = (CollectorImpl) providerTask.getCollector();
        collector.executeBefore(t);
        logger.info("beforeExecute,metrics: {},mo: {}", collector.getRule().getMetrics(), collector.getMo());
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        ProviderTask providerTask = (ProviderTask)r;
        CollectorImpl collector = (CollectorImpl) providerTask.getCollector();
        collector.executeAfter(t);
        logger.info("afterExecute,metrics: {},mo: {}", collector.getRule().getMetrics(), collector.getMo());
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new ProviderTask<>((Collector) runnable);
    }
}
