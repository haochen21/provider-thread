package com.beta.providerthread.service;

import com.beta.providerthread.model.HitLog;
import com.beta.providerthread.model.Metrics;
import com.beta.providerthread.monitor.MetricsMonitorInfo;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

@Service
public class SemaphoreService {

    private ConcurrentHashMap<String, Semaphore> cache;

    public SemaphoreService(){
        cache = new ConcurrentHashMap<>();
    }

    public Semaphore getSemaphore(HitLog hitLog){
        String key = hitLog.getRuleId()+"."+hitLog.getMoId();
        return cache.computeIfAbsent(key,
                k -> new Semaphore(1));
    }
}
