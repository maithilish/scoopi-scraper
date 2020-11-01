package org.codetab.scoopi.metrics.server;

import org.codetab.scoopi.metrics.aggregate.Aggregator;

public class MetricsServletListener extends MetricsServlet.ContextListener {

    @Override
    protected Aggregator getMetricsAggregator() {
        return new Aggregator();
    }

}
