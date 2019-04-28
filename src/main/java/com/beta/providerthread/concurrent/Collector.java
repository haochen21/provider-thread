package com.beta.providerthread.concurrent;

import com.beta.providerthread.cache.MetricsValueCache;
import com.beta.providerthread.model.HitLog;

import com.beta.providerthread.model.SampleValue;
import com.beta.providerthread.monitor.CircuitBreakerService;
import com.beta.providerthread.provider.RpcMetricsProvider;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;


@Setter
@Getter
public class Collector implements Runnable, Consumer<Integer> {

    private MetricsValueCache metricsValueCache;

    private ProviderThreadPool threadPool;

    private CircuitBreakerService circuitBreakerService;

    private Future future;

    private HitLog hitLog;

    private int timeout;

    private static final Logger logger = LoggerFactory.getLogger(Collector.class);

    public Collector(MetricsValueCache metricsValueCache, ProviderThreadPool threadPool,
                     CircuitBreakerService circuitBreakerService, HitLog hitLog) {
        this.metricsValueCache = metricsValueCache;
        this.threadPool = threadPool;
        this.circuitBreakerService = circuitBreakerService;
        this.hitLog = hitLog;
    }

    public void collect() {
        future = threadPool.submit(this);
    }

    @Override
    public void accept(Integer timeout) {
        future = threadPool.submit(this);
        try{
            future.get(timeout, TimeUnit.MILLISECONDS);
        }catch (TimeoutException ex){
            future.cancel(true);
            logger.error("TimeoutException");
            throw new RuntimeException();
        } catch (Exception ex){
            //ex.printStackTrace();
            throw new RuntimeException();
        }
    }

    @Override
    public void run() {
        RpcMetricsProvider provider = new RpcMetricsProvider();
        SampleValue sampleValue = provider.sample(this.hitLog.getMo(), this.hitLog.getRule().getMetrics());
        if(!future.isCancelled()){
            logger.info(sampleValue.toString());
        }

    }

}
