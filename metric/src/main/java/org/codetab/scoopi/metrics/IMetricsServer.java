package org.codetab.scoopi.metrics;

import java.util.Map;

public interface IMetricsServer {

    void start();

    void stop();

    void setMetricsJsonData(Map<String, byte[]> metricsJsonHolder);
}
