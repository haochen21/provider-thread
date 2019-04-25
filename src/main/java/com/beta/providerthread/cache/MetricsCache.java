package com.beta.providerthread.cache;

import com.beta.providerthread.model.Metrics;
import com.beta.providerthread.service.MetricsService;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@NoArgsConstructor
public class MetricsCache implements CacheInit {

    @Autowired
    private MetricsService metricsService;

    private ConcurrentHashMap<String, Metrics> cache;

    private static final Logger logger = LoggerFactory.getLogger(MetricsCache.class);

    public MetricsCache(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    public Runnable load() {
        return () -> {
            logger.info("load metrics cache start.......");
            sleep(2000);
            cache = metricsService.findAll().stream().collect(
                    Collectors.toMap(metrics -> metrics.getMoType().toString()+"-"+metrics.getName(),
                            Function.identity(), (m1, m2) -> m1, ConcurrentHashMap::new));
            logger.info("load metrics cache end.......");
        };
    }

    public Metrics get(String key) {
        return cache.get(key);
    }
}
