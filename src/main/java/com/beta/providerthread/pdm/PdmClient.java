package com.beta.providerthread.pdm;

import com.beta.providerthread.model.Metrics;
import com.beta.providerthread.model.Mo;
import com.beta.providerthread.model.SampleValue;
import com.beta.providerthread.model.ValueType;
import com.beta.providerthread.reactor.HitLogReactor;
import lombok.NoArgsConstructor;
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
public class PdmClient {

    private static final Logger logger = LoggerFactory.getLogger(PdmClient.class);

    public Mono<SampleValue> sample(Mo mo, Metrics metrics) {
        return Mono.subscriberContext()
                .map(context -> context.put("startTime",new Date().getTime()))
                .just(new Random().nextInt(10000))
                .log().map(random -> {
            SampleValue sampleValue = new SampleValue();
            sampleValue.setMo(mo);
            sampleValue.setMetrics(metrics);
            sampleValue.setType(ValueType.INTEGER);
            sampleValue.setValue(random);
            sampleValue.setSampleTime(LocalDateTime.now());
            logger.info("111111");
            return sampleValue;
        }).log().delayElement(Duration.ofMillis(new Random().nextInt(1000)));
    }
}
