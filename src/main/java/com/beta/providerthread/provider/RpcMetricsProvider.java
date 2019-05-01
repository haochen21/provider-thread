package com.beta.providerthread.provider;

import com.beta.providerthread.model.Metrics;
import com.beta.providerthread.model.Mo;
import com.beta.providerthread.model.SampleValue;
import com.beta.providerthread.model.ValueType;
import com.beta.providerthread.service.WebClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RpcMetricsProvider implements MetricsProvider {

    private WebClientService webClientService;

    private static final Logger logger = LoggerFactory.getLogger(RpcMetricsProvider.class);

    public RpcMetricsProvider(WebClientService webClientService) {
        this.webClientService = webClientService;
    }

    @Override
    public SampleValue sample(Mo mo, Metrics metrics) {
        logger.info("start sample,mo: {},metrics: {}", mo, metrics);

        return this.webClientService.getPdmWebclient()
                .get()
                .uri("/{mo}/{metrics}", mo.getName(), metrics.getName())
                .retrieve()
                .bodyToMono(Integer.class)
                .map(value -> {
                    logger.info("end sample,mo: {},metrics: {}", mo, metrics);
                    SampleValue sampleValue = new SampleValue();
                    sampleValue.setMo(mo);
                    sampleValue.setMetrics(metrics);
                    sampleValue.setType(ValueType.INTEGER);
                    sampleValue.setValue(value);
                    sampleValue.setSampleTime(LocalDateTime.now());
                    return sampleValue;
                }).block();
    }
}
