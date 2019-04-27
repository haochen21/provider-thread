package com.beta.providerthread.collect;

public interface Collector extends Runnable {

    // 任务开始执行前调用
    void executeBefore(Thread t);

    // 任务执行完成或发生异常时调用
    void executeAfter(Throwable t);

    // 线程池队列满，拒绝新加入任务时调用
    void reject(Runnable r);

}
