package org.codetab.scoopi.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.servlets.MetricsServlet;

public class MetricsServletListener extends MetricsServlet.ContextListener {

    static final MetricRegistry METRIC_REGISTRY =
            SharedMetricRegistries.getOrCreate("scoopi");

    @Override
    protected MetricRegistry getMetricRegistry() {
        return METRIC_REGISTRY;
    }

}
