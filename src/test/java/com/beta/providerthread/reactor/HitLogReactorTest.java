package com.beta.providerthread.reactor;

import com.beta.providerthread.cache.*;
import com.beta.providerthread.concurrent.HitLogPoller;
import com.beta.providerthread.eventbus.EventBusService;
import com.beta.providerthread.mock.*;
import com.beta.providerthread.monitor.MetricsMonitorService;
import com.beta.providerthread.service.*;
import org.junit.Test;

public class HitLogReactorTest {

    @Test
    public void testSampling() {

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

        MetricsValueCache metricsValueCache = new MetricsValueCache();
        metricsValueCache.init();

        HitLogService hitLogService = new MockHitLogServiceImpl();
        OmHitLogCache omHitLogCache = new OmHitLogCache(hitLogService, moCache);
        AlarmHitLogCache alarmHitLogCache = new AlarmHitLogCache(hitLogService, moCache);

        CircuitBreakerService circuitBreakerService = new CircuitBreakerService();
        SemaphoreService semaphoreService = new SemaphoreService();

        HitLogReactor hitLogReactor = new HitLogReactor();
        hitLogReactor.setAlarmHitLogCache(alarmHitLogCache);
        hitLogReactor.setOmHitLogCache(omHitLogCache);
        hitLogReactor.setCircuitBreakerService(circuitBreakerService);
        hitLogReactor.setSemaphoreService(semaphoreService);

        hitLogReactor.init();
        eventBusService.getEventBus().register(hitLogReactor);


        CacheCreateService service = new CacheCreateService();
        service.setMoTypeCache(moTypeCache);
        service.setMoCache(moCache);
        service.setMetricsCache(metricsCache);
        service.setOmRuleCache(omRuleCache);
        service.setOmHitLogCache(omHitLogCache);
        service.setAlarmRuleCache(alarmRuleCache);
        service.setAlarmHitLogCache(alarmHitLogCache);
        service.setEventBusService(eventBusService);

        new Thread(() -> service.load()).start();

        while (true) {
            try {
                Thread.sleep(600 * 1000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }
}
