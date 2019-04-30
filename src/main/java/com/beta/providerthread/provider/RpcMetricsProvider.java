package com.beta.providerthread.provider;

import com.beta.providerthread.model.Metrics;
import com.beta.providerthread.model.Mo;
import com.beta.providerthread.model.SampleValue;
import com.beta.providerthread.model.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class RpcMetricsProvider implements MetricsProvider {

    private static final Logger logger = LoggerFactory.getLogger(RpcMetricsProvider.class);

    @Override
    public SampleValue sample(Mo mo, Metrics metrics) {
        logger.info("start sample,mo: {},metrics: {}",mo,metrics);
        try {
            int sleep = new Random().nextInt(1000);
            Thread.sleep(sleep);

            int fault = new Random().nextInt(10);
            if(fault%2==0){
                throw new RestClientException("timeout");
            }
            SampleValue sampleValue = new SampleValue();
            sampleValue.setMo(mo);
            sampleValue.setMetrics(metrics);
            sampleValue.setType(ValueType.INTEGER);
            sampleValue.setValue(sleep);
            sampleValue.setSampleTime(LocalDateTime.now());

            logger.info("end sample,mo: {},metrics: {}",mo,metrics);

            return sampleValue;
        }catch (InterruptedException ex) {
            logger.error("find omHitLog error!", ex.getMessage());
            return null;
        }
    }
}
