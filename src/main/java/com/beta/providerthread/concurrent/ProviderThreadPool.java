package com.beta.providerthread.concurrent;

import com.beta.providerthread.model.Metrics;
import com.beta.providerthread.monitor.MetricsMonitorService;
import io.github.resilience4j.circuitbreaker.CircuitBreakerOpenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ProviderThreadPool extends ThreadPoolExecutor {

    private ThreadLocal<Long> startTime = new ThreadLocal<>();

    private AtomicLong totalServiceTime = new AtomicLong();

    private AtomicLong maxServiceTime = new AtomicLong(0L);

    private AtomicInteger finishedNumer = new AtomicInteger();

    private MetricsMonitorService metricsMonitorService;

    private static int CORE_POOL_SIZE = Runtime.getRuntime()
            .availableProcessors();

    private static int MAXIMUM_POOL_SIZE = Runtime.getRuntime()
            .availableProcessors();

    private static RejectedTaskController REJECTED_TASK_CONTROLLER
            = new RejectedTaskController();

    private static long KEEP_ALIVE_TIME = 10;

    private static final Logger logger = LoggerFactory.getLogger(ProviderThreadPool.class);

    public ProviderThreadPool(MetricsMonitorService metricsMonitorService) {
        super(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                REJECTED_TASK_CONTROLLER);
        this.metricsMonitorService = metricsMonitorService;
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        startTime.set(Long.valueOf(System.nanoTime()));
        ProviderTask providerTask = (ProviderTask) r;
        Collector collector = providerTask.getCollector();
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

        if (collector.isTimeout()) {
            t = new TimeoutException("bizTimeout: " + collector.getBizTimeout() + " '" + collector.getHitLog().getRule().getMetricsName() + "'");
        } else if (collector.isCircuitBreakOpen()) {
            t = new CircuitBreakerOpenException("CircuitBreaker '" + collector.getHitLog().getRule().getMetricsName() + "' is open");
        } else if (t == null && r instanceof Future<?>) {
            // MetricsProvider.sample()抛出的异常
            // 线程池在执行任务时捕获了所有异常，并将此异常加入结果中,线程池中的所有线程都将无法捕获到抛出的异常。
            // 异常是封装在此时的Future对象中的
            // 任务执行完成获取其结果时,Future.get()会抛出此RuntimeException。
            try {
                Future<?> future = (Future<?>) r;
                if (future.isDone())
                    future.get();
            } catch (Exception ex) {
                t = ex;
            }
        }
        Metrics metrics = collector.getHitLog().getRule().getMetrics();
        if (t != null) {
            if(t instanceof TimeoutException){
                metricsMonitorService.getMetricsMonitorInfo(metrics).getTimeout().addAndGet(1);
            }else if(t instanceof CircuitBreakerOpenException){
                metricsMonitorService.getMetricsMonitorInfo(metrics).getNotPermitted().addAndGet(1);
            }else {
                metricsMonitorService.getMetricsMonitorInfo(metrics).getError().addAndGet(1);
            }
        }else {
            metricsMonitorService.getMetricsMonitorInfo(metrics).getSuccess().addAndGet(1);
        }
        metricsMonitorService.getMetricsMonitorInfo(metrics).getServiceTime().addAndGet(serviceTime);

        logger.info("afterExecute,metrics: {},mo: {}", collector.getHitLog().getRule().getMetrics(), collector.getHitLog().getMo());
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new ProviderTask<>((Collector) runnable);
    }

    public double getAvgServiceTime() {
        return fromNanoToSeconds(this.totalServiceTime.get()) / (double) this.finishedNumer.get();
    }

    private long fromNanoToSeconds(long nanos) {
        return TimeUnit.NANOSECONDS.toSeconds(nanos);
    }
}
