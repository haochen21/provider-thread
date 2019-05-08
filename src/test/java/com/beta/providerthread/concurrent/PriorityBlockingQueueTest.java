package com.beta.providerthread.concurrent;

import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.PriorityBlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;

public class PriorityBlockingQueueTest {

    @Test
    public void testOrderQueue() throws InterruptedException {
        PriorityBlockingQueue<ProviderTask> queue = new PriorityBlockingQueue<>();

        queue.add(new ProviderTask(new Collector(null,null,null,null,0, Collector.Priority.DEFAULT)));
        queue.add(new ProviderTask(new Collector(null,null,null,null,0, Collector.Priority.MEDIUM)));
        queue.add(new ProviderTask(new Collector(null,null,null,null,0, Collector.Priority.DEFAULT)));
        queue.add(new ProviderTask(new Collector(null,null,null,null,0, Collector.Priority.URGENT)));
        queue.add(new ProviderTask(new Collector(null,null,null,null,0, Collector.Priority.LOW)));
        queue.add(new ProviderTask(new Collector(null,null,null,null,0, Collector.Priority.HIGH)));

        assertThat(queue.take().getCollector().getPriority()).isEqualTo(Collector.Priority.URGENT);
        assertThat(queue.take().getCollector().getPriority()).isEqualTo(Collector.Priority.HIGH);
        assertThat(queue.take().getCollector().getPriority()).isEqualTo(Collector.Priority.MEDIUM);
        assertThat(queue.take().getCollector().getPriority()).isEqualTo(Collector.Priority.LOW);
        assertThat(queue.take().getCollector().getPriority()).isEqualTo(Collector.Priority.DEFAULT);
        assertThat(queue.take().getCollector().getPriority()).isEqualTo(Collector.Priority.DEFAULT);
    }

    @Test
    public void testReplaceOrderQueue() throws InterruptedException {
        PriorityBlockingQueue<ProviderTask> queue = new PriorityBlockingQueue<>(3);

        queue.add(new ProviderTask(new Collector(null,null,null,null,0, Collector.Priority.DEFAULT)));
        queue.add(new ProviderTask(new Collector(null,null,null,null,0, Collector.Priority.MEDIUM)));
        queue.add(new ProviderTask(new Collector(null,null,null,null,0, Collector.Priority.DEFAULT)));

        ProviderTask providerTask = new ProviderTask(new Collector(null,null,null,null,0, Collector.Priority.URGENT));
        do {
            ProviderTask[] providerTasks = queue.toArray(new ProviderTask[0]);
            Arrays.sort(providerTasks);
            int compare = providerTask.compareTo(providerTasks[providerTasks.length - 1]);
            if (compare < 0) {
                queue.remove(providerTasks[providerTasks.length - 1]);
            } else {
                break;
            }
        } while (!queue.offer(providerTask));


        assertThat(queue.take().getCollector().getPriority()).isEqualTo(Collector.Priority.URGENT);
        assertThat(queue.take().getCollector().getPriority()).isEqualTo(Collector.Priority.MEDIUM);
        assertThat(queue.take().getCollector().getPriority()).isEqualTo(Collector.Priority.DEFAULT);
    }
}
