package com.beta.providerthread.concurrent;

import com.beta.providerthread.collect.Collector;
import com.beta.providerthread.collect.CollectorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class RejectedTaskController implements RejectedExecutionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RejectedTaskController.class);

    @Override
    public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {
        if (task instanceof Collector) {
            ProviderTask providerTask = (ProviderTask) task;
            providerTask.getCollector();
            CollectorImpl collector = (CollectorImpl) providerTask.getCollector();
            collector.reject(task);
        }
    }
}
