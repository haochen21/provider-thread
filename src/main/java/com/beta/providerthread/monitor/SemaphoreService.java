package com.beta.providerthread.monitor;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

@Service
public class SemaphoreService {

    private ConcurrentHashMap<String, Semaphore> semaphoreMap;

    public SemaphoreService(){
        semaphoreMap = new ConcurrentHashMap<>();
    }

    public Semaphore getSemaphore(String name){
        if(!semaphoreMap.containsKey(name)){
            Semaphore semaphore = new Semaphore(1);
            semaphoreMap.putIfAbsent(name,semaphore);
        }
        return semaphoreMap.get(name);
    }
}
