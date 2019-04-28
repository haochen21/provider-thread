package com.beta.providerthread.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ProviderThreadPool extends ThreadPoolExecutor {

    private ThreadLocal<Long> startTime = new ThreadLocal<>();

    private AtomicLong totalServiceTime = new AtomicLong();

    private AtomicLong maxServiceTime = new AtomicLong(0L);

    private AtomicInteger finishedNumer = new AtomicInteger();

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
        startTime.set(Long.valueOf(System.nanoTime()));
        ProviderTask providerTask = (ProviderTask) r;
        Collector collector =  providerTask.getCollector();
        logger.info("beforeExecute,metrics: {},mo: {}", collector.getHitLog().getRule().getMetrics(), collector.getHitLog().getMo());
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        long serviceTime = System.nanoTime() - startTime.get().longValue();
        totalServiceTime.addAndGet(serviceTime);
        if (maxServiceTime.get() < serviceTime) {
            maxServiceTime.set(serviceTime);
        }
        finishedNumer.incrementAndGet();

        ProviderTask providerTask = (ProviderTask) r;
        Collector collector = providerTask.getCollector();
        logger.info("afterExecute,metrics: {},mo: {}", collector.getHitLog().getRule().getMetrics(), collector.getHitLog().getMo());
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new ProviderTask<>((Collector)runnable);
    }

    public double getAvgServiceTime() {
        return fromNanoToSeconds(this.totalServiceTime.get()) / (double) this.finishedNumer.get();
    }

    private long fromNanoToSeconds(long nanos) {
        return TimeUnit.NANOSECONDS.toSeconds(nanos);
    }
}
