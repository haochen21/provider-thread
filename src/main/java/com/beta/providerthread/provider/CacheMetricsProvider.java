package com.beta.providerthread.provider;

import com.beta.providerthread.model.Metrics;
import com.beta.providerthread.model.Mo;
import com.beta.providerthread.model.SampleValue;
import com.beta.providerthread.model.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class CacheMetricsProvider implements MetricsProvider {

    private static final Logger logger = LoggerFactory.getLogger(CacheMetricsProvider.class);

    @Override
    public SampleValue sample(Mo mo, Metrics metrics) {
        logger.info("start sample,mo: {},metrics: {}", mo, metrics);
        try {
            int sleep = new Random().nextInt(2000) + 1000;
            logger.info("sleep is : {}", sleep);
            Thread.sleep(sleep);
            SampleValue sampleValue = new SampleValue();
            sampleValue.setMo(mo);
            sampleValue.setMetrics(metrics);
            sampleValue.setType(ValueType.INTEGER);
            sampleValue.setValue(sleep);
            sampleValue.setSampleTime(LocalDateTime.now());

            logger.info("end sample,mo: {},metrics: {}", mo, metrics);
            return sampleValue;

        }catch (Exception ex) {
            return null;
        }
    }

}
