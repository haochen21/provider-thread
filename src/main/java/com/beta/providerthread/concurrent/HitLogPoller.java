package com.beta.providerthread.concurrent;

import com.beta.providerthread.cache.AlarmHitLogCache;
import com.beta.providerthread.cache.MetricsValueCache;
import com.beta.providerthread.cache.OmHitLogCache;
import com.beta.providerthread.eventbus.HitLogCacheEvent;
import com.beta.providerthread.model.HitLog;
import com.beta.providerthread.model.ProviderType;
import com.beta.providerthread.model.RuleType;
import com.beta.providerthread.monitor.MetricsMonitorService;
import com.beta.providerthread.service.CircuitBreakerService;
import com.beta.providerthread.service.SemaphoreService;
import com.google.common.eventbus.Subscribe;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Setter
@NoArgsConstructor
public class HitLogPoller {

    @Autowired
    OmHitLogCache omHitLogCache;

    @Autowired
    AlarmHitLogCache alarmHitLogCache;

    @Autowired
    MetricsValueCache metricsValueCache;

    @Autowired
    CircuitBreakerService circuitBreakerService;

    @Autowired
    SemaphoreService semaphoreService;

    @Autowired
    MetricsMonitorService metricsMonitorService;

    ScheduledThreadPoolExecutor executor;

    ProviderThreadPool threadPool;

    RestTemplate restTemplate;

    //key: ruleId+"."+moId
    ConcurrentHashMap<String, ScheduledFuture> futureMap;

    private static final Logger logger = LoggerFactory.getLogger(HitLogPoller.class);

    @PostConstruct
    public void init() {
        ThreadFactory threadFactory = new HitLogPollerThreadFactory();
        executor = new ScheduledThreadPoolExecutor(2, threadFactory);
        executor.setRemoveOnCancelPolicy(true);

        futureMap = new ConcurrentHashMap<>();

        threadPool = new ProviderThreadPool(metricsMonitorService);

        restTemplate = new RestTemplate();
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

    /**
     * 采集时间到后，把hitLog加入到采集线程池
     */
    public void addHitLog(HitLog hitLog) {
        if (hitLog.getRule().getMetrics().getProviderType() != ProviderType.RPC) {
            return;
        }
        ScheduledFuture future = executor.scheduleAtFixedRate(new HitLogTask(hitLog),
                0, hitLog.getRule().getSampleInterval(), TimeUnit.SECONDS);
        String key = hitLog.getRuleId() + "." + hitLog.getMoId();
        futureMap.put(key, future);
    }


    private static class HitLogPollerThreadFactory implements ThreadFactory {

        private final AtomicInteger threadNumber = new AtomicInteger(1);

        private static final String HitLogPoolerThreadName = "HitLogPoller";

        private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

        public Thread newThread(Runnable r) {
            Thread thread = defaultFactory.newThread(r);
            thread.setName(HitLogPoolerThreadName + "-" + threadNumber.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    }

    @AllArgsConstructor
    private class HitLogTask implements Runnable {

        private HitLog hitLog;

        @Override
        public void run() {
            logger.info("hitLogTask: {}", hitLog);
            if (hitLog.getRule().getMetrics().getProviderType() == ProviderType.RPC) {
                if (hitLog.getRule().getRuleType() == RuleType.OM) {
                    // 把任务加入线程池
                    Collector collector = new Collector(metricsValueCache,
                            circuitBreakerService, semaphoreService, hitLog, 2,0);
                    // 把任务提交到线程池，后面任务的处理与当前线程没有关系
                    threadPool.submit(collector);
                }
            }

        }
    }
}
