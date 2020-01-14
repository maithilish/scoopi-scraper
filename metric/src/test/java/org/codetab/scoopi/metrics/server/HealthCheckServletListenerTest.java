package org.codetab.scoopi.metrics.server;

import static org.assertj.core.api.Assertions.assertThat;

import org.codetab.scoopi.metrics.InetHealthCheck;
import org.junit.Before;
import org.junit.Test;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;

public class HealthCheckServletListenerTest {

    private HealthCheckServletListener hcsl;

    @Before
    public void setUp() throws Exception {
        hcsl = new HealthCheckServletListener();
    }

    @Test
    public void testGetHealthCheckRegistry() {
        HealthCheck hc = new InetHealthCheck();
        HealthCheckRegistry registry = hcsl.getHealthCheckRegistry();
        registry.register("hc1", hc);
        assertThat(registry.getHealthCheck("hc1")).isEqualTo(hc);
    }

}
