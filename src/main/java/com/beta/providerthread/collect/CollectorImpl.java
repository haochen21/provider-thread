package com.beta.providerthread.collect;

import com.beta.providerthread.cache.MetricsValueCache;
import com.beta.providerthread.concurrent.ProviderThreadPool;
import com.beta.providerthread.concurrent.ProviderTimer;
import com.beta.providerthread.model.Mo;
import com.beta.providerthread.model.Rule;

import com.beta.providerthread.model.SampleValue;
import com.beta.providerthread.provider.RpcMetricsProvider;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.util.concurrent.Future;

@Setter
@Getter
public class CollectorImpl implements Collector {

    private Mo mo;

    private Rule rule;

    private MetricsValueCache metricsValueCache;

    private ProviderThreadPool threadPool;

    private ProviderTimer.TimerListener timerListener;

    private Reference<ProviderTimer.TimerListener> timerReference;

    private Future future;

    private static final Logger logger = LoggerFactory.getLogger(CollectorImpl.class);

    public CollectorImpl(Mo mo, Rule rule, MetricsValueCache metricsValueCache, ProviderThreadPool threadPool, int timeout) {
        this.mo = mo;
        this.rule = rule;
        this.metricsValueCache = metricsValueCache;
        this.threadPool = threadPool;

        timerListener = new ProviderTimer.TimerListener() {

            @Override
            public void tick() {
                logger.info("{},{} timeout!!!!", mo, rule);
                // 已经超时，取消任务
                if (future != null) {
                    future.cancel(true);
                }
                throw new RuntimeException(mo.getMoType() + mo.getName() + rule.getMetrics().getName() + " timeout.");
            }

            @Override
            public int getIntervalTimeInMilliseconds() {
                return timeout;
            }
        };
    }

    @Override
    public void executeBefore(Thread t) {
        timerReference = ProviderTimer.getInstance().addTimerListener(timerListener);
    }

    @Override
    public void executeAfter(Throwable t) {
        close();
    }

    @Override
    public void reject(Runnable r) {

    }

    private void close() {
        timerReference.clear();
    }

    public void collect() {
        future = threadPool.submit(this);
    }

    @Override
    public void run() {
        RpcMetricsProvider provider = new RpcMetricsProvider();
        SampleValue sampleValue = provider.sample(this.getMo(), this.getRule().getMetrics());
        logger.info(sampleValue.toString());
    }
}
