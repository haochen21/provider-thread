package com.beta.providerthread.mock;

import com.beta.providerthread.model.AlarmHitLog;
import com.beta.providerthread.model.OmHitLog;
import com.beta.providerthread.service.HitLogService;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@NoArgsConstructor
public class MockHitLogServiceImpl implements HitLogService {

    private static final Logger logger = LoggerFactory.getLogger(MockHitLogServiceImpl.class);

    @Override
    public List<OmHitLog> findOmHitLogs() {
        List<OmHitLog> omHitLogs = new ArrayList<>();
        try {
            int sleep = new Random().nextInt(10) + 100;
            Thread.sleep(sleep);

            OmHitLog cpuStatusHitLog = new OmHitLog();
            cpuStatusHitLog.setRuleId("1");
            cpuStatusHitLog.setCategoryName("host");
            cpuStatusHitLog.setMoTypeName("Windows");
            cpuStatusHitLog.setMoId("1");
            omHitLogs.add(cpuStatusHitLog);

            OmHitLog windowsProcessOmHitLog = new OmHitLog();
            windowsProcessOmHitLog.setRuleId("2");
            windowsProcessOmHitLog.setCategoryName("host");
            windowsProcessOmHitLog.setMoTypeName("Windows");
            windowsProcessOmHitLog.setMoId("1");
            omHitLogs.add(windowsProcessOmHitLog);

            logger.info("find omHitLog time is: {}", sleep);
        } catch (Exception ex) {
            logger.error("find omHitLog error!", ex);
        }
        omHitLogs.clear();
        return omHitLogs;
    }

    @Override
    public List<AlarmHitLog> findAlarmHitLogs() {
        List<AlarmHitLog> alarmHitLogs = new ArrayList<>();
        try {
            int sleep = new Random().nextInt(100) + 100;
            Thread.sleep(sleep);

            for (int i = 0; i < 3000; i++) {
                AlarmHitLog windowsStatusAlarmHitLog = new AlarmHitLog();
                windowsStatusAlarmHitLog.setRuleId("1");
                windowsStatusAlarmHitLog.setCategoryName("host");
                windowsStatusAlarmHitLog.setMoTypeName("Windows");
                windowsStatusAlarmHitLog.setMoId("" + i);
                alarmHitLogs.add(windowsStatusAlarmHitLog);
            }

            logger.info("find alarmHitLog time is: {}", sleep);
        } catch (Exception ex) {
            logger.error("find alarmHitLog error!", ex);
        }
        return alarmHitLogs;
    }
}
