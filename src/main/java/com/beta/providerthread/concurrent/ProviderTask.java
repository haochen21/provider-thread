package com.beta.providerthread.concurrent;

import com.beta.providerthread.collect.Collector;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.FutureTask;

@Getter
@Setter
public class ProviderTask<V> extends FutureTask<V> {

    private Collector collector;

    public ProviderTask(Runnable runnable) {
        super(runnable, null);
        if (runnable instanceof Collector) {
            this.collector = (Collector) runnable;
        }
    }
}
