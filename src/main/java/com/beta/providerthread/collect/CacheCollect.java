package com.beta.providerthread.collect;

import com.beta.providerthread.cache.MetricsValueCache;
import com.beta.providerthread.model.SampleValue;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class CacheCollect extends Collector {

    private MetricsValueCache metricsValueCache;

    @Override
    public SampleValue execute() {
        return null;
    }

}
