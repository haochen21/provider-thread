package com.beta.providerthread.concurrent;

import com.beta.providerthread.collect.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class RejectedTaskController implements RejectedExecutionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RejectedTaskController.class);

    @Override
    public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {
        Collector collector = (Collector)task;
        logger.info("reject,metrics: {},mo: {}",collector.getMetrics(),collector.getMo());
    }
}
