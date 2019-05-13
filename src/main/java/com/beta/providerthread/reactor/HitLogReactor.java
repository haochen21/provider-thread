package com.beta.providerthread.reactor;

import com.beta.providerthread.cache.AlarmHitLogCache;
import com.beta.providerthread.cache.OmHitLogCache;
import com.beta.providerthread.eventbus.HitLogCacheEvent;
import com.beta.providerthread.model.HitLog;
import com.beta.providerthread.model.Metrics;
import com.beta.providerthread.model.SampleValue;
import com.beta.providerthread.monitor.MetricsMonitorService;
import com.beta.providerthread.pdm.PdmClient;
import com.beta.providerthread.service.CircuitBreakerService;
import com.beta.providerthread.service.SemaphoreService;
import com.google.common.eventbus.Subscribe;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerOpenException;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Setter
public class HitLogReactor {

    @Autowired
    OmHitLogCache omHitLogCache;

    @Autowired
    AlarmHitLogCache alarmHitLogCache;

    @Autowired
    CircuitBreakerService circuitBreakerService;

    @Autowired
    SemaphoreService semaphoreService;

    @Autowired
    MetricsMonitorService metricsMonitorService;

    // key: ruleId+"."+moId
    ConcurrentHashMap<String, ScheduledFuture> futureMap;

    ScheduledThreadPoolExecutor executor;

    Scheduler bizProcessPool;

    Scheduler samplingPool;

    private static final Logger logger = LoggerFactory.getLogger(HitLogReactor.class);

    @PostConstruct
    public void init() {
        ThreadFactory threadFactory = new HitLogReactor.HitLogReactorThreadFactory();
        executor = new ScheduledThreadPoolExecutor(2, threadFactory);
        // 指定任务可以从定时器里删除
        executor.setRemoveOnCancelPolicy(true);

        futureMap = new ConcurrentHashMap<>();

        samplingPool = Schedulers.newElastic("sampling");
        bizProcessPool = Schedulers.newParallel("bizProcess", 10);
    }

    @Subscribe
    public void handlerHitLogCache(HitLogCacheEvent hitLogCacheEvent) {
        logger.info("hitLog cache finished....");
        omHitLogCache.getCache().forEach((key, omHitLog) -> {
            logger.info(omHitLog.toString());
            addHitLog(omHitLog);
        });
        alarmHitLogCache.getCache().forEach((key, alarmHitLog) -> {
            logger.info(alarmHitLog.toString());
            addHitLog(alarmHitLog);
        });
    }

    public void addHitLog(HitLog hitLog) {
        ScheduledFuture future = executor.scheduleAtFixedRate(new HitLogReactor.HitLogTask(hitLog),
                0, hitLog.getRule().getSampleInterval(), TimeUnit.SECONDS);
        String key = hitLog.getRuleId() + "." + hitLog.getMoId();
        futureMap.put(key, future);
    }

    @AllArgsConstructor
    private class HitLogTask implements Runnable {

        private HitLog hitLog;

        @Override
        public void run() {
            logger.info("hitLogTask: {}", hitLog);
            BulkheadConfig config = BulkheadConfig.custom()
                    .maxConcurrentCalls(1)
                    .maxWaitTime(0)
                    .build();
            BulkheadRegistry registry = BulkheadRegistry.of(config);
            Bulkhead bulkhead = registry.bulkhead("foo");
            CircuitBreaker circuitBreaker = circuitBreakerService.
                    getCircuitBreaker(hitLog.getRule().getMetrics());

            PdmClient pdmClient = new PdmClient();

            pdmClient.sample(hitLog.getMo(), this.hitLog.getRule().getMetrics())
                    .publishOn(bizProcessPool)
                    .transform(CircuitBreakerOperator.of(circuitBreaker))
                    .timeout(Duration.ofSeconds(1))
                    .doOnError(error -> {
                        Metrics metrics = this.hitLog.getRule().getMetrics();
                        if (error instanceof TimeoutException) {
                            circuitBreaker.onError(
                                    2 * 1000, error);
                            metricsMonitorService.getMetricsMonitorInfo(metrics).getTimeout().addAndGet(1);
                        }else if (error instanceof CircuitBreakerOpenException) {
                            metricsMonitorService.getMetricsMonitorInfo(metrics).getNotPermitted().addAndGet(1);
                        }else {
                            metricsMonitorService.getMetricsMonitorInfo(metrics).getError().addAndGet(1);
                        }
                    })
                    .subscribeOn(samplingPool)
                    .elapsed()
                    .subscribe(tuple2 -> {
                        Metrics metrics = this.hitLog.getRule().getMetrics();
                        metricsMonitorService.getMetricsMonitorInfo(metrics).getSuccess().addAndGet(1);

                        metricsMonitorService.getMetricsMonitorInfo(metrics).getServiceTime().addAndGet(tuple2.getT1());
                        if (metricsMonitorService.getMetricsMonitorInfo(metrics).getMaxServiceTime().get() < tuple2.getT1()) {
                            metricsMonitorService.getMetricsMonitorInfo(metrics).getMaxServiceTime().set(tuple2.getT1());
                        }
                    });

        }
    }

    private void postHandler(SampleValue sampleValue) {
        logger.info("post handler...." + sampleValue.toString());
        String key = sampleValue.getMo().getMoType() + "." + sampleValue.getMetrics().getName() + sampleValue.getMo().getId();

    }

    private static class HitLogReactorThreadFactory implements ThreadFactory {

        private final AtomicInteger threadNumber = new AtomicInteger(1);

        private static final String HitLogPoolerThreadName = "HitLogReactor";

        private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

        public Thread newThread(Runnable r) {
            Thread thread = defaultFactory.newThread(r);
            thread.setName(HitLogPoolerThreadName + "-" + threadNumber.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    }
}
