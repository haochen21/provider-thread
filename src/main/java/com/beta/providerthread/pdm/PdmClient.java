package com.beta.providerthread.pdm;

import com.beta.providerthread.model.Metrics;
import com.beta.providerthread.model.Mo;
import com.beta.providerthread.model.SampleValue;
import com.beta.providerthread.model.ValueType;
import com.beta.providerthread.reactor.HitLogReactor;
import com.beta.providerthread.service.WebClientService;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;

@Service
@NoArgsConstructor
@Setter
public class PdmClient {

    private WebClientService webClientService;

    private static final Logger logger = LoggerFactory.getLogger(PdmClient.class);

    public Mono<SampleValue> mockSample(Mo mo, Metrics metrics) {
        return Mono.just(new Random().nextInt(10000))
                .map(random -> {
                    if (random > 20000) {
                        throw new RuntimeException("" + random);
                    }
                    SampleValue sampleValue = new SampleValue();
                    sampleValue.setMo(mo);
                    sampleValue.setMetrics(metrics);
                    sampleValue.setType(ValueType.INTEGER);
                    sampleValue.setValue(random);
                    sampleValue.setSampleTime(LocalDateTime.now());
                    logger.info("111111");
                    return sampleValue;
                }).delayElement(Duration.ofMillis(new Random().nextInt(500) + 1000));
    }

    public Mono<SampleValue> sampling(Mo mo, Metrics metrics) {
        Mono<SampleValue> result = webClientService.getPdmWebclient().get()
                .uri("/pdm/client/sampling?latency=" + (new Random().nextInt(500)+1000))
                .retrieve()
                .bodyToMono(Integer.class)
                .map(value -> {
                    SampleValue sampleValue = new SampleValue();
                    sampleValue.setMo(mo);
                    sampleValue.setMetrics(metrics);
                    sampleValue.setType(ValueType.INTEGER);
                    sampleValue.setValue(value);
                    sampleValue.setSampleTime(LocalDateTime.now());
                    logger.info("111111");
                    return sampleValue;
                });

        return result;
    }
}
