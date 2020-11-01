package org.codetab.scoopi.metrics.serialize;

import java.util.Map;

public class Metrics {

    private String version = "4.0.0";
    private Map<String, Gauge> gauges;
    private Map<String, Counter> counters;
    private Map<String, Histogram> histograms;
    private Map<String, Meter> meters;
    private Map<String, Timer> timers;

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public Map<String, Gauge> getGauges() {
        return gauges;
    }

    public void setGauges(final Map<String, Gauge> gauges) {
        this.gauges = gauges;
    }

    public Map<String, Counter> getCounters() {
        return counters;
    }

    public void setCounters(final Map<String, Counter> counters) {
        this.counters = counters;
    }

    public Map<String, Histogram> getHistograms() {
        return histograms;
    }

    public void setHistograms(final Map<String, Histogram> histograms) {
        this.histograms = histograms;
    }

    public Map<String, Meter> getMeters() {
        return meters;
    }

    public void setMeters(final Map<String, Meter> meters) {
        this.meters = meters;
    }

    public Map<String, Timer> getTimers() {
        return timers;
    }

    public void setTimers(final Map<String, Timer> timers) {
        this.timers = timers;
    }

    public void aggregate(final Metrics other) {

    }

}
