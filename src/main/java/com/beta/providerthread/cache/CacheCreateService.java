package com.beta.providerthread.cache;

import com.beta.providerthread.eventbus.EventBusService;
import com.beta.providerthread.eventbus.HitLogCacheEvent;
import com.beta.providerthread.mock.*;
import com.beta.providerthread.model.*;
import com.beta.providerthread.poller.HitLogPoller;
import com.beta.providerthread.service.*;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Setter
@Service
public class CacheCreateService {

    @Autowired
    MoTypeCache moTypeCache;

    @Autowired
    MoCache moCache;

    @Autowired
    MetricsCache metricsCache;

    @Autowired
    OmRuleCache omRuleCache;

    @Autowired
    AlarmRuleCache alarmRuleCache;

    @Autowired
    OmHitLogCache omHitLogCache;

    @Autowired
    AlarmHitLogCache alarmHitLogCache;

    @Autowired
    EventBusService eventBusService;

    private static final Logger logger = LoggerFactory.getLogger(CacheCreateService.class);

    public CacheCreateService(){

    }

    @PostConstruct
    public void load() {
        // 1.装载moType
        CompletableFuture<Map<String, MoType>> moTypeFuture = CompletableFuture
                .supplyAsync(moTypeCache);

        // 2.装载mo和metrics
        CompletableFuture<Map<String, Mo>> moFuture = moTypeFuture.thenApplyAsync(moCache);
        CompletableFuture<Map<String, Metrics>> metricsFuture = moTypeFuture.thenApplyAsync(metricsCache);
        CompletableFuture<Map<String, Metrics>> cf2 = moFuture.thenCombine(metricsFuture, (s, s2) -> s2);

        // 3.装载omRule,omHitLog
        CompletableFuture<Map<String, OmRule>> omRuleFuture = cf2.thenApplyAsync(omRuleCache);
        CompletableFuture<Map<String, OmHitLog>> omHitLogFuture = omRuleFuture.thenApplyAsync(omHitLogCache);

        // 3.装载alarmRule,alarmHitLog
        CompletableFuture<Map<String, AlarmRule>> alarmRuleFuture = cf2.thenApplyAsync(alarmRuleCache);
        CompletableFuture<Map<String, AlarmHitLog>> alarmHitLogFuture = alarmRuleFuture.thenApplyAsync(alarmHitLogCache);

        CompletableFuture.allOf(omHitLogFuture,alarmHitLogFuture).join();

        eventBusService.getEventBus().post(new HitLogCacheEvent());
    }

    public static void main(String[] args) {
        EventBusService eventBusService = new EventBusService();

        MoTypeService moTypeService = new MockMoTypeService();
        MoTypeCache moTypeCache = new MoTypeCache(moTypeService);

        MetricsService metricsService = new MockMetricsServiceImpl();
        MetricsCache metricsCache = new MetricsCache(metricsService);

        RuleService ruleService = new MockRuleServiceImpl();
        OmRuleCache omRuleCache = new OmRuleCache(ruleService);
        AlarmRuleCache alarmRuleCache = new AlarmRuleCache(ruleService);

        MoService moService = new MockMoServiceImpl();
        MoCache moCache = new MoCache(moService);

        HitLogService hitLogService = new MockHitLogServiceImpl();
        OmHitLogCache omHitLogCache = new OmHitLogCache(hitLogService, moCache);
        AlarmHitLogCache alarmHitLogCache = new AlarmHitLogCache(hitLogService, moCache);

        HitLogPoller hitLogPoller = new HitLogPoller();
        hitLogPoller.setAlarmHitLogCache(alarmHitLogCache);
        hitLogPoller.setOmHitLogCache(omHitLogCache);
        eventBusService.getEventBus().register(hitLogPoller);

        CacheCreateService service = new CacheCreateService();
        service.setMoTypeCache(moTypeCache);
        service.setMoCache(moCache);
        service.setMetricsCache(metricsCache);
        service.setOmRuleCache(omRuleCache);
        service.setOmHitLogCache(omHitLogCache);
        service.setAlarmRuleCache(alarmRuleCache);
        service.setAlarmHitLogCache(alarmHitLogCache);
        service.setEventBusService(eventBusService);

        service.load();
    }

}
