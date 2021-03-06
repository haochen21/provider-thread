package com.beta.providerthread.concurrent;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.FutureTask;

@Getter
@Setter
public class ProviderTask<V> extends FutureTask<V> implements
        Comparable<ProviderTask<V>> {

    private Collector collector;

    public ProviderTask(Collector collector) {
        super(collector, null);
        this.collector = collector;
    }

    @Override
    public int compareTo(ProviderTask<V> other) {
        return collector.compareTo(other.getCollector());
    }
}
