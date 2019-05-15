package com.beta.providerthread.monitor;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@Setter
@NoArgsConstructor
public class MetricsMonitorInfo {

    private String moType;

    private String metricName;

    private AtomicInteger success = new AtomicInteger();

    private AtomicInteger timeout = new AtomicInteger();

    private AtomicInteger notPermitted = new AtomicInteger();

    private AtomicInteger semaphore = new AtomicInteger();

    private AtomicInteger error = new AtomicInteger();

    private AtomicLong serviceTime = new AtomicLong();

    private AtomicLong maxServiceTime = new AtomicLong(0);

    public void clear() {
        maxServiceTime.set(0);
        success.set(0);
        timeout.set(0);
        notPermitted.set(0);
        semaphore.set(0);
        error.set(0);
        serviceTime.set(0);
    }

    public Map<String, Object> getStatisticInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("moType", moType);
        info.put("metricName", metricName);
        info.put("avgTime/Millis",getAvgTime());
        info.put("maxTime/Millis",this.getMaxServiceTime().get());
        info.put("success", success);
        info.put("timeout", timeout);
        info.put("notPermitted", notPermitted);
        info.put("semaphoreError", semaphore);
        info.put("error", error);

        return info;
    }

    private double getAvgTime(){
        if(this.getSuccess().get() == 0){
            return new Double(0.00);
        }
        long serverTimeInMillis = TimeUnit.NANOSECONDS.toMillis(this.getServiceTime().get());
        Double avg  =  (double)(this.getServiceTime().get())/(this.getSuccess().get());
        DecimalFormat df = new DecimalFormat("###.###");
        return Double.parseDouble(df.format(avg));
    }
}
