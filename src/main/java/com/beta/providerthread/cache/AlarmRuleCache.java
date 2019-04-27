package com.beta.providerthread.cache;

import com.beta.providerthread.mock.MockMetricsServiceImpl;
import com.beta.providerthread.mock.MockMoTypeService;
import com.beta.providerthread.mock.MockRuleServiceImpl;
import com.beta.providerthread.model.*;
import com.beta.providerthread.service.MetricsService;
import com.beta.providerthread.service.MoTypeService;
import com.beta.providerthread.service.RuleService;
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
public class AlarmRuleCache implements Function<Map<String, Metrics>,
        Map<String, AlarmRule>> {

    private RuleService ruleService;

    private ConcurrentHashMap<String, AlarmRule> alarmRuleCache;

    private static final Logger logger = LoggerFactory.getLogger(AlarmRuleCache.class);

    public AlarmRuleCache(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @Override
    public Map<String, AlarmRule> apply(Map<String, Metrics> metricsMap) {
        logger.info("load alarmRule cache start.......");
        alarmRuleCache = ruleService.findAllAlarmRules().stream()
                .map(omRule -> {
                    Metrics metrics = metricsMap.get(omRule.getMetricsId());
                    omRule.setMetrics(metrics);
                    return omRule;
                }).collect(
                        Collectors.toMap(Rule::getId,
                                Function.identity(), (m1, m2) -> m1, ConcurrentHashMap::new));
        logger.info("load alarmRule cache end.......");
        return alarmRuleCache;
    }

    public static void main(String[] args) {
        MoTypeService moTypeService = new MockMoTypeService();
        MoTypeCache moTypeCache = new MoTypeCache(moTypeService);

        MetricsService metricsService = new MockMetricsServiceImpl();
        MetricsCache metricsCache = new MetricsCache(metricsService);

        RuleService ruleService = new MockRuleServiceImpl();
        AlarmRuleCache alarmRuleCache = new AlarmRuleCache(ruleService);

        CompletableFuture<Map<String, MoType>> moTypeFuture = CompletableFuture
                .supplyAsync(moTypeCache);
        CompletableFuture<Map<String, Metrics>> metricsFuture = moTypeFuture.thenApplyAsync(metricsCache);
        CompletableFuture<Map<String, AlarmRule>> alarmRuleFuture = metricsFuture.thenApplyAsync(alarmRuleCache);
        alarmRuleFuture.thenAcceptAsync(omRuleMap ->
                omRuleMap.forEach((k, v) -> logger.info(v.toString()))).join();
    }
}
