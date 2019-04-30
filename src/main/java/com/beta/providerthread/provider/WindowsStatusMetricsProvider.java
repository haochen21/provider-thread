package com.beta.providerthread.provider;

import com.beta.providerthread.model.Metrics;
import com.beta.providerthread.model.Mo;
import com.beta.providerthread.model.SampleValue;
import com.beta.providerthread.model.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class WindowsStatusMetricsProvider implements MonoMetricsProvider {

    private static final Logger logger = LoggerFactory.getLogger(WindowsStatusMetricsProvider.class);

    @Override
    public Mono<SampleValue> sample(Mo mo, Metrics metrics) {
        logger.info("mo: {},metrics: {}", mo, metrics);
        try {
            int sleep = new Random().nextInt(2000) + 1000;
            Thread.sleep(sleep);

            SampleValue sampleValue = new SampleValue();
            sampleValue.setMo(mo);
            sampleValue.setMetrics(metrics);
            sampleValue.setType(ValueType.INTEGER);
            sampleValue.setValue(sleep);
            sampleValue.setSampleTime(LocalDateTime.now());

            return Mono.just(sampleValue);
        } catch (Exception ex) {
            logger.error("find omHitLog error!", ex);
            return Mono.empty();
        }
    }
}
