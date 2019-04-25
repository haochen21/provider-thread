package com.beta.providerthread.cache;

import com.beta.providerthread.model.AlarmRule;
import com.beta.providerthread.model.OmRule;
import com.beta.providerthread.model.Rule;
import com.beta.providerthread.service.RuleService;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@NoArgsConstructor
public class RuleCache implements CacheInit {

    @Autowired
    private RuleService ruleService;

    private ConcurrentHashMap<String, OmRule> omRuleCache;

    private ConcurrentHashMap<String, AlarmRule> alarmRuleCache;

    private static final Logger logger = LoggerFactory.getLogger(HitLogCache.class);

    public RuleCache(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    public void init() {

    }

    @Override
    public Runnable load() {
        return () -> {
            logger.info("load rule cache start.......");
            sleep(3000);
            omRuleCache = ruleService.findAllOmRules().stream().collect(
                    Collectors.toMap(Rule::getId,
                            Function.identity(), (m1, m2) -> m1, ConcurrentHashMap::new));

            alarmRuleCache = ruleService.findAllAlarmRules().stream().collect(
                    Collectors.toMap(Rule::getId,
                            Function.identity(), (m1, m2) -> m1, ConcurrentHashMap::new));
            logger.info("load rule cache end.......");
        };
    }
}
