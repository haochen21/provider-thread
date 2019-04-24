package com.beta.providerthread.collect;

import com.beta.providerthread.cache.MetricsValueCache;
import com.beta.providerthread.model.Metrics;
import com.beta.providerthread.model.Mo;
import com.beta.providerthread.model.SampleValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Collector implements Runnable {

    private Mo mo;

    private Metrics metrics;

    private MetricsValueCache metricsValueCache;

    public abstract SampleValue execute();

    @Override
    public void run() {
        SampleValue sampleValue = execute();
        metricsValueCache.put(sampleValue.getMetrics() + "-" + sampleValue.getMo(), sampleValue);
    }

}
