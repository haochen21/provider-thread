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
import com.beta.providerthread.service.WebClientService;
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

    @Autowired
    WebClientService webClientService;

    // key: ruleId+"."+moId
    ConcurrentHashMap<String, ScheduledFuture> futureMap;

    ScheduledThreadPoolExecutor executor;

    Scheduler bizProcessPool;

    Scheduler samplingPool;

    PdmClient pdmClient;

    private static final Logger logger = LoggerFactory.getLogger(HitLogReactor.class);

    @PostConstruct
    public void init() {
        ThreadFactory threadFactory = new HitLogReactor.HitLogReactorThreadFactory();
        executor = new ScheduledThreadPoolExecutor(2, threadFactory);
        // 指定任务可以从定时器里删除
        executor.setRemoveOnCancelPolicy(true);

        futureMap = new ConcurrentHashMap<>();

        samplingPool = Schedulers.newElastic("sampling");

        bizProcessPool = Schedulers.newParallel("bizProcess", 100);

        pdmClient = new PdmClient();
        pdmClient.setWebClientService(webClientService);
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
        ScheduledFuture future = executor.scheduleAtFixedRate(() -> sampling(hitLog),
                0, 10, TimeUnit.SECONDS);
        String key = hitLog.getRuleId() + "." + hitLog.getMoId();
        futureMap.put(key, future);
    }

    private void sampling(HitLog hitLog) {
        CircuitBreaker circuitBreaker = circuitBreakerService.
                getCircuitBreaker(hitLog.getRule().getMetrics());

        pdmClient.sampling(hitLog.getMo(), hitLog.getRule().getMetrics())
                .transform(CircuitBreakerOperator.of(circuitBreaker))
                .timeout(Duration.ofSeconds(2))
                .doOnError(error -> {
                    Metrics metrics = hitLog.getRule().getMetrics();
                    if (error instanceof TimeoutException) {
                        circuitBreaker.onError(
                                2 * 1000, error);
                        metricsMonitorService.getMetricsMonitorInfo(metrics).getTimeout().addAndGet(1);
                    } else if (error instanceof CircuitBreakerOpenException) {
                        metricsMonitorService.getMetricsMonitorInfo(metrics).getNotPermitted().addAndGet(1);
                    } else {
                        metricsMonitorService.getMetricsMonitorInfo(metrics).getError().addAndGet(1);
                    }
                })
                .publishOn(bizProcessPool)
                .elapsed()
                .map(tuple2-> {
                    logger.info("time: {}",tuple2.getT1());
                    Metrics metrics = hitLog.getRule().getMetrics();
                    metricsMonitorService.getMetricsMonitorInfo(metrics).getSuccess().addAndGet(1);

                    metricsMonitorService.getMetricsMonitorInfo(metrics).getServiceTime().addAndGet(tuple2.getT1());
                    if (metricsMonitorService.getMetricsMonitorInfo(metrics).getMaxServiceTime().get() < tuple2.getT1()) {
                        metricsMonitorService.getMetricsMonitorInfo(metrics).getMaxServiceTime().set(tuple2.getT1());
                    }

                    postHandler(tuple2.getT2());
                    return tuple2.getT2();
                })
                .subscribe();
    }

    private void postHandler(SampleValue sampleValue) {
        logger.info("start post handler...." + sampleValue.toString());
        String key = sampleValue.getMo().getMoType() + "." + sampleValue.getMetrics().getName() + sampleValue.getMo().getId();
        try{
            logger.info("key: {}",key);
            Thread.sleep(200);
            logger.info("end post handler...." + sampleValue.toString());
        }catch(Exception ex){
            ex.printStackTrace();
        }
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
