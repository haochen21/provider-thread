package com.beta.providerthread.service;

import com.beta.providerthread.model.AlarmRule;
import com.beta.providerthread.model.OmRule;

import java.util.List;

public interface RuleService {

    List<OmRule> findAllOmRules();

    List<AlarmRule> findAllAlarmRules();
}
