package org.codetab.scoopi.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;

public class MetricsServletListenerTest {

    @Test
    public void testGetMetricRegistry() {
        MetricRegistry expected = SharedMetricRegistries.getOrCreate("scoopi");
        MetricsServletListener msl = new MetricsServletListener();
        MetricRegistry actual = msl.getMetricRegistry();

        assertThat(actual).isSameAs(expected);
    }

}
