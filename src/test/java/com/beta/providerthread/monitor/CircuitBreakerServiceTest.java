package com.beta.providerthread.monitor;

import com.beta.providerthread.model.Metrics;
import com.beta.providerthread.model.Mo;
import com.beta.providerthread.model.ProviderType;
import com.beta.providerthread.provider.RpcMetricsProvider;
import com.beta.providerthread.provider.TimeoutMetricProvider;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CircuitBreakerServiceTest {

    @Test
    public void testCircuitBreaker() throws Exception {
        CircuitBreakerService service = new CircuitBreakerService();
        CircuitBreaker circuitBreaker = service.getCircuitBreaker("windowsProcess");

        RpcMetricsProvider provider = new RpcMetricsProvider();
        TimeoutMetricProvider timeoutMetricProvider = new TimeoutMetricProvider();

        Mo mo = new Mo();
        mo.setId("1");
        mo.setCategoryName("host");
        mo.setMoTypeName("Windows");
        mo.setName("host001");
        mo.setIp("127.0.0.1");

        Metrics windowsProcessMetrics = new Metrics();
        windowsProcessMetrics.setId("3");
        windowsProcessMetrics.setName("windowsProcess");
        windowsProcessMetrics.setCategoryName("host");
        windowsProcessMetrics.setMoTypeName("Windows");
        windowsProcessMetrics.setProvider("com.beta.providerthread.provider.RpcMetricsProvider");
        windowsProcessMetrics.setProviderType(ProviderType.RPC);
        windowsProcessMetrics.setServiceUrl("http://127.0.0.1/api/v1/metrics");

        CheckedFunction0<String> decoratedSupplier = CircuitBreaker.decorateCheckedSupplier(circuitBreaker,
                () -> {
                    throw new RuntimeException();
                });
        Try<String> result = Try.of(decoratedSupplier);
        Thread.sleep(1000);

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        decoratedSupplier = CircuitBreaker.decorateCheckedSupplier(circuitBreaker,
                () -> {
                    throw new RuntimeException();
                });
        result = Try.of(decoratedSupplier);
        Thread.sleep(1000);


        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        decoratedSupplier = CircuitBreaker.decorateCheckedSupplier(circuitBreaker,
                () -> {
                    throw new RuntimeException();
                });
        result = Try.of(decoratedSupplier);

        Thread.sleep(1000);
        decoratedSupplier = CircuitBreaker.decorateCheckedSupplier(circuitBreaker,
                () -> {
                    throw new RuntimeException();
                });
        result = Try.of(decoratedSupplier);

        Thread.sleep(1000);
        decoratedSupplier = CircuitBreaker.decorateCheckedSupplier(circuitBreaker,
                () -> {
                    throw new RuntimeException();
                });
        result = Try.of(decoratedSupplier);

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.HALF_OPEN);

    }
}
