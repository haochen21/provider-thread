package com.beta.providerthread.concurrent;

import com.beta.providerthread.model.Metrics;
import com.beta.providerthread.model.Mo;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class Collector implements Runnable {

    private Mo mo;

    private Metrics metrics;


    @Override
    public void run() {

    }
}
