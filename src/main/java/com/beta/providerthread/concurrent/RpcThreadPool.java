package com.beta.providerthread.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * I/O阻塞的任务线程池
 */
public class RpcThreadPool extends ThreadPoolExecutor {

    private ThreadLocal<Long> startTime = new ThreadLocal<>();

    private AtomicLong totalServiceTime = new AtomicLong();

    private AtomicLong maxServiceTime = new AtomicLong(0L);

    private AtomicInteger finishedNumer = new AtomicInteger();

    private static RejectedTaskController REJECTED_TASK_CONTROLLER
            = new RejectedTaskController();

    private static long KEEP_ALIVE_TIME = 10;

    private static final Logger logger = LoggerFactory.getLogger(RpcThreadPool.class);

    public RpcThreadPool(int corePoolSize,int maxPoolSize,int keepAliveTime,int queueLength) {
        super(corePoolSize, maxPoolSize, keepAliveTime,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(queueLength),
                REJECTED_TASK_CONTROLLER);
        this.setThreadFactory(new RpcThreadFactory());
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

    private static class RpcThreadFactory implements ThreadFactory {

        private final AtomicInteger poolNumber = new AtomicInteger(1);

        private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

        public Thread newThread(Runnable r) {
            Thread thread = defaultFactory.newThread(r);
            thread.setName("rpcPool-" +
                    poolNumber.getAndIncrement() +
                    "-thread-");
            thread.setDaemon(true);
            return thread;
        }
    }
}
