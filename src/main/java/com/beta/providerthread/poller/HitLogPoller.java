package com.beta.providerthread.poller;

import com.beta.providerthread.cache.AlarmHitLogCache;
import com.beta.providerthread.cache.OmHitLogCache;
import com.beta.providerthread.eventbus.HitLogCacheEvent;
import com.beta.providerthread.model.HitLog;
import com.google.common.eventbus.Subscribe;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.*;

@Service
@Setter
@NoArgsConstructor
public class HitLogPoller {

    @Autowired
    OmHitLogCache omHitLogCache;

    @Autowired
    AlarmHitLogCache alarmHitLogCache;

    ScheduledExecutorService executor;

    private static final Logger logger = LoggerFactory.getLogger(HitLogPoller.class);

    @PostConstruct
    public void init() {
        ThreadFactory threadFactory = new HitLogPollerThreadFactory();
        executor = new ScheduledThreadPoolExecutor(1, threadFactory);
    }

    @Subscribe
    public void handlerHitLogCache(HitLogCacheEvent hitLogCacheEvent){
        logger.info("hitLog cache finished....");
        omHitLogCache.getCache().forEach((k, v) -> logger.info(v.toString()));
        alarmHitLogCache.getCache().forEach((k, v) -> logger.info(v.toString()));
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
