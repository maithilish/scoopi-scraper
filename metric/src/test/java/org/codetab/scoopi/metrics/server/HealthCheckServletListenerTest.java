package org.codetab.scoopi.metrics.server;

import static org.junit.Assert.assertSame;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.codahale.metrics.health.HealthCheckRegistry;

public class HealthCheckServletListenerTest {
    @InjectMocks
    private HealthCheckServletListener healthCheckServletListener;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetHealthCheckRegistry() throws IllegalAccessException {

        Object expected =
                FieldUtils.readStaticField(HealthCheckServletListener.class,
                        "HEALTH_CHECK_REGISTRY", true);

        HealthCheckRegistry actual =
                healthCheckServletListener.getHealthCheckRegistry();

        assertSame(expected, actual);
    }
}
