package com.beta.providerthread.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HitLogCache implements CacheInit {

    private static final Logger logger = LoggerFactory.getLogger(HitLogCache.class);

    @Override
    public Runnable load() {
        return () -> {
            logger.info("load hitLog cache start.......");
            sleep(1000);

            logger.info("load hitLog cache end.......");
        };
    }
}
