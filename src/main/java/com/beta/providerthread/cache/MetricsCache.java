package com.beta.providerthread.cache;

import com.beta.providerthread.mock.MockMetricsServiceImpl;
import com.beta.providerthread.mock.MockMoTypeService;
import com.beta.providerthread.model.Metrics;
import com.beta.providerthread.model.MoType;
import com.beta.providerthread.service.MetricsService;
import com.beta.providerthread.service.MoTypeService;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@NoArgsConstructor
public class MetricsCache implements Function<Map<String, MoType>,
        Map<String, Metrics>> {

    private MetricsService metricsService;

    // key: metrics.id
    private ConcurrentHashMap<String, Metrics> cache;

    private static final Logger logger = LoggerFactory.getLogger(MetricsCache.class);

    public MetricsCache(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Override
    public Map<String, Metrics> apply(Map<String, MoType> moTypeMap) {
        logger.info("load metrics cache start.......");
        cache = metricsService.findAll().stream().map(metrics -> {
            MoType moType = moTypeMap.get(metrics.getCategoryName() + "." + metrics.getMoTypeName());
            metrics.setMoType(moType);
            return metrics;
        }).collect(Collectors.toMap(metrics -> metrics.getId(),Function.identity(), (m1, m2) -> m1, ConcurrentHashMap::new));
        logger.info("load metrics cache end.......");
        return cache;
    }

    public Metrics get(String key) {
        return cache.get(key);
    }

    public static void main(String[] args) {
        MoTypeService moTypeService = new MockMoTypeService();
        MoTypeCache moTypeCache = new MoTypeCache(moTypeService);

        MetricsService metricsService = new MockMetricsServiceImpl();
        MetricsCache metricsCache = new MetricsCache(metricsService);

        CompletableFuture<Map<String, MoType>> moTypeFuture = CompletableFuture
                .supplyAsync(moTypeCache);
        CompletableFuture<Map<String, Metrics>> metricsFuture = moTypeFuture.thenApplyAsync(metricsCache);
        metricsFuture.thenAcceptAsync(metricsMap ->
                metricsMap.forEach((k,v)-> logger.info(v.toString()))).join();
    }

}
