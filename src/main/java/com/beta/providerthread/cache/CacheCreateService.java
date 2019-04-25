package com.beta.providerthread.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class CacheCreateService {

    @Autowired
    private MoCache moCache;

    @Autowired
    private MetricsCache metricsCache;

    @Autowired
    private RuleCache ruleCache;

    public CacheCreateService(){

    }

    @PostConstruct
    public void init(){

    }

}
