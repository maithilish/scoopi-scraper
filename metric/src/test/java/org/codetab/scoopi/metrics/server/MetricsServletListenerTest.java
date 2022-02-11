package org.codetab.scoopi.metrics.server;

import static org.junit.Assert.assertNotNull;

import org.codetab.scoopi.metrics.aggregate.Aggregator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class MetricsServletListenerTest {
    @InjectMocks
    private MetricsServletListener metricsServletListener;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testgetMetricsAggregator() {
        Aggregator actual = metricsServletListener.getMetricsAggregator();

        assertNotNull(actual);
    }
}
