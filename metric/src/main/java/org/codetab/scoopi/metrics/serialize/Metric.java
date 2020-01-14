package org.codetab.scoopi.metrics.serialize;

public interface Metric {

    void aggregate(Metric other);
}
