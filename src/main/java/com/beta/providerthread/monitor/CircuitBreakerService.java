package com.beta.providerthread.monitor;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;


/**
 * A CircuitBreaker can be in one of the three states:
 *
 * CLOSED – everything is fine, no short-circuiting involved
 * OPEN – remote server is down, all requests to it are short-circuited
 * HALF_OPEN – a configured amount of time since entering OPEN state has elapsed
 *     and CircuitBreaker allows requests to check if the remote service is back online
 *
 * the failure rate threshold above which the CircuitBreaker opens and starts short-circuiting calls
 * the wait duration which defines how long the CircuitBreaker should stay open before it switches to half open
 * the size of the ring buffer when the CircuitBreaker is half open or closed
 * a custom Predicate which evaluates if an exception should count as a failure and thus increase the failure rate
 */
@Service
public class CircuitBreakerService {

    private CircuitBreakerRegistry circuitBreakerRegistry;

    private ConcurrentHashMap<String, CircuitBreaker> circuitBreakerMap;

    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerService.class);

    public CircuitBreakerService(){
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMillis(1000))
                .ringBufferSizeInHalfOpenState(2)
                .ringBufferSizeInClosedState(4)
                .build();
        circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig);

        circuitBreakerMap = new ConcurrentHashMap<>();
    }

    public CircuitBreaker createCircuitBreaker(String name){
        if(!circuitBreakerMap.containsKey(name)){
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(name);
            circuitBreakerMap.putIfAbsent(name,circuitBreaker);
        }
        return circuitBreakerMap.get(name);
    }

    public static void main(String[] args) {

    }
}
