package com.beta.providerthread.poller;

import com.beta.providerthread.model.HitLog;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.*;

@Service
@NoArgsConstructor
public class HitLogPoller {

    private ScheduledExecutorService executor;

    private static final Logger logger = LoggerFactory.getLogger(HitLogPoller.class);

    @PostConstruct
    public void init() {
        ThreadFactory threadFactory = new HitLogPollerThreadFactory();
        executor = new ScheduledThreadPoolExecutor(1, threadFactory);
    }

    /**
     * 采集时间到后，把hitLog加入到采集线程池
     */
    public void addHitLog(HitLog hitLog) {
        executor.scheduleAtFixedRate(new HitLogTask(hitLog),
                0, hitLog.getRule().getSampleInterval(), TimeUnit.SECONDS);
    }


    private static class HitLogPollerThreadFactory implements ThreadFactory {

        private static final String HitLogPoolerThreadName = "HitLogPoller";

        private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

        public Thread newThread(Runnable r) {
            Thread thread = defaultFactory.newThread(r);
            thread.setName(HitLogPoolerThreadName);
            thread.setDaemon(true);
            return thread;
        }
    }

    @AllArgsConstructor
    private class HitLogTask implements Runnable {

        private HitLog hitLog;

        @Override
        public void run() {
            logger.info("{} sample span is ok.......", hitLog);


        }

    }
}
