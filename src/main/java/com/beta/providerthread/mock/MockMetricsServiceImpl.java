package com.beta.providerthread.mock;

import com.beta.providerthread.model.Metrics;
import com.beta.providerthread.model.ProviderType;
import com.beta.providerthread.service.MetricsService;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@NoArgsConstructor
public class MockMetricsServiceImpl implements MetricsService {

    private static final Logger logger = LoggerFactory.getLogger(MockMetricsServiceImpl.class);

    @Override
    public List<Metrics> findAll() {
        List<Metrics> metricsList = new ArrayList<>();
        try {
            int sleep = new Random().nextInt(2000) + 1000;
            Thread.sleep(sleep);
            Metrics cpuStatusMetrics = new Metrics();
            cpuStatusMetrics.setId("1");
            cpuStatusMetrics.setName("cpuStatus");
            cpuStatusMetrics.setCategoryName("host");
            cpuStatusMetrics.setMoTypeName("Windows");
            cpuStatusMetrics.setProvider("com.beta.providerthread.provider.CacheMetricsProvider");
            cpuStatusMetrics.setProviderType(ProviderType.CPU);
            metricsList.add(cpuStatusMetrics);

            Metrics windowsStatusMetrics = new Metrics();
            windowsStatusMetrics.setId("2");
            windowsStatusMetrics.setName("windowsStatus");
            windowsStatusMetrics.setCategoryName("host");
            windowsStatusMetrics.setMoTypeName("Windows");
            windowsStatusMetrics.setProvider("com.beta.providerthread.provider.WindowsStatusMetricsProvider");
            windowsStatusMetrics.setProviderType(ProviderType.RPC);
            metricsList.add(windowsStatusMetrics);

            Metrics windowsProcessMetrics = new Metrics();
            windowsProcessMetrics.setId("3");
            windowsProcessMetrics.setName("windowsProcess");
            windowsProcessMetrics.setCategoryName("host");
            windowsProcessMetrics.setMoTypeName("Windows");
            windowsProcessMetrics.setProvider("com.beta.providerthread.provider.RpcMetricsProvider");
            windowsProcessMetrics.setProviderType(ProviderType.RPC);
            windowsProcessMetrics.setServiceUrl("http://127.0.0.1/api/v1/metrics");
            metricsList.add(windowsProcessMetrics);

            logger.info("find metrics time is: {}", sleep);
        }catch (Exception ex) {
            logger.error("find metrics error!", ex);
        }

        return metricsList;
    }
}
