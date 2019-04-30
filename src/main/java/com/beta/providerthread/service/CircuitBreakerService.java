package com.beta.providerthread.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A CircuitBreaker can be in one of the three states:
 *
 * CLOSED – 服务正常，不需要进行短路
 * OPEN – 远程服务宕机，所有请求都短路
 * HALF_OPEN – 进入打开状态一段时间后，熔断器允许检查远程服务是否恢复
 *
 * 触发熔断的失败率阈值
 * 熔断器从打开状态到半开状态的等待时间
 * 熔断器在半开状态时环状缓冲区的大小
 * 熔断器在关闭状态时环状缓冲区的大小,达到值后才计算失败率阈值
 */
@Service
public class CircuitBreakerService {

    private CircuitBreakerRegistry circuitBreakerRegistry;

    private ConcurrentHashMap<String, CircuitBreaker> circuitBreakerMap;

    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerService.class);

    public CircuitBreakerService(){
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMillis(60*1000))
                .ringBufferSizeInHalfOpenState(2)
                .ringBufferSizeInClosedState(2)
                .build();
        circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig);

        circuitBreakerMap = new ConcurrentHashMap<>();
    }

    public CircuitBreaker getCircuitBreaker(String name){
        if(!circuitBreakerMap.containsKey(name)){
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(name);
            circuitBreakerMap.putIfAbsent(name,circuitBreaker);
        }
        return circuitBreakerMap.get(name);
    }

}
