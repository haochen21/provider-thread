package com.beta.providerthread.cache;

import com.beta.providerthread.mock.*;
import com.beta.providerthread.model.*;
import com.beta.providerthread.service.*;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@NoArgsConstructor
public class AlarmHitLogCache implements Function<Map<String, AlarmRule>,
        Map<String, AlarmHitLog>> {

    private HitLogService hitLogService;

    private MoCache moCache;

    // key: ruleId+"."+moId
    private ConcurrentHashMap<String, AlarmHitLog> cache;

    private static final Logger logger = LoggerFactory.getLogger(AlarmHitLogCache.class);

    public AlarmHitLogCache(HitLogService hitLogService, MoCache moCache) {
        this.hitLogService = hitLogService;
        this.moCache = moCache;
    }

    @Override
    public Map<String, AlarmHitLog> apply(Map<String, AlarmRule> alarmRuleMap) {
        logger.info("load alarmHitLog cache start.......");
        cache = hitLogService.findAlarmHitLogs().stream()
                .map(alarmHitLog -> {
                    AlarmRule alarmRule = alarmRuleMap.get(alarmHitLog.getRuleId());
                    alarmHitLog.setRule(alarmRule);

                    String moKey = alarmRule.getMetrics().getMoType() + "." + alarmHitLog.getMoId();
                    Mo mo = moCache.get(moKey);
                    alarmHitLog.setMo(mo);

                    return alarmHitLog;
                }).collect(
                        Collectors.toMap(alarmHitLog -> alarmHitLog.getRuleId() + "." + alarmHitLog.getMoId(),
                                Function.identity(), (m1, m2) -> m1, ConcurrentHashMap::new));
        logger.info("load alarmHitLog cache end.......");
        return cache;
    }

    public ConcurrentHashMap<String, AlarmHitLog> getCache() {
        return cache;
    }

    public static void main(String[] args) {
        MoTypeService moTypeService = new MockMoTypeService();
        MoTypeCache moTypeCache = new MoTypeCache(moTypeService);

        MetricsService metricsService = new MockMetricsServiceImpl();
        MetricsCache metricsCache = new MetricsCache(metricsService);

        RuleService ruleService = new MockRuleServiceImpl();
        AlarmRuleCache alarmRuleCache = new AlarmRuleCache(ruleService);

        MoService moService = new MockMoServiceImpl();
        MoCache moCache = new MoCache(moService);

        HitLogService hitLogService = new MockHitLogServiceImpl();
        AlarmHitLogCache alarmHitLogCache = new AlarmHitLogCache(hitLogService, moCache);

        CompletableFuture<Map<String, MoType>> moTypeFuture = CompletableFuture
                .supplyAsync(moTypeCache);
        CompletableFuture<Map<String, Mo>> moFuture = moTypeFuture.thenApplyAsync(moCache);
        CompletableFuture<Map<String, Metrics>> metricsFuture = moTypeFuture.thenApplyAsync(metricsCache);

        CompletableFuture<Map<String, Metrics>> cf2 = moFuture.thenCombine(metricsFuture, (s, s2) -> s2);

        CompletableFuture<Map<String, AlarmRule>> alarmRuleFuture = cf2.thenApplyAsync(alarmRuleCache);
        CompletableFuture<Map<String, AlarmHitLog>> alarmHitLogFuture = alarmRuleFuture.thenApplyAsync(alarmHitLogCache);
        alarmHitLogFuture.thenAcceptAsync(alarmHitLogMap ->
                alarmHitLogMap.forEach((k, v) -> logger.info(k + ":" + v.toString()))).join();
    }
}
