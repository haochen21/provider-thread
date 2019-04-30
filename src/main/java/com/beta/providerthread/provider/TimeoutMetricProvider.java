package com.beta.providerthread.provider;

import com.beta.providerthread.model.Metrics;
import com.beta.providerthread.model.Mo;
import com.beta.providerthread.model.SampleValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class TimeoutMetricProvider implements MetricsProvider {

    private static final Logger logger = LoggerFactory.getLogger(TimeoutMetricProvider.class);

    @Override
    public SampleValue sample(Mo mo, Metrics metrics) {
        logger.info("start sample,mo: {},metrics: {}", mo, metrics);
        Mono.just("chenhao").delayElement(Duration.ofSeconds(4)).block();
        logger.info("end sample,mo: {},metrics: {}", mo, metrics);
        return null;
    }
}
