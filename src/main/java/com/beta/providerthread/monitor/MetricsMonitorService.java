package com.beta.providerthread.monitor;

import com.beta.providerthread.concurrent.HitLogPoller;
import com.beta.providerthread.model.Metrics;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class MetricsMonitorService {

    private ConcurrentHashMap<String, MetricsMonitorInfo> cache;

    private ScheduledThreadPoolExecutor executor;

    private static final Logger logger = LoggerFactory.getLogger(MetricsMonitorService.class);

    public MetricsMonitorService() {
        cache = new ConcurrentHashMap<>();
        executor = new ScheduledThreadPoolExecutor(1);
        executor.scheduleAtFixedRate(new MonitorStatistic(),
                0, 60, TimeUnit.SECONDS);
    }

    public MetricsMonitorInfo getMetricsMonitorInfo(Metrics metrics) {
        String key = metrics.getMoType() + "." + metrics.getName();
        return cache.computeIfAbsent(key,
                k -> {
                    MetricsMonitorInfo info = new MetricsMonitorInfo();
                    info.setMoType(metrics.getMoType().toString());
                    info.setMetricName(metrics.getName());
                    return info;
                });
    }

    private class MonitorStatistic implements Runnable {

        @Override
        public void run() {
            cache.forEach((k, v) -> {
                try{
                    logger.info("statistic info: {}." , v.getStatisticInfo());
                    v.clear();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            });


        }
    }
}
