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
public class OmHitLogCache implements Function<Map<String, OmRule>,
        Map<String, OmHitLog>> {

    private HitLogService hitLogService;

    private MoCache moCache;

    // key: ruleId+"."+moId
    private ConcurrentHashMap<String, OmHitLog> cache;

    private static final Logger logger = LoggerFactory.getLogger(OmHitLogCache.class);

    public OmHitLogCache(HitLogService hitLogService, MoCache moCache) {
        this.hitLogService = hitLogService;
        this.moCache = moCache;
    }

    @Override
    public Map<String, OmHitLog> apply(Map<String, OmRule> omRuleMap) {
        logger.info("load omHitLog cache start.......");
        cache = hitLogService.findOmHitLogs().stream()
                .map(omHitLog -> {
                    OmRule omRule = omRuleMap.get(omHitLog.getRuleId());
                    omHitLog.setRule(omRule);

                    String moKey = omRule.getMetrics().getMoType() + "." + omHitLog.getMoId();
                    Mo mo = moCache.get(moKey);
                    omHitLog.setMo(mo);

                    return omHitLog;
                }).collect(
                        Collectors.toMap(omHitLog -> omHitLog.getRuleId() + "." + omHitLog.getMoId(),
                                Function.identity(), (m1, m2) -> m1, ConcurrentHashMap::new));
        logger.info("load omHitLog cache end.......");
        return cache;
    }

    public ConcurrentHashMap<String, OmHitLog> getCache() {
        return cache;
    }

    public static void main(String[] args) {
        MoTypeService moTypeService = new MockMoTypeService();
        MoTypeCache moTypeCache = new MoTypeCache(moTypeService);

        MetricsService metricsService = new MockMetricsServiceImpl();
        MetricsCache metricsCache = new MetricsCache(metricsService);

        RuleService ruleService = new MockRuleServiceImpl();
        OmRuleCache omRuleCache = new OmRuleCache(ruleService);

        MoService moService = new MockMoServiceImpl();
        MoCache moCache = new MoCache(moService);

        HitLogService hitLogService = new MockHitLogServiceImpl();
        OmHitLogCache omHitLogCache = new OmHitLogCache(hitLogService, moCache);

        CompletableFuture<Map<String, MoType>> moTypeFuture = CompletableFuture
                .supplyAsync(moTypeCache);
        CompletableFuture<Map<String, Mo>> moFuture = moTypeFuture.thenApplyAsync(moCache);
        CompletableFuture<Map<String, Metrics>> metricsFuture = moTypeFuture.thenApplyAsync(metricsCache);

        CompletableFuture<Map<String, Metrics>> cf2 = moFuture.thenCombine(metricsFuture, (s, s2) -> s2);

        CompletableFuture<Map<String, OmRule>> omRuleFuture = cf2.thenApplyAsync(omRuleCache);
        CompletableFuture<Map<String, OmHitLog>> omHitLogFuture = omRuleFuture.thenApplyAsync(omHitLogCache);
        omHitLogFuture.thenAcceptAsync(omHitLogMap ->
                omHitLogMap.forEach((k, v) -> logger.info(k + ":" + v.toString()))).join();
    }
}
