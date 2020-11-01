package org.codetab.scoopi.metrics.serialize;

import org.apache.commons.lang3.Validate;

public class Timer implements Metric {

    // CHECKSTYLE:OFF:
    public long count;
    public double max;
    public double mean;
    public double min;
    public double p50;
    public double p75;
    public double p95;
    public double p98;
    public double p99;
    public double p999;
    public double stddev;
    public double m15_rate;
    public double m1_rate;
    public double m5_rate;
    public double mean_rate;
    public String duration_units;
    public String rate_units;
    // CHECKSTYLE:ON:

    @Override
    public void aggregate(final Metric other) {
        Validate.isInstanceOf(Timer.class, other);

        Timer oth = (Timer) other;
        count += oth.count;
        if (max < oth.max) {
            max = oth.max;
        }
        if (min > oth.min) {
            min = oth.min;
        }
        mean = ((count * mean) + (oth.count * oth.mean)) / (count + oth.count);

        p50 = (p50 + oth.p50) / 2;
        p75 = (p75 + oth.p75) / 2;
        p95 = (p95 + oth.p95) / 2;
        p98 = (p98 + oth.p98) / 2;
        p99 = (p99 + oth.p99) / 2;
        p999 = (p999 + oth.p999) / 2;

        m15_rate = (m15_rate + oth.m15_rate) / 2;
        m1_rate = (m1_rate + oth.m1_rate) / 2;
        m5_rate = (m5_rate + oth.m5_rate) / 2;
        mean_rate = (mean_rate + oth.mean_rate) / 2;
    }
}
