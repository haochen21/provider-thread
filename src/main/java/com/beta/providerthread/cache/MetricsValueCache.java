package com.beta.providerthread.cache;

import com.beta.providerthread.model.SampleValue;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@NoArgsConstructor
public class MetricsValueCache {

    private ConcurrentHashMap<String, SampleValue> cache;

    @PostConstruct
    public void init() {
        cache = new ConcurrentHashMap<>();
    }

    public void put(String key, SampleValue sampleValue) {
        cache.put(key, sampleValue);
    }

    public Optional<SampleValue> get(String key) {
        return Optional.ofNullable(cache.get(key));
    }
}
