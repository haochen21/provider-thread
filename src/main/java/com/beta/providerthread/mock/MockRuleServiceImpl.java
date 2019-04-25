package com.beta.providerthread.mock;

import com.beta.providerthread.model.AlarmRule;
import com.beta.providerthread.model.MoType;
import com.beta.providerthread.model.OmRule;
import com.beta.providerthread.model.RuleType;
import com.beta.providerthread.service.RuleService;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@NoArgsConstructor
public class MockRuleServiceImpl implements RuleService {

    @Override
    public List<OmRule> findAllOmRules() {
        List<OmRule> omRules = new ArrayList<>();

        OmRule cpuStatusOmRule = new OmRule();
        cpuStatusOmRule.setId("1");
        cpuStatusOmRule.setRuleType(RuleType.OM);
        cpuStatusOmRule.setMoType(MoType.WINDOWS);
        cpuStatusOmRule.setMetricsName("cpuStatus");
        cpuStatusOmRule.setMoFilter("1=1");
        cpuStatusOmRule.setSampleInterval(10L);
        omRules.add(cpuStatusOmRule);

        OmRule windowsProcessOmRule = new OmRule();
        windowsProcessOmRule.setId("2");
        windowsProcessOmRule.setRuleType(RuleType.OM);
        windowsProcessOmRule.setMoType(MoType.WINDOWS);
        windowsProcessOmRule.setMetricsName("windowsProcess");
        windowsProcessOmRule.setMoFilter("1=1");
        windowsProcessOmRule.setSampleInterval(30L);
        omRules.add(cpuStatusOmRule);

        return omRules;
    }

    @Override
    public List<AlarmRule> findAllAlarmRules() {
        List<AlarmRule> alarmRules = new ArrayList<>();

        AlarmRule windowsStatusOmRule = new AlarmRule();
        windowsStatusOmRule.setId("1");
        windowsStatusOmRule.setRuleType(RuleType.ALARM);
        windowsStatusOmRule.setMoType(MoType.WINDOWS);
        windowsStatusOmRule.setMetricsName("windowsStatus");
        windowsStatusOmRule.setMoFilter("1=1");
        windowsStatusOmRule.setSampleInterval(60L);
        alarmRules.add(windowsStatusOmRule);

        return alarmRules;
    }
}
