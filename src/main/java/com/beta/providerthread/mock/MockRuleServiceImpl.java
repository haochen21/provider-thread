package com.beta.providerthread.mock;

import com.beta.providerthread.model.AlarmRule;
import com.beta.providerthread.model.OmRule;
import com.beta.providerthread.model.RuleType;
import com.beta.providerthread.service.RuleService;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@NoArgsConstructor
public class MockRuleServiceImpl implements RuleService {

    private static final Logger logger = LoggerFactory.getLogger(MockRuleServiceImpl.class);

    @Override
    public List<OmRule> findAllOmRules() {
        List<OmRule> omRules = new ArrayList<>();
        try{
            int sleep = new Random().nextInt(2000) + 1000;
            Thread.sleep(sleep);

            OmRule cpuStatusOmRule = new OmRule();
            cpuStatusOmRule.setId("1");
            cpuStatusOmRule.setMetricsId("1");
            cpuStatusOmRule.setRuleType(RuleType.OM);
            cpuStatusOmRule.setMetricsName("cpuStatus");
            cpuStatusOmRule.setMoFilter("1=1");
            cpuStatusOmRule.setSampleInterval(10L);
            omRules.add(cpuStatusOmRule);

            OmRule windowsProcessOmRule = new OmRule();
            windowsProcessOmRule.setId("2");
            windowsProcessOmRule.setMetricsId("3");
            windowsProcessOmRule.setRuleType(RuleType.OM);
            windowsProcessOmRule.setMetricsName("windowsProcess");
            windowsProcessOmRule.setMoFilter("1=1");
            windowsProcessOmRule.setSampleInterval(20L);
            omRules.add(windowsProcessOmRule);
            logger.info("find omRule time is: {}", sleep);
        }catch (Exception ex) {
            logger.error("find omRule error!", ex);
        }
        return omRules;
    }

    @Override
    public List<AlarmRule> findAllAlarmRules() {
        List<AlarmRule> alarmRules = new ArrayList<>();
        try{
            int sleep = new Random().nextInt(2000) + 1000;
            Thread.sleep(sleep);

            AlarmRule windowsStatusAlarmRule = new AlarmRule();
            windowsStatusAlarmRule.setId("1");
            windowsStatusAlarmRule.setMetricsId("2");
            windowsStatusAlarmRule.setRuleType(RuleType.ALARM);
            windowsStatusAlarmRule.setMetricsName("windowsStatus");
            windowsStatusAlarmRule.setMoFilter("1=1");
            windowsStatusAlarmRule.setSampleInterval(60L);
            alarmRules.add(windowsStatusAlarmRule);

            logger.info("find alarmRule time is: {}", sleep);
        }catch (Exception ex) {
            logger.error("find alarmRule error!", ex);
        }

        return alarmRules;
    }
}
