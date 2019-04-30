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
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

@Setter
@Getter
public class Collector implements Runnable {

    private MetricsValueCache metricsValueCache;

    private ProviderThreadPool threadPool;

    private CircuitBreakerService circuitBreakerService;

    private SemaphoreService semaphoreService;

    private HitLog hitLog;

    private static final Logger logger = LoggerFactory.getLogger(Collector.class);

    public Collector(MetricsValueCache metricsValueCache, CircuitBreakerService circuitBreakerService,
                     SemaphoreService semaphoreService, HitLog hitLog) {
        this.metricsValueCache = metricsValueCache;
        this.circuitBreakerService = circuitBreakerService;
        this.semaphoreService = semaphoreService;
        this.hitLog = hitLog;
    }


    @Override
    public void run() {
        String key = hitLog.getRule().getId() + "." + hitLog.getMo().getId();
        Semaphore semaphore = semaphoreService.getSemaphore(key);

        if (semaphore.tryAcquire()) {
            CircuitBreaker circuitBreaker = circuitBreakerService.
                    getCircuitBreaker(hitLog.getRule().getMetrics().getName());

            CacheMetricsProvider provider = new CacheMetricsProvider();
            //TimeoutMetricProvider provider = new TimeoutMetricProvider();
            // 如果upstream返回一个mono,在断路状态下，upstream还会被调用

            Callable<SampleValue> callable = () -> provider.sample(this.hitLog.getMo(), this.hitLog.getRule().getMetrics());
            Mono.fromCallable(callable)
                    .transform(CircuitBreakerOperator.of(circuitBreaker))
                    .timeout(Duration.ofSeconds(2))
                    .subscribe(sampleValue -> {
                        semaphore.release();
                    }, throwable -> {
                        circuitBreaker.onError(
                                2000, throwable);
                        logger.error("@@@@@@@@@@@@@@@@@" + throwable);
                        logger.info("^^^^^^^^^^^^^^^^^^^^^^^^^^" + circuitBreaker.getState().toString());
                        semaphore.release();
                        //这里把异常重新抛出，让线程池的afterExecute进行处理，实现统计功能
                        doThrow(throwable);
                    });

        } else {
            logger.error("previous task don't finish");
        }
    }

    // 只能抛出RuntimeException,这里封装一下,让编译器编译通过
    private <E extends Throwable> void doThrow(Throwable t) throws E {
        throw (E) t;
    }

}
