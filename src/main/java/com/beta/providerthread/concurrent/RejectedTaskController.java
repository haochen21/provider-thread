package com.beta.providerthread.concurrent;

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
            Collector collector = providerTask.getCollector();
            logger.info("reject,{}",collector.getHitLog().toString());
        }
    }
}
