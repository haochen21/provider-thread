package com.beta.providerthread.cache;

import com.beta.providerthread.mock.MockMetricsServiceImpl;
import com.beta.providerthread.mock.MockMoServiceImpl;
import com.beta.providerthread.mock.MockRuleServiceImpl;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;

@Setter
@Service
public class CacheCreateService {

    @Autowired
    private MoCache moCache;

    @Autowired
    private MetricsCache metricsCache;

    @Autowired
    private RuleCache ruleCache;

    @Autowired
    private HitLogCache hitLogCache;

    private static final Logger logger = LoggerFactory.getLogger(CacheCreateService.class);

    public CacheCreateService(){

    }

    @PostConstruct
    public void init() {

        // 1.装载MO
        // 2.同步装载指标和规则
        // 3.装载hitLog
        CompletableFuture<Void> cf = CompletableFuture.runAsync(moCache.load())
                .thenCompose(s -> CompletableFuture.allOf(CompletableFuture.runAsync(metricsCache.load()),
                        CompletableFuture.runAsync(ruleCache.load())))
                .thenRun(hitLogCache.load());

        cf.join();
    }

    public static void main(String[] args) {
        MoCache moCache = new MoCache(new MockMoServiceImpl());
        MetricsCache metricsCache = new MetricsCache(new MockMetricsServiceImpl());
        RuleCache ruleCache = new RuleCache(new MockRuleServiceImpl());
        HitLogCache hitLogCache = new HitLogCache();

        CacheCreateService service = new CacheCreateService();
        service.setMoCache(moCache);
        service.setMetricsCache(metricsCache);
        service.setRuleCache(ruleCache);
        service.setHitLogCache(hitLogCache);

        service.init();
    }

}
