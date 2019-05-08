package com.beta.providerthread.concurrent;

import com.beta.providerthread.cache.MetricsValueCache;
import com.beta.providerthread.model.HitLog;

import com.beta.providerthread.model.SampleValue;
import com.beta.providerthread.provider.CacheMetricsProvider;
import com.beta.providerthread.service.CircuitBreakerService;
import com.beta.providerthread.service.SemaphoreService;
import com.beta.providerthread.provider.MetricsProvider;
import com.beta.providerthread.provider.TimeoutMetricProvider;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerOpenException;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;

@Setter
@Getter
public class Collector implements Runnable, Comparable<Collector> {

    private MetricsValueCache metricsValueCache;

    private ProviderThreadPool threadPool;

    private CircuitBreakerService circuitBreakerService;

    private SemaphoreService semaphoreService;

    private HitLog hitLog;

    private int bizTimeout;

    private Priority priority;

    private boolean isTimeout = false;

    private boolean isCircuitBreakOpen = false;

    private static final Logger logger = LoggerFactory.getLogger(Collector.class);

    public Collector(MetricsValueCache metricsValueCache, CircuitBreakerService circuitBreakerService,
                     SemaphoreService semaphoreService, HitLog hitLog, int bizTimeout, Priority priority) {
        this.metricsValueCache = metricsValueCache;
        this.circuitBreakerService = circuitBreakerService;
        this.semaphoreService = semaphoreService;
        this.hitLog = hitLog;
        this.bizTimeout = bizTimeout;
        this.priority = priority;
    }


    @Override
    public void run() {
        Semaphore semaphore = semaphoreService.getSemaphore(hitLog);

        if (semaphore.tryAcquire()) {
            CircuitBreaker circuitBreaker = circuitBreakerService.
                    getCircuitBreaker(hitLog.getRule().getMetrics());

            CacheMetricsProvider provider = new CacheMetricsProvider();
            //TimeoutMetricProvider provider = new TimeoutMetricProvider();

            // 如果upstream返回一个mono,在断路状态下，upstream还会被调用
            Callable<SampleValue> callable = () -> provider.sample(this.hitLog.getMo(), this.hitLog.getRule().getMetrics());
            Mono.fromCallable(callable)
                    .transform(CircuitBreakerOperator.of(circuitBreaker))
                    .timeout(Duration.ofSeconds(bizTimeout))
                    .doOnError(error -> {
                        // circuitBreak抛出的异常不在线程池线程里，线程池afterExecute获取不到
                        if (error instanceof TimeoutException) {
                            // 这里抛出的异常不在线程池线程里，线程池afterExecute获取不到
                            // 业务超时异常，这里手动触发电路器异常统计
                            circuitBreaker.onError(
                                    bizTimeout * 1000, error);
                            this.isTimeout = true;
                        } else if (error instanceof CircuitBreakerOpenException) {
                            this.isCircuitBreakOpen = true;
                        }
                    })
                    .doFinally(signalType -> semaphore.release())
                    .subscribe(sampleValue -> {
                        postHandler(sampleValue);
                    }, throwable -> {
                        //这里把sample()异常重新抛出，让线程池的afterExecute进行处理，实现统计功能
                        doThrow(throwable);
                    });

        } else {
            logger.error("previous task don't finish");
        }
    }

    @Override
    public int compareTo(Collector other) {
        if (this == other) {
            return 0;
        }
        if (other == null) {
            return -1;
        }
        return this.priority.compare(other.priority);
    }

    private void postHandler(SampleValue sampleValue) {
        logger.info("post handler..................");
        String key = sampleValue.getMo().getMoType() + "." + sampleValue.getMetrics().getName() + sampleValue.getMo().getId();
        metricsValueCache.put(key, sampleValue);
    }

    // 只能抛出RuntimeException,这里封装一下,让编译器编译通过
    private <E extends Throwable> void doThrow(Throwable t) throws E {
        throw (E) t;
    }

    public enum Priority {
        URGENT("Urgent", 10),
        HIGH("High", 5),
        MEDIUM("Medium", 2),
        LOW("Low", 0),
        DEFAULT("Default", -1);

        @Getter
        private final String name;

        private final int value;

        Priority(String name, int value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return getName();
        }

        public int compare(Priority that) {
            // 顺序是降序
            return that.value - this.value;
        }
    }

}
