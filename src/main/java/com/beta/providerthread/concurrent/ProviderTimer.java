package com.beta.providerthread.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ProviderTimer {

    private AtomicReference<ScheduledExecutor> executor = new AtomicReference<>();

    private static ProviderTimer INSTANCE = new ProviderTimer();

    private static final Logger logger = LoggerFactory.getLogger(ProviderTimer.class);

    private ProviderTimer() {
        // 私有方法防止public初始化
    }

    /**
     * 获得全局实例对象
     * 任务执行完成后，需要关闭定时器
     * @return
     */
    public static ProviderTimer getInstance() {
        return INSTANCE;
    }

    public Reference<TimerListener> addTimerListener(final TimerListener listener) {

        startThreadIfNeeded();

        //超时的回调方法通过另一个线程调用
        Runnable r = ()-> listener.tick();

        // 任务指定超时时间，超过这个时间后，定时线程启动超时处理
        ScheduledFuture<?> f = executor.get().getThreadPool().scheduleAtFixedRate(r,
                listener.getIntervalTimeInMilliseconds(),
                listener.getIntervalTimeInMilliseconds(),
                TimeUnit.MILLISECONDS);
        return new TimerReference(listener, f);
    }

    protected void startThreadIfNeeded() {
        // 这里使用原子更新方法，保证多线程下只有一个实例更新成功
        while (executor.get() == null || !executor.get().isInitialized()) {
            if (executor.compareAndSet(null, new ScheduledExecutor())) {
                // 生成定时器线程池实例
                executor.get().initialize();
            }
        }
    }

    /**
     * 当内存不够的时候，JVM会清除这个对象
     */
    private static class TimerReference extends SoftReference<TimerListener> {

        private final ScheduledFuture<?> f;

        TimerReference(TimerListener referent, ScheduledFuture<?> f) {
            super(referent);
            this.f = f;
        }

        /**
         * 任务执行完成后，需要关闭定时器
         */
        @Override
        public void clear() {
            super.clear();
            // 取消定时器线程
            f.cancel(false);
        }

    }


    private static class ScheduledExecutor {

        private volatile ScheduledThreadPoolExecutor executor;

        private volatile boolean initialized;

        private int coreSize = 4;

        public void initialize() {

            ThreadFactory threadFactory = new ThreadFactory() {

                final AtomicInteger counter = new AtomicInteger();

                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "ProviderTimer-" + counter.incrementAndGet());
                    thread.setDaemon(true);
                    return thread;
                }
            };

            executor = new ScheduledThreadPoolExecutor(coreSize, threadFactory);
            initialized = true;
        }

        public ScheduledThreadPoolExecutor getThreadPool() {
            return executor;
        }

        public boolean isInitialized() {
            return initialized;
        }
    }

    public interface TimerListener {

        /**
         * 定时器启动的时候，调用这个方法
         * <p>
         * 这个方法应该在另一个线程启动
         */
        void tick();

        /**
         * tick方法执行等候时间
         */
        int getIntervalTimeInMilliseconds();
    }
}

