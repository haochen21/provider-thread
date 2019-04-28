package com.beta.providerthread.provider;

import com.beta.providerthread.model.Metrics;
import com.beta.providerthread.model.Mo;
import com.beta.providerthread.model.SampleValue;

import java.util.concurrent.TimeoutException;

public class TimeoutMetricProvider implements MetricsProvider {

    @Override
    public SampleValue sample(Mo mo, Metrics metrics) throws Exception {
        throw new TimeoutException();
    }
}
