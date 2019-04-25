package com.beta.providerthread.mock;

import com.beta.providerthread.model.Metrics;
import com.beta.providerthread.model.MoType;
import com.beta.providerthread.model.ProviderType;
import com.beta.providerthread.service.MetricsService;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@NoArgsConstructor
public class MockMetricsServiceImpl implements MetricsService {

    @Override
    public List<Metrics> findAll() {
        List<Metrics> metricsList = new ArrayList<>();

        Metrics cpuStatusMetrics = new Metrics();
        cpuStatusMetrics.setName("cpuStatus");
        cpuStatusMetrics.setMoType(MoType.WINDOWS);
        cpuStatusMetrics.setProvider("com.beta.providerthread.provider.CacheMetricsProvider");
        cpuStatusMetrics.setProviderType(ProviderType.CPU);
        metricsList.add(cpuStatusMetrics);

        Metrics windowsStatusMetrics = new Metrics();
        windowsStatusMetrics.setName("windowsStatus");
        windowsStatusMetrics.setMoType(MoType.WINDOWS);
        windowsStatusMetrics.setProvider("com.beta.providerthread.provider.WindowsStatusMetricsProvider");
        windowsStatusMetrics.setProviderType(ProviderType.RPC);
        metricsList.add(windowsStatusMetrics);

        Metrics windowsProcessMetrics = new Metrics();
        windowsProcessMetrics.setName("windowsProcess");
        windowsProcessMetrics.setMoType(MoType.WINDOWS);
        windowsProcessMetrics.setProvider("com.beta.providerthread.provider.RpcMetricsProvider");
        windowsProcessMetrics.setProviderType(ProviderType.RPC);
        windowsProcessMetrics.setServiceUrl("http://127.0.0.1/api/v1/metrics");
        metricsList.add(windowsProcessMetrics);

        return metricsList;
    }
}
