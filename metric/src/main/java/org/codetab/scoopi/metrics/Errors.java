package org.codetab.scoopi.metrics;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.codahale.metrics.Counter;

@Singleton
public class Errors {

    @Inject
    private MetricsHelper metricsHelper;

    private Counter counter;

    public void start() {
        counter = metricsHelper.getCounter(this, "system", "error");
    }

    public void inc() {
        counter.inc();
    }

    public long getCount() {
        return counter.getCount();
    }
}
