package com.beta.providerthread.cache;

public interface CacheInit {

    Runnable load();

    default void sleep(int span) {
        try {
            Thread.sleep(span);
        } catch (Exception ex) {

        }
    }
}
