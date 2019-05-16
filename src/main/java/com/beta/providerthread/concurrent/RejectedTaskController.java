package com.beta.providerthread.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class RejectedTaskController implements RejectedExecutionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RejectedTaskController.class);

    @Override
    public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {
        if (task instanceof Collector) {
            ProviderTask providerTask = (ProviderTask) task;
            Collector collector = providerTask.getCollector();
            logger.info("reject,{}", collector.getHitLog().toString());
//            do {
//                ProviderTask[] providerTasks = executor.getQueue().toArray(new ProviderTask[0]);
//                Arrays.sort(providerTasks);
//                int compare = providerTask.compareTo(providerTasks[providerTasks.length - 1]);
//                if (compare < 0) {
//                    logger.info("priority: {},replace priority: {}", collector.getPriority(),
//                            providerTasks[providerTasks.length - 1].getCollector().getPriority());
//                    executor.getQueue().remove(providerTasks[providerTasks.length - 1]);
//                } else {
//                    logger.info("priority: {},can't replace priority: {}", collector.getPriority(),
//                            providerTasks[providerTasks.length - 1].getCollector().getPriority());
//                    break;
//                }
//            } while (!executor.getQueue().offer(task));
        }
    }
}
