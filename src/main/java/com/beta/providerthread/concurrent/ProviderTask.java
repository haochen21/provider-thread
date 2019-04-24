package com.beta.providerthread.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ProviderTask<V> extends FutureTask {

    public ProviderTask(Callable callable) {
        super(callable);
    }
}
