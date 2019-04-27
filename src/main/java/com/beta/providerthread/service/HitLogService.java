package com.beta.providerthread.service;

import com.beta.providerthread.model.AlarmHitLog;
import com.beta.providerthread.model.OmHitLog;

import java.util.List;

public interface HitLogService {

    List<OmHitLog> findOmHitLogs();

    List<AlarmHitLog> findAlarmHitLogs();
}
